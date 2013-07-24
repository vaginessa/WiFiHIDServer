package org.catrobat.wifihidserver;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ConnectionListener extends Thread {
	private Thread thisThread;
	private DatagramPacket dataPacket;
	public Object succesLock;
	private final int port = 64000;	
	private static DatagramSocket dataSocket;
	public boolean startedSuccessfully;
	
	public ConnectionListener() {
		this.setName("ConnectionListener");
		succesLock = new Object();
		startedSuccessfully = false;
	}
	
	public void run() {
		thisThread = currentThread();
		while (thisThread == this) {	
			initialize();
			if (startedSuccessfully) {
				listen();				
			} else {
				break;
			}
		}
	}
	
	public void initialize() {
		synchronized (succesLock) {
			byte[] message = new byte[1];
			dataPacket = new DatagramPacket(message, message.length);
			dataSocket = null;
			try {
				dataSocket = new DatagramSocket(port);
				dataSocket.setSoTimeout(1000);
				startedSuccessfully = true;
			} catch (SocketException e) {
				stopThread();
			}
			succesLock.notifyAll();
		}
	}
	
	public void listen() {
		while (thisThread == this) {
			try {
				if (dataSocket != null) {
					dataSocket.receive(dataPacket);
					response();
					break;						
				} else {
					continue;
				}
			} catch (IOException ioExc) {
				if (thisThread != this) {
					continue;
				}
				try {
					sleep(1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void response() {
		byte[] ip = null;
		try {
			ip = InetAddress.getLocalHost().getHostName().getBytes();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}		
		DatagramPacket dataSend = null;
		try {
			dataSend = new DatagramPacket(ip, ip.length, dataPacket.getSocketAddress());
		} catch (SocketException e1) {
			e1.printStackTrace();
		}		
		try {
			dataSocket.send(dataSend);
		} catch (IOException e) {
			e.printStackTrace();
		}		
		dataSocket.close();		
	}
	
	public void stopThread(){
		thisThread = null;
		if (dataSocket != null) {
			dataSocket.close();
		}		
	}
	
	public interface errorOnSystem{
		public void errorDialog(String message);
	}
}
