package client;

import java.util.LinkedList;
import java.util.StringTokenizer;

import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;

import java.io.IOException;
import server.event.Event;
import server.Board;

public class DataStreamClient {

	private byte[] buffer;
	private InetAddress address;
	private int port;
	Board b;

	private DatagramSocket socket;

	public DataStreamClient(Board b) {
		this.b = b;
	}

	public LinkedList<Event> send(Event e) throws IOException {
		buffer = "".getBytes();
		if (e != null) {
			buffer = e.toString().getBytes();
		}
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
		socket.send(packet);
		packet = new DatagramPacket(buffer, buffer.length);
		socket.receive(packet);
		String received = new String(packet.getData(), 0, packet.getLength());
		return convertToEvent(received);
	}

	public LinkedList<Event> convertToEvent(String recv) {
		LinkedList<Event> l = new LinkedList<Event>();
		String[] eventstrings = recv.split(";"); // so hopefully server will be implemented to separate events by
													// semicolon
		for (String s : eventstrings) {
			l.add(Event.createFromString(b, new StringTokenizer(s)));
		}
		return l;
	}

	public void close() {
		socket.close();
	}
}
