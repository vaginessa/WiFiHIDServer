package org.catrobat.wifihidserver;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import org.catrobat.wifihidserver.Connection.ConnectionHandling;


public class ConnectionHandler extends Thread implements ConnectionHandling{
	private Thread thisThread;
	private InputHandler inputHandler;
	private StartUI uiThread;
	private Server server;
	private java.net.ServerSocket serverSocket;
	private ArrayList<Connection>connectionList;
	private static final int port = 63000;
	
	public ConnectionHandler(InputHandler inputHandler, StartUI ui, Server server){
		this.inputHandler = inputHandler;
		uiThread = ui;
		connectionList = new ArrayList<Connection>();
		this.server = server;
		this.setName("ConnectionHandler");
	}
	
	public void run() {
		initialize();
		int connectionCount = 0;
	    Socket client;
	    while (thisThread == this) {
	    	try {
	    		if (serverSocket != null) {
	    			client = serverSocket.accept();
	    			connectionCount++;
	    			Connection newConnection;
	    			if(client != null) {
	    				newConnection = createNewConnection(client, connectionCount);	    				
	    			} else {
	    				continue;
	    			}
		    		if (newConnection != null) {
		    			newConnection.start();		    			
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
	    try {
			serverSocket = createNewServerSocket();
    		assert this.serverSocket.isBound();
			serverSocket.setSoTimeout(1000);
		} catch (IOException e1) {
			e1.printStackTrace();
		}		    
	}
	
	public java.net.ServerSocket createNewServerSocket() throws IOException {
		return new java.net.ServerSocket(port);
	}
	
	public Connection createNewConnection(Socket client, int connectionCount) {
		return new Connection(client, inputHandler, uiThread, connectionCount, this, server);
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
	
	public void addNewConnection(Connection newConnection){
		Iterator<Connection> it = connectionList.iterator();
		Connection connection = null;
        while (it.hasNext()) {
        	connection = it.next();
            if (connection.getIp().equals(newConnection.getIp())) {
            	return;            	
            }
        }
        connectionList.add(newConnection);
	}
	
	public void removeConnection(Connection connection){
		connectionList.remove(connection);
	}
}
