package client;

import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;

import java.io.IOException;
import server.event.Event;

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

	public String send(Event e) throws IOException {
		buffer = "".getBytes();
		if (e != null) {
			buffer = e.toString().getBytes();
		}
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
		socket.send(packet);
		packet = new DatagramPacket(buffer, buffer.length);
		socket.receive(packet);
		String received = new String(packet.getData(), 0, packet.getLength());
		return received;
	}

	public void close() {
		socket.close();
	}
}
