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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Server.class, Connection.class})
public class ConnectionTest {
	
	private SocketAddress socketAddress;
	private ConnectionListener connectionListener;
	private static final String testIp = "www.google.at";
	private ObjectInputStream is;
	
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
	
	private byte[] receivedObject;
	@Test
	public void testConnectionHandlerWithLegalVersion() throws Exception {
		final Server mockServer = EasyMock.createNiceMock(Server.class);
		final InputHandler mockInputHandler = EasyMock.createNiceMock(InputHandler.class);
		final StartUI mockUiThread = EasyMock.createNiceMock(StartUI.class);
		final ServerSocket mockServerSocket = EasyMock.createNiceMock(ServerSocket.class);
		final Socket mockSocket = EasyMock.createNiceMock(Socket.class);
		final Connection mockConnection = EasyMock.createNiceMock(Connection.class);
		
		Object[] objectsToSend = new Object[2];
		objectsToSend[0] = Server.versionId;
		objectsToSend[1] = "Test Device - Standard Project";
		EasyMock.expect(mockSocket.getInputStream()).andReturn(serialize(objectsToSend));
		receivedObject = new byte[1000];
		EasyMock.expect(mockSocket.getOutputStream()).andReturn(new OutputStream() {

			private int byteCount = 0;

			@Override
			public void write(int b) throws IOException {
				receivedObject[byteCount] = (byte) b;
				byteCount++;
			}
		});
		EasyMock.replay(mockSocket, mockServer);
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
		EasyMock.replay(mockInputHandler, mockUiThread, mockServerSocket, mockConnection);
		connectionHandler.start();
		Thread.sleep(1000L);

		ByteArrayInputStream in = new ByteArrayInputStream(receivedObject);
		if (is == null) {
			is = new ObjectInputStream(in);
		}
		
		Object startRegistration = deserialize(is);
		Object confirmation = deserialize(is);
		
		assertTrue("Invalid incoming object to start syncronisation.", startRegistration instanceof Integer);
		assertTrue("Invalid incoming param to start syncronisation.", (Integer) startRegistration == 1);
		assertTrue("Invalid input, not of type Confirmation.", confirmation instanceof Confirmation);
		assertTrue("Invalid ConfirmationState. Not of type LEGAL_VERSION_ID", ((Confirmation) confirmation).
				getConfirmationState().equals(ConfirmationState.LEGAL_VERSION_ID) );
		EasyMock.verify(mockServer, mockInputHandler, mockServerSocket, mockSocket, mockConnection);
		receivedObject = null;
	}

	@Test
	public void testConnectionHandlerWithIllegalVersion() throws Exception {
		final Server mockServer = EasyMock.createNiceMock(Server.class);
		final InputHandler mockInputHandler = EasyMock.createNiceMock(InputHandler.class);
		final StartUI mockUiThread = EasyMock.createNiceMock(StartUI.class);
		final ServerSocket mockServerSocket = EasyMock.createNiceMock(ServerSocket.class);
		final Socket mockSocket = EasyMock.createNiceMock(Socket.class);
		final Connection mockConnection = EasyMock.createNiceMock(Connection.class);
		
		Object[] objectsToSend = new Object[1];
		objectsToSend[0] = Server.versionId + 1;
		EasyMock.expect(mockSocket.getInputStream()).andReturn(serialize(objectsToSend));
		receivedObject = new byte[1000];
		EasyMock.expect(mockSocket.getOutputStream()).andReturn(new OutputStream() {

			private int byteCount = 0;

			@Override
			public void write(int b) throws IOException {
				receivedObject[byteCount] = (byte) b;
				byteCount++;
			}
		});
		EasyMock.replay(mockSocket, mockServer);
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

		mockUiThread.errorDialogWithLink(EasyMock.isA(String[].class));
		EasyMock.expectLastCall();
		EasyMock.replay(mockInputHandler, mockUiThread, mockServerSocket, mockConnection);
		connectionHandler.start();
		Thread.sleep(1000L);
		
		ByteArrayInputStream in = new ByteArrayInputStream(receivedObject);
		if (is == null) {
			is = new ObjectInputStream(in);
		}
		Object startRegistration = deserialize(is);
		Object confirmation = deserialize(is);
		
		assertTrue("Invalid incoming object to start syncronisation.", startRegistration instanceof Integer);
		assertTrue("Invalid incoming param to start syncronisation.", (Integer) startRegistration == 1);
		assertTrue("Invalid input, not of type Confirmation.", confirmation instanceof Confirmation);
		assertTrue("Invalid ConfirmationState. Not of type ILLEGAL_VERSION_ID", ((Confirmation) confirmation).
				getConfirmationState().equals(ConfirmationState.ILLEGAL_VERSION_ID) );
		EasyMock.verify(mockServer, mockUiThread, mockInputHandler, mockServerSocket, mockSocket, mockConnection);
		receivedObject = null;
	}
	
	@Test
	public void testConnectionLegal() throws Exception {
		final Server mockServer = EasyMock.createNiceMock(Server.class);
		final InputHandler mockInputHandler = EasyMock.createNiceMock(InputHandler.class);
		final StartUI mockUiThread = EasyMock.createNiceMock(StartUI.class);
		final Socket mockSocket = EasyMock.createNiceMock(Socket.class);
		final ConnectionHandler mockConnectionHandler = EasyMock.createNiceMock(ConnectionHandler.class);
		final ObjectInputStream mockObjectInputStream = PowerMock.createMock(ObjectInputStream.class);
		final ObjectOutputStream mockObjectOutputStream = EasyMock.createNiceMock(ObjectOutputStream.class);
		final Command command = new Command('a', commandType.SINGLE_KEY);
		final KeyBoard mockKeyboard = EasyMock.createNiceMock(KeyBoard.class);
		final SocketAddress socketAddress = new InetSocketAddress(testIp, 64000);
		
		EasyMock.expect(mockSocket.getRemoteSocketAddress()).andReturn(socketAddress).anyTimes();
		EasyMock.replay(mockSocket);
		mockInputHandler.onIncoming(EasyMock.isA(Command.class), EasyMock.isA(Connection.class));
		EasyMock.expectLastCall().andDelegateTo(new InputHandler(mockKeyboard) {
			@Override
			public void onIncoming(Command command_, Connection connection) {
				assertTrue("Connection: Illegal input (not of type command).", command_ instanceof Command);
			}
		});		
		EasyMock.replay(mockUiThread);
		Connection connection = new Connection(mockSocket, mockInputHandler, mockUiThread, 3, mockConnectionHandler,
				mockServer, mockObjectInputStream, mockObjectOutputStream) {

			@Override
			public void writeToClient(Confirmation confirmation) throws IOException {
				assertTrue("Confirmation state was not of Type COMMAND_SEND_SUCCESSFULL.", confirmation.getConfirmationState().
						equals(ConfirmationState.COMMAND_SEND_SUCCESSFULL));
			}
		};
		EasyMock.expect(mockObjectInputStream.readObject()).andReturn(command).anyTimes();
		
		mockObjectInputStream.close();
		EasyMock.expectLastCall();
		mockObjectOutputStream.close();
		EasyMock.expectLastCall();
		mockSocket.close();
		EasyMock.expectLastCall();
		EasyMock.replay(mockServer, mockInputHandler, mockConnectionHandler);
		PowerMock.replay(mockObjectInputStream);
		
		connection.start();
		Thread.sleep(1000L);
		connection.stopThread();
		PowerMock.verify(mockObjectInputStream);
		EasyMock.verify(mockSocket, mockServer, mockInputHandler, mockConnectionHandler, mockObjectInputStream);
	}
	
	@Test
	public void testConnectionIlegal() throws Exception {
		final Server mockServer = EasyMock.createNiceMock(Server.class);
		final InputHandler mockInputHandler = EasyMock.createNiceMock(InputHandler.class);
		final StartUI mockUiThread = EasyMock.createNiceMock(StartUI.class);
		final Socket mockSocket = EasyMock.createNiceMock(Socket.class);
		final ConnectionHandler mockConnectionHandler = EasyMock.createNiceMock(ConnectionHandler.class);
		final ObjectInputStream mockObjectInputStream = PowerMock.createMock(ObjectInputStream.class);
		final ObjectOutputStream mockObjectOutputStream = EasyMock.createNiceMock(ObjectOutputStream.class);
		// command of wrong class type
		final Confirmation command = new Confirmation(ConfirmationState.ILLEGAL_VERSION_ID);
		final SocketAddress socketAddress = new InetSocketAddress(testIp, 64000);
		
		EasyMock.expect(mockSocket.getRemoteSocketAddress()).andReturn(socketAddress).anyTimes();
		EasyMock.replay(mockSocket);
		mockInputHandler.onIncoming(EasyMock.isA(Command.class), EasyMock.isA(Connection.class));
		EasyMock.replay(mockUiThread);
		Connection connection = new Connection(mockSocket, mockInputHandler, mockUiThread, 3, mockConnectionHandler,
				mockServer, mockObjectInputStream, mockObjectOutputStream) {

			@Override
			public void writeToClient(Confirmation confirmation) throws IOException {
				assertTrue("Confirmation state was not of Type ILLEGAL_CLASS.", confirmation.getConfirmationState().
						equals(ConfirmationState.ILLEGAL_CLASS));
			}
		};
		EasyMock.expect(mockObjectInputStream.readObject()).andReturn(command).anyTimes();
		
		mockObjectInputStream.close();
		EasyMock.expectLastCall();
		mockObjectOutputStream.close();
		EasyMock.expectLastCall();
		mockSocket.close();
		EasyMock.expectLastCall();
		EasyMock.replay(mockServer, mockConnectionHandler);
		PowerMock.replay(mockObjectInputStream);
		
		connection.start();
		Thread.sleep(1000L);
		PowerMock.verify(mockObjectInputStream);
		EasyMock.verify(mockSocket, mockServer, mockConnectionHandler, mockObjectInputStream);
	}
	
	private InputStream serialize(Object[] object) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int size = object.length;
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
		for(int i = 0; i < size; i++) {
			objectOutputStream.writeObject(object[i]);
		}	    
	    objectOutputStream.flush();
	    objectOutputStream.close();
	    InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
	    return inputStream;
	}
	
	private Object deserialize(Object data) throws IOException, ClassNotFoundException {
	    
		if (data instanceof byte[]) {
	    	ByteArrayInputStream in = new ByteArrayInputStream((byte[]) data);
	    	if (is == null) {
				is = new ObjectInputStream(in);
			}
	    } else {
	    	is = (ObjectInputStream) data;
	    }
	    return is.readObject();
	}
	
	private final int key = 28;
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

		keyCombination[0] = 13;
		keyCombination[1] = 20;
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

		keyCombination[0] = 13;
		keyCombination[1] = 28;
		EasyMock.expect(mockCommandSingleKey.getKey()).andReturn(key).anyTimes();
		EasyMock.expect(mockCommandKeyCombination.getKeyComb()).andReturn(keyCombination).anyTimes();
		EasyMock.expect(mockKeyboard.setKeyToHandle(EasyMock.anyInt())).andReturn(false);
		EasyMock.expect(mockKeyboard.setKeyToHandle(keyCombination)).andReturn(false);
		mockConnection.confirm(ConfirmationState.ILLEGAL_COMMAND);
		EasyMock.expectLastCall().times(2);
		EasyMock.replay(mockCommandSingleKey, mockConnection, mockCommandKeyCombination, mockKeyboard);

		InputHandler inputHandler = new InputHandler(mockKeyboard);
		inputHandler.start();
		Thread.sleep(500L);
		inputHandler.onIncoming(mockCommandSingleKey, mockConnection);
		inputHandler.onIncoming(mockCommandKeyCombination, mockConnection);

		Thread.sleep(2000L);
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
