package org.catrobat.wifihidserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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
	private boolean isNewConnection;
	
	public Connection(Socket client, InputHandler inhan, StartUI ui, int connectionNumber,
			ConnectionHandler connect, Server server){
		this.client = client;
		inputHandler = inhan;
		connectHandler = connect;
		this.server = server;
		uiThread = ui;
		connectionName = "connection " + Integer.toString(connectionNumber);
		thisThread = this;
		isNewConnection = true;
		InputStream input = null;
		try {
			input = client.getInputStream();
			if (input != null) {
				objectInput = new ObjectInputStream(input);
			}			
		} catch (IOException e1) {
			System.out.println("InputStream couldn't be established.");
			e1.printStackTrace();
			stopThread();
		}
		OutputStream output = null;
		try {
			output = client.getOutputStream();
			if (output != null) {
				objectOutput = new ObjectOutputStream(output);
			}			
		} catch (IOException e) {
			System.out.println("OutputStream couldn't be established.");
			stopThread();
		}
		this.setName(connectionName);
		splitIpPort(client.getRemoteSocketAddress().toString());
	    if(isNewConnection) {
	    	uiThread.addNewConnection(this);
	    	isNewConnection = false;
	    }
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
			objectOutput.writeObject(confirmation);
		} catch (IOException e1) {
			System.out.println("Connection to " + remoteIp + " broke.");
			stopThread();
		}	
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
