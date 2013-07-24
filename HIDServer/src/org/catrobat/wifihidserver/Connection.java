package org.catrobat.wifihidserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

import org.catrobat.catroid.io.Command;


public class Connection extends Thread{
	private Socket client;
	private BufferedReader bufferedReader;
	private String remote_ip;
	private String remote_port;
	private InputHandler input_handler;
	private ConnectionHandler connect_handler;
	private Server server;
	private StartUI ui_thread;
	private String user_name;
	private Thread this_thread;
	private ObjectInputStream object_input;
	private boolean is_new_user;
	
	public Connection(Socket client_, InputHandler inhan, StartUI ui, int user_number,
			ConnectionHandler connect, Server server_){
		client = client_;
		input_handler = inhan;
		connect_handler = connect;
		server = server_;
		ui_thread = ui;
		user_name = "user " + Integer.toString(user_number);
		this_thread = this;
		is_new_user = true;
		InputStream input = null;
		try {
			input = client.getInputStream();
			object_input = new ObjectInputStream(input);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		this.setName(user_name);
	}
	
	public void run(){
		connect_handler.addNewUser(this);
		server.addNewUser(this);
		while(this_thread == this){
			getInput();
		}		
	}
	
	public void getInput(){
	    int length = 0;
	    Command command = null;
	    try {
			command = (Command)object_input.readObject();
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println("Inputstream is closed.");
			stopThread();
			return;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			stopThread();
			return;
		} 
	    if(length == -1){
			stopThread();
			return;
		}
	    if(is_new_user) {
	    	ui_thread.addNewUser(this);
	    	is_new_user = false;
	    }
		splitIpPort(client.getRemoteSocketAddress().toString());
		input_handler.onIncoming(command, this);
	}
	
	public void splitIpPort(String ip_with_port){
		int pos = ip_with_port.indexOf(":");
		if(pos != -1){
			remote_ip = ip_with_port.substring(1, pos);
			remote_port = ip_with_port.substring(pos + 1);
		}
	}
	
	public String getIp(){
		return remote_ip;
	}
	
	public String getPort(){
		return remote_port;
	}
	
	public String getUserName(){
		return user_name;
	}
	
	public void stopThread(){
		connect_handler.removeUser(this);
		ui_thread.removeUser(this);
		this_thread = null;
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
