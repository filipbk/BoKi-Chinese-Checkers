package client;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import communication.ConnectRequest;
import communication.ConnectResponse;
import server.Server;

public class ClientTest {
	Server server;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		// = new Client("localhost", 8988, "User");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void clientConnection() throws IOException, ClassNotFoundException {
		Socket socket = mock(Socket.class);
		ServerSocket serverSocket = mock(ServerSocket.class);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buff = new byte[500];
		ByteArrayInputStream in = new ByteArrayInputStream(buff);
		//ObjectInputStream oin = new ObjectInputStream(in);
		ObjectOutputStream oout = new ObjectOutputStream(out);
		
		
		Client client = new Client("localhost", 8988, "User") {
			@Override
			Socket createSocket() throws UnknownHostException, IOException {
				return socket;
			}
			@Override
			public void runServerListener() {
				
			}
			
			@Override
			ObjectInputStream createInputStream() throws IOException {
				return null;
			}
		};
		when(serverSocket.accept()).thenReturn(socket);
		when(socket.getOutputStream()).thenReturn(out);
		when(socket.getInputStream()).thenReturn(in);
		//doReturn(out).when(socket).getOutputStream();
		serverSocket.accept();
		client.connect();
		assertTrue(client.isConnected());
	}
}
