package network;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import client.*;
import server.card.cardset.*;
import server.event.eventburst.EventBurst;

/**
 * interface for sending and receiving things, serializing and deserializing
 * 
 * @author Michael
 *
 */
public class DataStream {
    Socket socket;
    ObjectOutputStream out;
    ObjectInputStream in;
    private MessageType lastMessageType;

    public DataStream() {

    }

    public DataStream(Socket socket) {
        try {
            this.socket = socket;
            this.out = new ObjectOutputStream(this.socket.getOutputStream());
            this.in = new ObjectInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public DataStream(String ip, int port) throws IOException {
        this(new Socket(ip, port));
    }

    public static void pair(DataStream a, DataStream b) { // local data streams
        PipedInputStream apr = new PipedInputStream(), bpr = new PipedInputStream();
        PipedOutputStream apw = new PipedOutputStream(), bpw = new PipedOutputStream();
        try {
            apr.connect(bpw);
            bpr.connect(apw);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        a.pipeOut(apw);
        b.pipeOut(bpw);
        a.pipeIn(apr);
        b.pipeIn(bpr);
    }

    private void pipeOut(PipedOutputStream pos) {
        try {
            this.out = new ObjectOutputStream(pos);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void pipeIn(PipedInputStream pis) {
        try {
            this.in = new ObjectInputStream(pis);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void sendEventBurstString(String eventBurstString) throws IOException {
        this.out.writeInt(MessageType.EVENT.ordinal());
        this.out.writeObject(eventBurstString);
        this.out.flush();
    }

    public void sendPlayerAction(String action) throws IOException {
        this.out.writeInt(MessageType.PLAYERACTION.ordinal());
        this.out.writeObject(action);
        this.out.flush();
    }

    public void sendEmote(Emote emote) throws IOException {
        this.out.writeInt(MessageType.EMOTE.ordinal());
        this.out.writeObject(emote);
        this.out.flush();
    }

    public void sendDecklist(ConstructedDeck deck) throws IOException {
        this.out.writeInt(MessageType.DECK.ordinal());
        this.out.writeObject(deck);
        this.out.flush();
    }

    public void sendTeamAssign(int team) throws IOException {
        this.out.writeInt(MessageType.TEAMASSIGN.ordinal());
        this.out.writeInt(team);
        this.out.flush();
    }

    public void sendCommand(String command) throws IOException {
        this.out.writeInt(MessageType.COMMAND.ordinal());
        this.out.writeObject(command);
        this.out.flush();
    }

    /*
     * two parter, first use this method to determine message type, then use a
     * corresponding read...() method to finish reading the message
     * returns null if the connection was closed
     */
    public MessageType receive() throws IOException {
        MessageType mtype = MessageType.values()[in.readInt()];
        this.lastMessageType = mtype;
        return mtype;
    }

    public List<EventBurst> readEventBursts() throws IOException {
        try {
            return EventBurst.parseEventBursts((String) in.readObject());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String readPlayerAction() throws IOException {
        try {
            return (String) in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Emote readEmote() throws IOException {
        try {
            return (Emote) in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ConstructedDeck readDecklist() throws IOException {
        try {
            return (ConstructedDeck) this.in.readObject();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public int readTeamAssign() throws IOException {
        return in.readInt();
    }

    public String readCommand() throws IOException {
        try {
            return (String) in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * If we don't care about the message we just received, we "discard" it, reading
     * the message but not doing anything with it
     */
    public void discardMessage() throws IOException {
        switch (this.lastMessageType) {
            case EVENT -> this.readEventBursts();
            case PLAYERACTION -> this.readPlayerAction();
            case DECK -> this.readDecklist();
            case EMOTE -> this.readEmote();
            case TEAMASSIGN -> this.readTeamAssign();
            case COMMAND -> this.readCommand();
            default -> {
            }
        }
    }

    public void close() {
        try {
            if (this.socket != null) {
                this.socket.close();
            }
            this.out.close();
            this.in.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
