package org.catrobat.wifihidserver;

import java.awt.AWTException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;

public class Server {

	public final static int versionId = 3;

	private static Server instance = null;
	private ConnectionListener connectionListener;
	private InputHandler inputHandler;
	private ConnectionHandler connectionHandler;
	private static StartUI uiThread;
	private KeyBoard keyboard;
	private ArrayList<Connection> connectionList;
	private static boolean initializedSuccessfully;
	private ResourceBundle bundle;

	private Server() {
		bundle = ResourceBundle.getBundle(StartUI.bundleBaseName);
		initializedSuccessfully = true;
		System.out.println("Server start.");
		connectionList = new ArrayList<Connection>();
		startThreads();
	}

	public static Server getInstance(StartUI ui) {
		uiThread = ui;
		if (instance == null) {
			instance = new Server();
		}
		if (!initializedSuccessfully) {
			instance = null;
		}
		return instance;
	}

	public void startThreads() {
		connectionListener = new ConnectionListener();
		connectionListener.start();
		try {
			synchronized (connectionListener.succesLock) {
				connectionListener.succesLock.wait();
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		if (!connectionListener.startedSuccessfully) {
			uiThread.errorDialog(bundle
					.getString("errorMessageSocketAlreadyInUse"));
			initializedSuccessfully = false;
			return;
		}
		try {
			keyboard = new KeyBoard();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		if (keyboard != null) {
			inputHandler = new InputHandler(keyboard);
		}
		inputHandler.start();
		connectionHandler = new ConnectionHandler(inputHandler, uiThread, this);
		connectionHandler.start();
	}

	public void stopServer() {
		connectionListener.stopThread();
		connectionHandler.stopThread();
		inputHandler.stopThread();
		killConnectionThreads();
		try {
			connectionListener.join();
			connectionHandler.join();
			inputHandler.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		instance = null;
		System.out.println("Server finished.");
	}

	public void killConnectionThreads() {
		if (connectionList.size() != 0) {
			Iterator<Connection> it = connectionList.iterator();
			Connection connection = null;
			while (it.hasNext()) {
				connection = it.next();
				connection.stopThread();
				try {
					connection.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void addNewConnection(Connection connection) {
		connectionList.add(connection);
	}

	public void removeConnection(Connection connection) {
		connectionList.remove(connection);
	}
}
