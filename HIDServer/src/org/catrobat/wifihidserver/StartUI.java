package org.catrobat.wifihidserver;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JFrame;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EtchedBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.catrobat.wifihidserver.ConnectionListener.errorOnSystem;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

public class StartUI implements errorOnSystem {
	private JFrame frame;
	private Server server;
	private boolean serverStarted;
	private boolean serverStartable;
	private JButton buttonStart;
	private JTextPane textPanePort;
	private JTextPane textPaneIp;
	private JTextPane textClientName;
	private  final String downloadLink = "https://pocketcode.org";
	private boolean errorDialogOnScreen = false;
	private ResourceBundle bundle;
	
	public final static String bundleBaseName = "wifihidserver";

	public static void main(String[] args) {
		try {
	        UIManager.setLookAndFeel(
	            UIManager.getSystemLookAndFeelClassName());
	    } 
	    catch (UnsupportedLookAndFeelException e) {
	    }
	    catch (ClassNotFoundException e) {
	    }
	    catch (InstantiationException e) {
	    }
	    catch (IllegalAccessException e) {
	    }
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
		bundle = ResourceBundle.getBundle(bundleBaseName);
		serverStarted = false;
		serverStartable = true;
		server = null;
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				stopServer();
			}
		});
		frame.setBounds(100, 100, 357, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setResizable(false);

		buttonStart = new JButton("") {
			private static final long serialVersionUID = 1L;

			@Override
			public void setText(String arg0) {
			    super.setText(arg0);
			    if(arg0.length() > 0 ) {
				    FontMetrics metrics = getFontMetrics(getFont()); 
				    int width = metrics.stringWidth( getText() );
				    int height = metrics.getHeight();
				    Dimension newDimension =  new Dimension(width + 40, height + 35);
				    setPreferredSize(newDimension);
				    setBounds(new Rectangle(getLocation(), getPreferredSize()));
				    buttonStart.setAlignmentX(Component.CENTER_ALIGNMENT);
				    centerComponent(buttonStart);
			    }
			}
		};
		buttonStart.setBackground(UIManager.getColor("Button.background"));
		buttonStart.setFont(new Font("Dialog", Font.BOLD, 25));
		buttonStart.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				startServer();
			}
		});
		buttonStart.setBounds(110, 67, 131, 64);
		buttonStart.setText(bundle.getString("buttonStart"));
		
		frame.getContentPane().add(buttonStart);

		JTextPane txtpnPort = new JTextPane();
		txtpnPort.setEditable(false);
		txtpnPort.setBackground(UIManager.getColor("Button.background"));
		txtpnPort.setText(bundle.getString("textPanePort"));
		txtpnPort.setBounds(57, 195, 39, 21);
		frame.getContentPane().add(txtpnPort);

		JTextPane txtpnIpadresse = new JTextPane();
		txtpnIpadresse.setEditable(false);
		txtpnIpadresse.setBackground(UIManager.getColor("Button.background"));
		txtpnIpadresse.setText(bundle.getString("textPaneIpAddress"));
		txtpnIpadresse.setBounds(57, 228, 82, 21);
		frame.getContentPane().add(txtpnIpadresse);

		JTextPane textpaneDesktopServer = new JTextPane();
		textpaneDesktopServer.setEditable(false);
		textpaneDesktopServer.setFont(new Font("Dialog", Font.BOLD, 22));
		textpaneDesktopServer.setBackground(UIManager.getColor("Button.background"));
		textpaneDesktopServer.setText(bundle.getString("pocketServer"));
		StyledDocument doc = textpaneDesktopServer.getStyledDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
		doc.setParagraphAttributes(0, doc.getLength(), center, false);		
		textpaneDesktopServer.setBounds(76, 12, 204, 30);
		centerComponent(textpaneDesktopServer);
		frame.getContentPane().add(textpaneDesktopServer);

		textPaneIp = new JTextPane();
		textPaneIp.setEditable(false);
		textPaneIp.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		textPaneIp.setBounds(160, 228, 117, 21);
		frame.getContentPane().add(textPaneIp);

		textPanePort = new JTextPane();
		textPanePort.setEditable(false);
		textPanePort.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		textPanePort.setBounds(160, 195, 68, 21);
		frame.getContentPane().add(textPanePort);
		
		textClientName = new JTextPane();
		textClientName.setEditable(false);
		textClientName.setFont(new Font("Dialog", Font.BOLD, 17));
		textClientName.setBackground(UIManager.getColor("Button.background"));
		textClientName.setBounds(36, 148, 274, 35);
		
		textClientName.setText(bundle.getString("clickStart"));
		doc = textClientName.getStyledDocument();
		center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
		doc.setParagraphAttributes(0, doc.getLength(), center, false);
		centerComponent(textClientName);
		frame.getContentPane().add(textClientName);
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
	
	public void centerComponent(Component component) {
		Dimension frameSize = frame.getSize();
		int left = (frameSize.width - component.getWidth()) / 2 ;
		component.setLocation(left, component.getLocation().y);
	}

	public void errorDialog(String message) {
		int dialogCanceled = JOptionPane.YES_NO_CANCEL_OPTION;
		if (errorDialogOnScreen) {
			return;
		}		
		errorDialogOnScreen = true;
		JOptionPane.showMessageDialog(frame, message);
		if(dialogCanceled == JOptionPane.NO_OPTION) {
			System.out.println("now canceled");
			errorDialogOnScreen = false;
        }
	}
	
	public void errorDialogWithLink(String[] message) {
		int dialogCanceled = JOptionPane.YES_NO_CANCEL_OPTION;
		if (errorDialogOnScreen) {
			return;
		}
		JEditorPane ep = new JEditorPane("text/html", "<html><body style=\"" + "Google" + "\">" 
	            + message[0] + "<a href>" + "\n" + message[1] + "</a>" 
	            + message[2] + "</body></html>");
		
		ep.addHyperlinkListener(new HyperlinkListener() {
			
	        public void hyperlinkUpdate(HyperlinkEvent e) {
	            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
		            	Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
			        	if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			                try {
			                	java.net.URI uri = new java.net.URI(downloadLink);
			                	Desktop.getDesktop().browse(uri);
			                } catch (Exception e1) {
			                    e1.printStackTrace();
			                }
			            }
		            }
	            }
        });
	    ep.setEditable(false);
	    JLabel label = new JLabel();
	    ep.setBackground(label.getBackground());
	    errorDialogOnScreen = true;
	    JOptionPane.showMessageDialog(null, ep);
		if(dialogCanceled == JOptionPane.NO_OPTION) {
			errorDialogOnScreen = false;
        }
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
			buttonStart.setText(bundle.getString("stop"));
			textClientName.setText("");
		}
	}
	
	public void setIpAndPort(Connection connection) {
		if (connection != null) {
			textPaneIp.setText(connection.getIp());
			textPanePort.setText(connection.getPort());
		} else {
			textPaneIp.setText("");
			textPanePort.setText("");
			textClientName.setText(bundle.getString("clickStart"));
		}
	}
	
	public void setClientAndProjectName(String clientAndProjectName) {
		textClientName.setText(clientAndProjectName);
		textClientName.setVisible(true);
	}

	public void stopServer() {
		if (server != null) {
			server.stopServer();
			server = null;
			serverStarted = false;
			serverStartable = true;
			buttonStart.setText(bundle.getString("start"));
		}
		textClientName.setText(bundle.getString("clickStart"));
	}
}
