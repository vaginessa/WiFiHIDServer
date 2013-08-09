package org.catrobat.wifihidserver;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.JFrame;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;

import org.catrobat.wifihidserver.Connection.ConnectionHandling;
import org.catrobat.wifihidserver.ConnectionListener.errorOnSystem;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JComboBox;

public class StartUI implements ConnectionHandling, errorOnSystem {
	private JFrame frame;
	private Server server;
	private boolean serverStarted;
	private boolean serverStartable;
	private JButton buttonStart;
	private JTextPane textPanePort;
	private JTextPane textPaneIp;
	private ArrayList<Connection> connectionList;
	private JComboBox comboBoxConnections;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					StartUI window = new StartUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public StartUI() {
		initialize();
		centerFrame();
	}

	public void initialize() {
		serverStarted = false;
		serverStartable = true;
		connectionList = new ArrayList<Connection>();
		server = null;
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				stopServer();
			}
		});
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setResizable(false);

		buttonStart = new JButton("Start");
		buttonStart.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				startServer();
			}
		});
		buttonStart.setBounds(163, 52, 117, 25);
		frame.getContentPane().add(buttonStart);

		JTextPane txtpnPort = new JTextPane();
		txtpnPort.setBackground(UIManager.getColor("Button.background"));
		txtpnPort.setText("Port:");
		txtpnPort.setBounds(221, 161, 39, 21);
		frame.getContentPane().add(txtpnPort);

		JTextPane txtpnIpadresse = new JTextPane();
		txtpnIpadresse.setBackground(UIManager.getColor("Button.background"));
		txtpnIpadresse.setText("IP-Adresse:");
		txtpnIpadresse.setBounds(221, 194, 82, 21);
		frame.getContentPane().add(txtpnIpadresse);

		JTextPane txtpnDesktopServer = new JTextPane();
		txtpnDesktopServer.setFont(new Font("Dialog", Font.BOLD, 22));
		txtpnDesktopServer.setBackground(UIManager
				.getColor("Button.background"));
		txtpnDesktopServer.setText("Desktop Server");
		txtpnDesktopServer.setBounds(121, 0, 288, 30);
		frame.getContentPane().add(txtpnDesktopServer);

		textPaneIp = new JTextPane();
		textPaneIp.setEditable(false);
		textPaneIp
				.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		textPaneIp.setBounds(315, 194, 117, 21);
		frame.getContentPane().add(textPaneIp);

		textPanePort = new JTextPane();
		textPanePort.setEditable(false);
		textPanePort.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null,
				null));
		textPanePort.setBounds(315, 161, 68, 21);
		frame.getContentPane().add(textPanePort);

		comboBoxConnections = new JComboBox();
		comboBoxConnections.setBounds(44, 116, 159, 24);
		comboBoxConnections.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				Connection connection = findConnectionByName((String) comboBoxConnections
						.getSelectedItem());
				refreshConnectionList(connection);
			}
		});
		frame.getContentPane().add(comboBoxConnections);
	}

	public void centerFrame() {
		Dimension frameSize = frame.getSize();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int top = (screenSize.height - frameSize.height) / 2;
		int left = (screenSize.width - frameSize.width) / 2;
		frame.setLocation(left, top);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public void errorDialog(String message) {
		JOptionPane.showMessageDialog(frame, message);
	}

	public void startServer() {
		if (serverStarted) {
			stopServer();
		} else if (!serverStarted && serverStartable) {
			server = Server.getInstance(this);
			if (server == null) {
				return;
			}
			serverStarted = true;
			serverStartable = false;
			buttonStart.setText("Stop");
		}
	}

	public void stopServer() {
		if (server != null) {
			server.stopServer();
			server = null;
			serverStarted = false;
			serverStartable = true;
			buttonStart.setText("Start");
		}
		for (int i = 0; i < comboBoxConnections.getItemCount();) {
			comboBoxConnections.removeItemAt(i);
		}
		frame.getContentPane().add(comboBoxConnections);
	}

	public void addNewConnection(Connection connection) {
		connectionList.add(connection);
		if (connection != null) {
			comboBoxConnections.addItem((String) connection.getConnectionName());
			frame.getContentPane().add(comboBoxConnections);
		}
	}

	public void removeConnection(Connection connection) {
		if (connection != null) {
			connectionList.remove(connection);
			comboBoxConnections.removeItem((String) connection.getConnectionName());
			frame.getContentPane().add(comboBoxConnections);
		}
	}

	public void refreshConnectionList(Connection connection) {
		if (connection != null) {
			textPaneIp.setText(connection.getIp());
			textPanePort.setText(connection.getPort());
		} else {
			textPaneIp.setText("");
			textPanePort.setText("");
		}
	}

	public Connection findConnectionByName(String name) {
		Connection connection = null;
		if (connectionList != null) {
			Iterator<Connection> it = connectionList.iterator();
			while (it.hasNext()) {
				connection = it.next();
				if (connection.getConnectionName().equals(name))
					return connection;
			}
		}
		return null;
	}
}
