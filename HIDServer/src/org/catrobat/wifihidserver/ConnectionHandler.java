package org.catrobat.wifihidserver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.catrobat.wifihidserver.Connection.UserHandling;


public class ConnectionHandler extends Thread implements UserHandling{
	private Thread thisThread;
	private InputHandler inputHandler;
	private StartUI uiThread;
	private Server server;
	private java.net.ServerSocket serverSocket;
	private ArrayList<Connection>connectionList;
	private static final int port = 63000;
	
	public ConnectionHandler(InputHandler inputHandler_, StartUI ui, Server server_){
		inputHandler = inputHandler_;
		uiThread = ui;
		connectionList = new ArrayList<Connection>();
		server = server_;
		this.setName("ConnectionHandler");
	}
	
	public void run(){
		initialize();
		int connectionCount = 0;
	    java.net.Socket client;
	    while (thisThread == this) {
	    	try {
	    		if (serverSocket != null) {
	    			client = serverSocket.accept();
	    			connectionCount++;
		    		Connection newUser = new Connection(client, inputHandler, uiThread, connectionCount, this, server);
		    		if (newUser != null) {
		    			newUser.start();		    			
		    		}	    			
	    		} else {
	    			continue;
	    		}
			} catch (IOException e) {
				continue;
			}
	    }
	}
	
	public void initialize() {
		thisThread = this;
		serverSocket = null;
	    serverSocket = null;
	    try {
			serverSocket = new java.net.ServerSocket(port);
    		assert this.serverSocket.isBound();
			serverSocket.setSoTimeout(1000);
		} catch (IOException e1) {
			e1.printStackTrace();
		}		    
	}
	
	public void stopThread(){
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}			
		thisThread = null;
	}
	
	public void addNewUser(Connection newUser){
		Iterator<Connection> it = connectionList.iterator();
		Connection connection = null;
        while (it.hasNext()) {
        	connection = it.next();
            if (connection.getIp().equals(newUser.getIp())) {
            	return;            	
            }
        }
        connectionList.add(newUser);
	}
	
	public void removeUser(Connection connection){
		connectionList.remove(connection);
	}
}
