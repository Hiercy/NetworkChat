package com.mike.chat.entity;


import java.net.InetAddress;

//Client entity 
public class Clnt {

	public String name; 		// user name
	public InetAddress host; 	// user address
	public int port; 			// user port
	private final int id; 		// user id
	public int attempt = 0; 	// attempt to reconnect to the server

	public Clnt(String name, InetAddress host, int port, final int id) {
		this.name = name;
		this.host = host;
		this.port = port;
		this.id   = id;
	}

	public int getID() {
		return id;
	}
}
