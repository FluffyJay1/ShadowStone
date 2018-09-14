package client;

import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;

import java.io.IOException;

public class DataStreamClient extends Thread {

	private byte[] buffer;
	private InetAddress address;
	private int port;

	private DatagramSocket socket;

	public void run() {
		try {

		} catch (Exception e) {

		}

	}

	public String send(String message) throws IOException {
		buffer = message.getBytes();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
		socket.send(packet);
		packet = new DatagramPacket(buffer, buffer.length);
		socket.receive(packet);
		return "";
	}

	public void close() {
		socket.close();
	}
}
