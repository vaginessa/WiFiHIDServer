package org.catrobat.wifihidserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.catrobat.catroid.io.Command;
import org.catrobat.catroid.io.Confirmation;
import org.catrobat.catroid.io.Confirmation.ConfirmationState;


public class Connection extends Thread{
	private Socket client;
	private BufferedReader bufferedReader;
	private String remoteIp;
	private String remotePort;
	private InputHandler inputHandler;
	private ConnectionHandler connectHandler;
	private Server server;
	private StartUI uiThread;
	private String connectionName;
	private Thread thisThread;
	private ObjectInputStream objectInput;
	private ObjectOutputStream objectOutput;
	
	public Connection(Socket client, InputHandler inhan, StartUI ui, int connectionNumber,
			ConnectionHandler connect, Server server, ObjectInputStream objectInput,
				ObjectOutputStream objectOutput) {
		this.client = client;
		inputHandler = inhan;
		connectHandler = connect;
		this.server = server;
		this.objectInput = objectInput;
		this.objectOutput = objectOutput;
		uiThread = ui;
		connectionName = "connection " + Integer.toString(connectionNumber);
		thisThread = this;
		this.setName(connectionName);
		splitIpPort(client.getRemoteSocketAddress().toString());
		uiThread.addNewConnection(this);
	}
	
	public void run(){
		connectHandler.addNewConnection(this);
		server.addNewConnection(this);
		while(thisThread == this){
			getInput();
		}		
	}
	
	public void getInput(){
		int length = 0;
	    Command command = null;
	    try {
	    	if (objectInput != null) {
	    		command = (Command)objectInput.readObject();
	    	}
		} catch (IOException e) {
			System.out.println("Connection to " + remoteIp + " broke. Inputstream is closed.");
			stopThread();
			return;
		} catch (ClassNotFoundException e) {
			confirm(ConfirmationState.ILLEGAL_CLASS);
			stopThread();
			return;
		} catch (ClassCastException e) {
			confirm(ConfirmationState.ILLEGAL_CLASS);
			stopThread();
			return;
		}
	    if(length == -1){
			stopThread();
			return;
		}
		inputHandler.onIncoming(command, this);
	}
	
	public void confirm(ConfirmationState state) {
		Confirmation confirmation = new Confirmation(state);
		try {
			writeToClient(confirmation);
		} catch (IOException e1) {
			System.out.println("Connection to " + remoteIp + " broke.");
			stopThread();
		}	
	}
	
	public void writeToClient(Confirmation confirmation) throws IOException {
		objectOutput.writeObject(confirmation);
	}
	
	public void splitIpPort(String ipWithPort){
		int pos = ipWithPort.indexOf(":");
		if(pos != -1){
			remoteIp = ipWithPort.substring(1, pos);
			remotePort = ipWithPort.substring(pos + 1);
		}
	}
	
	public String getIp(){
		return remoteIp;
	}
	
	public String getPort(){
		return remotePort;
	}
	
	public String getConnectionName(){
		return connectionName;
	}
	
	public void stopThread(){
		connectHandler.removeConnection(this);
		uiThread.removeConnection(this);
		thisThread = null;
		try {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
			if (objectInput != null) {
				objectInput.close();
			}
			if (objectOutput != null) {
				objectOutput.close();
			}
			if (client != null) {
				client.close();
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public interface Instruction{
		abstract void onIncoming(Command input, Connection connection);
		
	}
	
	public interface ConnectionHandling{
		public void addNewConnection(Connection newConnection);
		public void removeConnection(Connection conection);
	}
}
