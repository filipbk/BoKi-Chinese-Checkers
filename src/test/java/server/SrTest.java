package server;

import client.Client;

public class SrTest {
	public static void main(String args[]) 
    {
		Server server = Server.getInstance(8988);
    	server.listenForNewConnections();
    }
}