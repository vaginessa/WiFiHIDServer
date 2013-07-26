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
	private String userName;
	private Thread thisThread;
	private ObjectInputStream objectInput;
	private ObjectOutputStream objectOutput;
	private boolean isNewUser;
	
	public Connection(Socket client, InputHandler inhan, StartUI ui, int userNumber,
			ConnectionHandler connect, Server server){
		this.client = client;
		inputHandler = inhan;
		connectHandler = connect;
		this.server = server;
		uiThread = ui;
		userName = "user " + Integer.toString(userNumber);
		thisThread = this;
		isNewUser = true;
		InputStream input = null;
		try {
			input = client.getInputStream();
			objectInput = new ObjectInputStream(input);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		OutputStream output = null;
		try {
			output = client.getOutputStream();
			objectOutput = new ObjectOutputStream(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.setName(userName);
	}
	
	public void run(){
		connectHandler.addNewUser(this);
		server.addNewUser(this);
		while(thisThread == this){
			getInput();
		}		
	}
	
	public void getInput(){
	    int length = 0;
	    Command command = null;
	    try {
			command = (Command)objectInput.readObject();
		} catch (IOException e) {
			System.out.println("Connection to " + remoteIp + " broke. Inputstream is closed.");
			stopThread();
			return;
		} catch (ClassNotFoundException e) {
			confirm(ConfirmationState.ILLEGAL_CLASS);
			stopThread();
			return;
		}
	    if(length == -1){
			stopThread();
			return;
		}
	    if(isNewUser) {
	    	uiThread.addNewUser(this);
	    	isNewUser = false;
	    }
		splitIpPort(client.getRemoteSocketAddress().toString());
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
	
	public String getUserName(){
		return userName;
	}
	
	public void stopThread(){
		connectHandler.removeUser(this);
		uiThread.removeUser(this);
		thisThread = null;
		try {
			if(bufferedReader != null)
				bufferedReader.close();
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public interface Instruction{
		abstract void onIncoming(Command input, Connection user_thread_);
		
	}
	
	public interface UserHandling{
		public void addNewUser(Connection new_user);
		public void removeUser(Connection user);
	}
}
