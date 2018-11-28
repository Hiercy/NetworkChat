package com.mike.chat.server;

import java.net.SocketException;

public class ServerMain {
	
	private int port;
	
	public ServerMain(int port) {
		this.port = port;
		new Server(port);
	}
	
	public static void main(String[] args) throws SocketException {
		int port;
		
		if (args.length != 1) 
			throw new IllegalArgumentException("[port]");
		
		port = Integer.parseInt(args[0]);
		new ServerMain(port);
	}
}
