package network;

import java.io.*;
import java.net.*;

import client.*;

public class DataStreamServer extends DataStream {
	private ServerSocket serverSocket;

	public DataStreamServer() {
		try {
			this.serverSocket = new ServerSocket(Game.SERVER_PORT);
			this.socket = this.serverSocket.accept();
			this.out = new PrintWriter(this.socket.getOutputStream());
			this.objectOut = new ObjectOutputStream(this.socket.getOutputStream());
			this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			this.objectIn = new ObjectInputStream(this.socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
