package com.mike.chat.client;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class ClientWindow extends JFrame implements Runnable{
	private static final long serialVersionUID = 3019033558936551190L;

	private JTextField txtMessage;
	private JTextArea  txtrHistory;
	private JPanel     contentPane;

	private Client client;

	private Thread listen;
	private boolean running = false;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmOnlineUsers;

	private OnlineUsers users;


	public ClientWindow(String name, String host, int port) {
		setTitle("The Chat");
		client = new Client(name, host, port);
		boolean connected = client.isConnected(host);
		makingWindow();
		if (!connected) {
			System.err.println("Connection failed!");
			write("Unknown Host(e.g. localhost)");
		} 
		write("Successfully connected\n" + "Host: " + host + ":" + port + "\nName: " + name);
		String connection = "/c/" + name;
		client.send(connection.getBytes());
		users = new OnlineUsers();
		running = true;
		new Thread(this, "Running").start();
	}

	private void makingWindow() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(880, 550);
		setLocationRelativeTo(null);

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmOnlineUsers = new JMenuItem("Online Users");
		mntmOnlineUsers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				users.setVisible(true);
			}
		});
		mnFile.add(mntmOnlineUsers);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{28, 815, 30, 7};
		gbl_contentPane.rowHeights = new int[]{45, 465, 40};
		contentPane.setLayout(gbl_contentPane);

		txtrHistory = new JTextArea();
		txtrHistory.setForeground(Color.DARK_GRAY);
		txtrHistory.setEditable(false);

		JScrollPane scroll = new JScrollPane(txtrHistory);

		GridBagConstraints gbc_txtrHistory = new GridBagConstraints();
		gbc_txtrHistory.insets = new Insets(0, 0, 5, 5);
		gbc_txtrHistory.fill = GridBagConstraints.BOTH;
		gbc_txtrHistory.gridx = 0;
		gbc_txtrHistory.gridy = 0;
		gbc_txtrHistory.gridwidth = 3;
		gbc_txtrHistory.gridheight = 2;
		gbc_txtrHistory.weightx = 1;
		gbc_txtrHistory.weighty = 1;
		contentPane.add(scroll, gbc_txtrHistory);
		txtrHistory.setFont(new Font("Arial Unicode MS", Font.PLAIN, 14));

		txtMessage = new JTextField();
		txtMessage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					send(txtMessage.getText(), true);
				}
			}
		});
		GridBagConstraints gbc_txtMessage = new GridBagConstraints();
		gbc_txtMessage.insets = new Insets(0, 0, 0, 5);
		gbc_txtMessage.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtMessage.gridx = 0;
		gbc_txtMessage.gridy = 2;
		gbc_txtMessage.gridwidth = 2;
		gbc_txtMessage.weightx = 1;
		gbc_txtMessage.weighty = 0;
		contentPane.add(txtMessage, gbc_txtMessage);
		txtMessage.setColumns(10);

		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				send(txtMessage.getText(), true);
			}
		});
		GridBagConstraints gbc_btnSend = new GridBagConstraints();
		gbc_btnSend.insets = new Insets(0, 0, 0, 5);
		gbc_btnSend.gridx = 2;
		gbc_btnSend.gridy = 2;
		gbc_btnSend.weightx = 0;
		gbc_btnSend.weighty = 0;
		contentPane.add(btnSend, gbc_btnSend);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				//				System.out.println("closed");
				String disconnect = "/d/" + client.getID();
				send(disconnect.trim(), false);
				running = false;
				client.close();
			}
		});

		setVisible(true);
		txtMessage.requestFocus();
	}

	public void run() {
		listen();
	}

	public void listen() {
		listen = new Thread("Listen") {
			public void run() {
				while (running) {
					String message = client.receive().trim().replaceAll("\\s+", " ");
					if (message.startsWith("/c/")) {
						client.setID(Integer.parseInt(message.substring(3, message.length())));
						write("Successfully connected to the server! ID: " + client.getID());
					} else if (message.startsWith("/m/")) {
						String text = message.substring(3, message.length());
						write(text);
					} else if (message.startsWith("/i/")) {
						String text = "/i/" + client.getID();
						text.trim();
						send(text, false);
					} else if (message.startsWith("/u/")) {
						String[] user = message.split("/u/|/n/");
						users.update(Arrays.copyOfRange(user, 1, user.length));
					}
				}
			}
		};
		listen.start();
	}

	private void send(String message, boolean isMsg) {
		message = message.replaceAll("\\s+", " ").trim();
		if (isMsg && !message.equals("")) {
			message = client.getName() + ": " + message;
			message = "/m/" + message;
			txtMessage.setText(""); // after sending message remove all text from input line
		}
		client.send(message.getBytes());
	}

	public void write(String message) {
		String msg = message.trim().replaceAll("\\s+", " ");
		if (!msg.isEmpty()) 
			txtrHistory.append(msg + "\n\r");
	}
}
