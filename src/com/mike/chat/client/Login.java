package com.mike.chat.client;

import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Login extends JFrame {
	private static final long serialVersionUID = 5723607877053757049L;
	
	private JPanel contentPane;
	private JTextField txtName;
	private JTextField txtHost;
	private JTextField txtPort;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login frame = new Login();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Login() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		setResizable(false);
		setTitle("Login");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(300, 380);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		txtName = new JTextField();
		txtName.setBounds(77, 46, 139, 20);
		contentPane.add(txtName);
		txtName.setColumns(10);
		
		JLabel lblName = new JLabel("Name:");
		lblName.setBounds(126, 21, 41, 14);
		contentPane.add(lblName);
		
		txtHost = new JTextField();
		txtHost.setBounds(77, 123, 139, 20);
		contentPane.add(txtHost);
		txtHost.setColumns(10);
		
		txtPort = new JTextField();
		txtPort.setBounds(77, 200, 139, 20);
		contentPane.add(txtPort);
		txtPort.setColumns(10);
		
		JButton btnLogIn = new JButton("Log in");
		btnLogIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name = txtName.getText();
				String host = txtHost.getText();
				int port = Integer.parseInt(txtPort.getText());
				
				login(name, host, port);
			}

			private void login(String name, String host, int port) {
				dispose();
//				System.out.println("Name: " + name + "\nHost: " + host + "\nPort: " + port);
				new ClientWindow(name, host, port);
			}
		});
		btnLogIn.setBounds(102, 303, 89, 23);
		contentPane.add(btnLogIn);
		
		JLabel lblHost = new JLabel("Host:");
		lblHost.setBounds(131, 98, 32, 14);
		contentPane.add(lblHost);
		
		JLabel lblPort = new JLabel("Port:");
		lblPort.setBounds(131, 175, 32, 14);
		contentPane.add(lblPort);
	}
}
