package network;

import java.io.*;
import java.net.*;

import client.*;
import server.card.cardpack.*;
import server.playeraction.*;

/**
 * interface for sending and receiving things, serializing and deserializing
 * 
 * @author Michael
 *
 */
public class DataStream {
	Socket socket;
	PrintWriter out;
	ObjectOutputStream objectOut;
	BufferedReader in;
	ObjectInputStream objectIn;
	private MessageType lastMessageType;

	public DataStream() {

	}

	public DataStream(Socket socket) {
		try {
			this.socket = socket;
			this.out = new PrintWriter(this.socket.getOutputStream());
			this.objectOut = new ObjectOutputStream(this.socket.getOutputStream());
			this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			this.objectIn = new ObjectInputStream(this.socket.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public DataStream(String ip, int port) throws UnknownHostException, IOException {
		this(new Socket(ip, port));
	}

	public void sendEvent(String eventstring) {
		this.out.println(MessageType.EVENT);
		this.out.println(eventstring + Game.BLOCK_END);
	}

	public void sendPlayerAction(PlayerAction action) {
		this.out.println(MessageType.PLAYERACTION);
		this.out.println(action.toString());
	}

	public void sendDecklist(ConstructedDeck deck) {
		this.out.println(MessageType.DECK);
		try {
			this.objectOut.writeObject(deck);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean ready() {
		try {
			return this.in.ready();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	// two parter, first use this method to determine message type, then use
	// a corresponding read...() method to finish reading the message
	public MessageType receive() {
		String header = "";
		try {
			header = in.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MessageType mtype = null;
		for (MessageType mt : MessageType.values()) {
			if (mt.toString().equals(header)) {
				mtype = mt;
			}
		}
		this.lastMessageType = mtype;
		return mtype;
	}

	public String readEvent() {
		try {
			String events = "", line = "";
			while (!line.equals(Game.BLOCK_END)) {
				events += line;
				line = in.readLine();
			}
			return events;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String readPlayerAction() {
		try {
			return in.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public ConstructedDeck readDecklist() {
		try {
			return (ConstructedDeck) this.objectIn.readObject();
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void discardMessage() {
		switch (this.lastMessageType) {
		case EVENT:
			this.readEvent();
			break;
		case PLAYERACTION:
			this.readPlayerAction();
			break;
		case DECK:
			this.readDecklist();
			break;
		default:
			break;
		}
	}

	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
