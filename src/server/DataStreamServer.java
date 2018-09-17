package server;

import server.event.Event;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.io.IOException;

public class DataStreamServer extends Thread {

	private boolean running;
	private byte[] buffer = new byte[1024];

	private DatagramSocket socket;
	Board b;

	public DataStreamServer(Board b) {
		this.b = b;
	}

	public void run() {
		running = true;
		try {
			while (running) {
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);

				InetAddress address = packet.getAddress();
				int port = packet.getPort();
				String received = new String(packet.getData());
				b.eventlist.addAll(convertToEvent(received));
				LinkedList<Event> happenings = b.resolveAll(); // great name
				String ret = "";
				for (Event e : happenings) {
					ret += e.toString() + ";";
				}
				byte[] retbyte = ret.getBytes();
				packet = new DatagramPacket(retbyte, retbyte.length, address, port);
				socket.send(packet);
			}
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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

}
