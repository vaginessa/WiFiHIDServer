package org.catrobat.wifihidserver;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import org.catrobat.catroid.io.Confirmation;
import org.catrobat.catroid.io.Confirmation.ConfirmationState;
import org.catrobat.wifihidserver.Connection.ConnectionHandling;


public class ConnectionHandler extends Thread implements ConnectionHandling{
	private Thread thisThread;
	private InputHandler inputHandler;
	private StartUI uiThread;
	private Server server;
	private java.net.ServerSocket serverSocket;
	private ArrayList<Connection>connectionList;
	private static final int port = 63000;
	private ObjectInputStream objectInput;
	private ObjectOutputStream objectOutput;
	
	private final String errorMessage1 = "<br>Die installierte Version von ... ist nicht mehr kompatibel.<br>\n" +
			"Die neue Version kann ";
	private final String errorMessage2 = "hier";
	private final String errorMessage3 = " heruntergeladen werden.";
	
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
	    			try {
						versionCheck(client);
					} catch (Exception e) {
						continue;
					}
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
		return new Connection(client, inputHandler, uiThread, connectionCount, this, server,
				objectInput, objectOutput);
	}

	public void versionCheck(Socket client) throws Exception{
		int versionId = 0;
		startSync(client);
		versionId = waitForClientRegistration();
		Confirmation confirmation;
		if(versionId == Server.versionId) {
			confirmation = new Confirmation(ConfirmationState.LEGAL_VERSION_ID);
			sendToClient(confirmation);
		} else {
			confirmation = new Confirmation(ConfirmationState.ILLEGAL_VERSION_ID);
			confirmation.setVersionId(Server.versionId);
			sendToClient(confirmation);
			
			Thread onUiThread;
			if (versionId > Server.versionId) {
				onUiThread = new Thread (new Runnable() {
				
					@Override
					public void run() {
						String[] message = new String[3];			
						message[0] = errorMessage1;
						message[1] = errorMessage2;
						message[2] = errorMessage3;
						uiThread.errorDialogWithLink(message);
					}
				});
			} else {
				onUiThread = new Thread (new Runnable() {
				
					@Override
					public void run() {
						String message = "Die momentane Catroid-Version ist zu alt.";
						uiThread.errorDialog(message);
					}
				});
			}			
			onUiThread.start();
			throw new Exception();
		}
	}
	
	public void startSync(Socket client) throws Exception{
		InputStream input = null;
		objectInput = null;
		OutputStream output =  null;
		objectOutput = null;
		try {
			input = client.getInputStream();
			objectInput = new ObjectInputStream(input);
			output = client.getOutputStream();
			objectOutput = new ObjectOutputStream(output);			
		} catch (IOException e1) {
			System.out.println("Output/InputStream couldn't be established.");
			throw e1;
		}
		int startRegistration = 1;
		sendToClient(startRegistration);		
	}
	
	public int waitForClientRegistration() throws IOException, ClassNotFoundException {
		int versionId = 0;
		versionId = (Integer) objectInput.readObject();
		return versionId;
	}
	
	public void sendToClient(Object object) throws IOException {
		try {
			objectOutput.writeObject(object);
		} catch (IOException e1) {
			System.out.println("Connection to client broke.");
			throw e1;
		}
	}
	
	public void closeStreams(ObjectInputStream input, ObjectOutputStream output) {
		try {
			if(input != null) {
				input.close();
			}
		} catch (IOException e) {
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
