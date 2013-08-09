package org.catrobat.wifihidserver.tests;

import static org.junit.Assert.*;

import org.catrobat.catroid.io.Command;
import org.catrobat.catroid.io.Command.commandType;
import org.catrobat.catroid.io.Confirmation.ConfirmationState;
import org.catrobat.catroid.io.Confirmation;
import org.catrobat.wifihidserver.Connection;
import org.catrobat.wifihidserver.ConnectionHandler;
import org.catrobat.wifihidserver.ConnectionListener;
import org.catrobat.wifihidserver.InputHandler;
import org.catrobat.wifihidserver.KeyBoard;
import org.catrobat.wifihidserver.Server;
import org.catrobat.wifihidserver.StartUI;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

import java.beans.DesignMode;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.AbstractMap;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.invocation.realmethod.RealMethod;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Server.class})
public class ConnectionTest {
	
	private SocketAddress socketAddress;
	private ConnectionListener connectionListener;
	private static final String testIp = "www.google.at";
	
	@Test
	public void testConnectionListener() throws Exception {
		final DatagramSocket mockSocket = EasyMock.createNiceMock(DatagramSocket.class);
		final byte[] message = new byte[1];

		connectionListener = new ConnectionListener() {
			@Override
			public DatagramSocket createNewSocket() {
				return mockSocket;
			}
		};
		mockSocket.receive(EasyMock.anyObject(DatagramPacket.class));
		EasyMock.expectLastCall().andDelegateTo(new DatagramSocket() {
			@Override
			public void receive(DatagramPacket datagramPacket) {
				socketAddress = new InetSocketAddress(testIp, 64000);
				datagramPacket.setSocketAddress(socketAddress);
				assertTrue("ConnectionListener: not set right", datagramPacket.getSocketAddress().equals(socketAddress));
				datagramPacket.setData(message);
				datagramPacket.setLength(1);
			}
		});
		mockSocket.send(EasyMock.anyObject(DatagramPacket.class));
		EasyMock.expectLastCall().andDelegateTo((new DatagramSocket() {
			@Override
			public void send(DatagramPacket datagramPacket) {
				assertTrue("ConnectionListener: illegal socketAddress\n",
						datagramPacket.getSocketAddress().equals(socketAddress));
			}
		}));
		mockSocket.close();
		EasyMock.expectLastCall().andDelegateTo((new DatagramSocket() {
			@Override
			public void close() {
				connectionListener.stopThread();
			}
		}));
		EasyMock.replay(mockSocket);
		connectionListener.start();
		Thread.sleep(5000L);
		EasyMock.verify(mockSocket); 	
	}	
	
	@Test
	public void testConnectionHandler() throws Exception {
		final Server mockServer = EasyMock.createNiceMock(Server.class);
		final InputHandler mockInputHandler = EasyMock.createNiceMock(InputHandler.class);
		final StartUI mockUiThread = EasyMock.createNiceMock(StartUI.class);
		final ServerSocket mockServerSocket = EasyMock.createNiceMock(ServerSocket.class);
		final Socket mockSocket = EasyMock.createNiceMock(Socket.class);
		final Connection mockConnection = EasyMock.createNiceMock(Connection.class);
		ConnectionHandler connectionHandler = new ConnectionHandler(mockInputHandler, mockUiThread, mockServer) {

			@Override
			public ServerSocket createNewServerSocket() {
				return mockServerSocket;
			}

			@Override
			public Connection createNewConnection(Socket client, int connectionCount) {
				assertTrue("ConnectionHandler: invalid Socket.", client.equals(mockSocket));
				return mockConnection;
			}
		};
		EasyMock.expect(mockServerSocket.accept()).andDelegateTo((new ServerSocket() {
			@Override
			public Socket accept() {
				return mockSocket;
			}
		}));
		mockConnection.start();
		EasyMock.expectLastCall();
		EasyMock.replay(mockServer, mockInputHandler, mockUiThread, mockServerSocket, mockSocket, mockConnection);
		connectionHandler.start();
		Thread.sleep(1000L);
		EasyMock.verify(mockServer, mockInputHandler, mockUiThread, mockServerSocket, mockSocket, mockConnection);
	}

	@Test
	public void testConnectionLegal() throws Exception {
		final Server mockServer = EasyMock.createNiceMock(Server.class);
		final InputHandler mockInputHandler = EasyMock.createNiceMock(InputHandler.class);
		final StartUI mockUiThread = EasyMock.createNiceMock(StartUI.class);
		final Socket mockSocket = EasyMock.createNiceMock(Socket.class);
		final ConnectionHandler mockConnectionHandler = EasyMock.createNiceMock(ConnectionHandler.class);
		final Command command = new Command('a', commandType.SINGLE_KEY);

		EasyMock.expect(mockSocket.getInputStream()).andReturn(serialize(command));
		final SocketAddress socketAddress = new InetSocketAddress(testIp, 64000);
		EasyMock.expect(mockSocket.getRemoteSocketAddress()).andReturn(socketAddress).anyTimes();
		mockInputHandler.onIncoming(EasyMock.isA(Command.class), EasyMock.isA(Connection.class));
		EasyMock.expectLastCall().andDelegateTo(new InputHandler(new KeyBoard() {
		}) {
			@Override
			public void onIncoming(Command command_, Connection connection) {
				assertTrue("Connection: Illegal input (not of type command).", command_ instanceof Command);
			}
		});
		EasyMock.replay(mockSocket, mockServer, mockInputHandler, mockUiThread, mockConnectionHandler);
		Connection connection = new Connection(mockSocket, mockInputHandler, mockUiThread, 3, mockConnectionHandler,
				mockServer) {
		};
		connection.start();
		Thread.sleep(1000L);
		connection.stopThread();
		EasyMock.verify(mockSocket, mockServer, mockInputHandler, mockUiThread, mockConnectionHandler);
	}
	
	private byte[] receivedObject;
	@Test
	public void testConnectionIllegal() throws Exception {
		final Server mockServer = EasyMock.createNiceMock(Server.class);
		final InputHandler mockInputHandler = EasyMock.createNiceMock(InputHandler.class);
		final StartUI mockUiThread = EasyMock.createNiceMock(StartUI.class);
		final Socket mockSocket = EasyMock.createNiceMock(Socket.class);
		final ConnectionHandler mockConnectionHandler = EasyMock.createNiceMock(ConnectionHandler.class);
		final Confirmation confirmation = new Confirmation(ConfirmationState.COMMAND_SEND_SUCCESSFULL);

		receivedObject = new byte[1000];
		EasyMock.expect(mockSocket.getInputStream()).andReturn(serialize(confirmation));
		EasyMock.expect(mockSocket.getOutputStream()).andReturn(new OutputStream() {

			private int byteCount = 0;

			@Override
			public void write(int b) throws IOException {
				receivedObject[byteCount] = (byte) b;
				byteCount++;
			}
		});

		final SocketAddress socketAddress = new InetSocketAddress(testIp, 64000);
		EasyMock.expect(mockSocket.getRemoteSocketAddress()).andReturn(socketAddress).anyTimes();
		mockSocket.close();
		EasyMock.expectLastCall();
		EasyMock.replay(mockSocket, mockServer, mockInputHandler, mockUiThread, mockConnectionHandler);
		Connection connection = new Connection(mockSocket, mockInputHandler, mockUiThread, 3, mockConnectionHandler,
				mockServer) {
		};
		connection.start();
		Thread.sleep(1000L);
		assertTrue("ConnectionIllegal: Response not of type Confirmation",
				deserialize(receivedObject) instanceof Confirmation);
		assertTrue(
				"sadjkfsadlj",
				((Confirmation) deserialize(receivedObject)).getConfirmationState().equals(
						ConfirmationState.ILLEGAL_CLASS));
		connection.stopThread();
		EasyMock.verify(mockSocket, mockServer, mockInputHandler, mockUiThread, mockConnectionHandler);
	}
	
	private InputStream serialize(Object object) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
	    objectOutputStream.writeObject(object);
	    objectOutputStream.flush();
	    objectOutputStream.close();
	    InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
	    return inputStream;
	}
	
	private Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
	    ByteArrayInputStream in = new ByteArrayInputStream(data);
	    ObjectInputStream is = new ObjectInputStream(in);
	    return is.readObject();
	}
	
	private final int key = 3;
	private int[] keyCombination = new int[2];
	@Test
	public void testInputHandlerLegalArguments() throws Exception {
		final Connection mockConnection = EasyMock.createNiceMock(Connection.class);
		final Command mockCommandSingleKey = EasyMock.createNiceMock(Command.class);
		final Command mockCommandKeyCombination = EasyMock.createNiceMock(Command.class);

		final KeyBoard mockKeyboard = PowerMock.createNiceMock(KeyBoard.class);
		EasyMock.expect(mockCommandSingleKey.getCommandType()).andReturn(commandType.SINGLE_KEY).times(4);
		EasyMock.expect(mockConnection.getIp()).andReturn(testIp).anyTimes();
		EasyMock.expect(mockCommandKeyCombination.getCommandType()).andReturn(commandType.KEY_COMBINATION).times(3);

		keyCombination[0] = 123456;
		keyCombination[1] = 234567;
		EasyMock.expect(mockCommandSingleKey.getKey()).andReturn(key).anyTimes();
		EasyMock.expect(mockCommandKeyCombination.getKeyComb()).andReturn(keyCombination).anyTimes();
		EasyMock.expect(mockKeyboard.setKeyToHandle(EasyMock.anyInt())).andReturn(false);
		EasyMock.expect(mockKeyboard.setKeyToHandle(keyCombination)).andReturn(false);
		mockConnection.confirm(ConfirmationState.ILLEGAL_COMMAND);
		EasyMock.expectLastCall().times(2);
		EasyMock.replay(mockCommandSingleKey, mockConnection, mockCommandKeyCombination, mockKeyboard);

		InputHandler inputHandler = new InputHandler(mockKeyboard);
		inputHandler.start();
		inputHandler.onIncoming(mockCommandSingleKey, mockConnection);
		inputHandler.onIncoming(mockCommandKeyCombination, mockConnection);

		Thread.sleep(1000L);
		EasyMock.verify(mockCommandSingleKey, mockCommandKeyCombination, mockKeyboard, mockConnection);
	}
	
	@Test
	public void testInputHandlerIllegalArguments() throws Exception {
		final Connection mockConnection = EasyMock.createNiceMock(Connection.class);
		final Command mockCommandSingleKey = EasyMock.createNiceMock(Command.class);
		final Command mockCommandKeyCombination = EasyMock.createNiceMock(Command.class);

		final KeyBoard mockKeyboard = PowerMock.createNiceMock(KeyBoard.class);
		EasyMock.expect(mockCommandSingleKey.getCommandType()).andReturn(commandType.SINGLE_KEY).times(4);
		EasyMock.expect(mockConnection.getIp()).andReturn(testIp).anyTimes();
		EasyMock.expect(mockCommandKeyCombination.getCommandType()).andReturn(commandType.KEY_COMBINATION).times(3);

		keyCombination[0] = 123456;
		keyCombination[1] = 234567;
		EasyMock.expect(mockCommandSingleKey.getKey()).andReturn(key).anyTimes();
		EasyMock.expect(mockCommandKeyCombination.getKeyComb()).andReturn(keyCombination).anyTimes();
		EasyMock.expect(mockKeyboard.setKeyToHandle(EasyMock.anyInt())).andReturn(false);
		EasyMock.expect(mockKeyboard.setKeyToHandle(keyCombination)).andReturn(false);
		mockConnection.confirm(ConfirmationState.ILLEGAL_COMMAND);
		EasyMock.expectLastCall().times(2);
		EasyMock.replay(mockCommandSingleKey, mockConnection, mockCommandKeyCombination, mockKeyboard);

		InputHandler inputHandler = new InputHandler(mockKeyboard);
		inputHandler.start();
		inputHandler.onIncoming(mockCommandSingleKey, mockConnection);
		inputHandler.onIncoming(mockCommandKeyCombination, mockConnection);

		Thread.sleep(1000L);
		EasyMock.verify(mockCommandSingleKey, mockCommandKeyCombination, mockKeyboard, mockConnection);
	}
	
	@Test
	public void testStartingAndStoppingServer() throws Exception {
		final StartUI mockUiThread = PowerMock.createNiceMock(StartUI.class);
		final ConnectionListener mockConnectionListener = PowerMock.createNiceMock(ConnectionListener.class);
		final Object mockSuccessLock = PowerMock.createNiceMock(Object.class);
		final KeyBoard mockKeyBoard = PowerMock.createNiceMock(KeyBoard.class);
		final InputHandler mockInputHandler = PowerMock.createNiceMock(InputHandler.class);
		final ConnectionHandler mockConnectionHandler = PowerMock.createNiceMock(ConnectionHandler.class);
		final Connection mockConnection = PowerMock.createNiceMock(Connection.class);

		PowerMock.expectNew(ConnectionListener.class).andReturn(mockConnectionListener);
		mockConnectionListener.succesLock = mockSuccessLock;
		mockConnectionListener.startedSuccessfully = true;
		PowerMock.expectNew(Object.class).andReturn(mockSuccessLock);
		mockSuccessLock.wait();
		PowerMock.expectLastCall();
		mockConnectionListener.start();
		EasyMock.expectLastCall();

		PowerMock.expectNew(KeyBoard.class).andReturn(mockKeyBoard);
		PowerMock.expectNew(InputHandler.class, EasyMock.isA(KeyBoard.class)).andReturn(mockInputHandler);
		mockInputHandler.start();
		PowerMock.expectLastCall();

		PowerMock.expectNew(ConnectionHandler.class, EasyMock.isA(InputHandler.class), EasyMock.isA(StartUI.class),
				EasyMock.isA(Server.class)).andReturn(mockConnectionHandler);
		mockConnectionHandler.start();
		PowerMock.expectLastCall();

		mockConnectionListener.stopThread();
		PowerMock.expectLastCall();
		mockConnectionHandler.stopThread();
		PowerMock.expectLastCall();
		mockInputHandler.stopThread();
		PowerMock.expectLastCall();

		mockConnectionListener.join();
		PowerMock.expectLastCall();
		mockConnectionHandler.join();
		PowerMock.expectLastCall();
		mockInputHandler.join();
		PowerMock.expectLastCall();

		mockConnection.stopThread();
		PowerMock.expectLastCall();
		mockConnection.join();
		PowerMock.expectLastCall();

		PowerMock.replayAll();
		Server server = Server.getInstance(mockUiThread);
		Thread.sleep(1000L);

		server.addNewConnection(mockConnection);
		server.stopServer();
		Thread.sleep(1000L);
		EasyMock.verify(mockConnectionListener, mockUiThread, mockSuccessLock, mockKeyBoard, mockInputHandler,
				mockConnectionHandler);
	}
}
