package com.mike.chat.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.mike.chat.UniqueID;
import com.mike.chat.entity.Clnt;

public class Server implements Runnable {

	private List<Clnt> clients = new ArrayList<>();
	private List<Integer> clientResponse = new ArrayList<>();

	private final int MAX_ATTEMPT = 5;

	private int port;
	private DatagramSocket socket;
	private boolean runnable = false;
	private Thread manage, receive, send;
	//	private Client client;

	private boolean raw = false;

	public Server(int port) {
		this.port = port;
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}

		new Thread(this, "Server").start();
	}

	@Override
	public void run() {
		runnable = true;
		System.out.println("Server listening port " + port + "...");
		manageClients();
		receive();

		Scanner sc = new Scanner(System.in);
		while (runnable) {
			String text = sc.nextLine();
			text.trim();
			if (!text.startsWith("/")) {
				sendToAll("/m/Server: " + text);
				continue;
			}
			text = text.substring(1);
			if (text.equals("raw")) 
				raw = !raw;
			else if (text.equals("online")) {
				System.out.println("List: ");
				for (int i = 0; i < clients.size(); i++) {
					Clnt c = clients.get(i);
					System.out.println(i + " " + c.name + "(" + c.getID() + ")" + "with port: " + c.port);
				}
			} else if (text.startsWith("kick")) {
				int id = -1;
				String name = text.split(" ")[1];
				boolean number = true;
				try {
					id = Integer.parseInt(name);
				} catch (NumberFormatException e) {
					number = false;
				}
				if (number) 
					kickById(id);
				 else 
					kickByName(name);
				
				continue;
			}
		}
	}

	private void kickByName(String name) {
		for (int i = 0; i < clients.size(); i++) {
			Clnt c = clients.get(i);
			if (name.equals(name)) {
				disconnect(c.getID(), true);
				break;
			}
		}
	}

	private void kickById(int id) {
		boolean exist = false;
		for (int i = 0; i < clients.size(); i++) {
			if (clients.get(i).getID() == id) {
				exist = true;
				break;
			}
		}
		if (exist)
			disconnect(id, true);
		else
			System.out.println("Client with id = " + id + " doesn't exist ");
	}

	private void receive() {
		receive = new Thread("Receive") {
			public void run() {
				while (runnable) {
					byte[] data = new byte[1024];
					DatagramPacket packet = new DatagramPacket(data, data.length);
					try {
						socket.receive(packet);
					} catch (IOException e) {
						e.printStackTrace();
					}
					process(packet);
				}
			}
		};
		receive.start();
	}

	private void process(DatagramPacket packet) {
		String message = new String(packet.getData()).replaceAll("\\s+", " ").trim();

		if (raw) 
			System.out.println(message);

		if (message.startsWith("/c/")) {
			int randId = UniqueID.getID();
			System.out.println("ID = " + randId);
			clients.add(new Clnt(message.substring(3, message.length()), packet.getAddress(), packet.getPort(), randId));
			System.out.println(message);
			String ID = "/c/" + randId;
			send(ID.getBytes(), packet.getAddress(), packet.getPort());
		} else if (message.startsWith("/m/")) {
			sendToAll(message);
		} else if (message.startsWith("/d/")) {
			String id = message;
			disconnect(Integer.parseInt(id.substring(3, id.length())), true);
		} else if (message.startsWith("/i/")) {
			clientResponse.add(Integer.parseInt(message.substring(3, message.length())));
		} else
			System.out.println(packet.getAddress() + ":" + packet.getPort() + "> " + message);
	}

	private void disconnect(int id, boolean status) {
		Clnt c = null;
		boolean existed = false;
		for (int i = 0; i < clients.size(); i++) {
			if (clients.get(i).getID() == id) {
				c = clients.get(i);
				clients.remove(i);
				existed = true;
				break;
			}
		}
		if (!existed)
			return;

		String message = "";
		if (status) 
			message = "Client " + c.name + "(" + c.getID() + ") @ " + c.host + ":" + c.port + " disconnected";
		else
			message = "Client " + c.name + "(" + c.getID() + ") @ " + c.host + ":" + c.port + " time out";

		System.out.println(message);
	}

	private void sendToAll(String message) {
		message = message.replaceAll("\\s+", " ").trim();
		if (message.startsWith("/m/")) {
			String text = message.substring(3, message.length());
			System.out.println(text);
		}
		for (int i = 0; i < clients.size(); i++) {
			Clnt client = clients.get(i);
			send(message.getBytes(), client.host, client.port);
		}
	}

	private void send(final byte[] data, final InetAddress host, final int port) {
		send = new Thread("Send") {
			public void run() {
				DatagramPacket packet = new DatagramPacket(data, data.length, host, port);
				try {
					socket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		send.start();
	}

	private void manageClients() {
		manage = new Thread("Manage") {
			public void run() {
				while (runnable) {
					sendToAll("/i/server");
					sendStatus();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					for (int i = 0; i < clients.size(); i++) {
						Clnt c = clients.get(i);
						if (!clientResponse.contains(c.getID())) {
							if (c.attempt >= MAX_ATTEMPT) {
								disconnect(c.getID(), false);
							} else {
								c.attempt++;
							}
						} else {
							clientResponse.remove(new Integer(c.getID()));
							c.attempt = 0;
						}
					}
				}
			}

			private void sendStatus() {
				if (clients.size() <= 0)
					return;
				String users = "/u/";
				for (int i = 0; i < clients.size() - 1; i++) 
					users += clients.get(i).name + "/n/";
				users += clients.get(clients.size() - 1).name;
				sendToAll(users);
			}
		};
		manage.start();
	}
}
