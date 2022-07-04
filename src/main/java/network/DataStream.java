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
    PrintStream out;
    ObjectOutputStream objectOut;
    BufferedReader in;
    ObjectInputStream objectIn;
    private MessageType lastMessageType;

    public DataStream() {

    }

    public DataStream(Socket socket) {
        try {
            this.socket = socket;
            this.out = new PrintStream(this.socket.getOutputStream(), true);
            this.objectOut = new ObjectOutputStream(this.socket.getOutputStream());
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.objectIn = new ObjectInputStream(this.socket.getInputStream());
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
        this.out = new PrintStream(pos, true, StandardCharsets.UTF_16);
        try {
            this.objectOut = new ObjectOutputStream(pos);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void pipeIn(PipedInputStream pis) {
        this.in = new BufferedReader(new InputStreamReader(pis, StandardCharsets.UTF_16));
        try {
            this.objectIn = new ObjectInputStream(pis);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void sendEventBurstString(String eventBurstString) {
        this.out.println(MessageType.EVENT);
        this.out.println(eventBurstString);
        this.out.println(Game.BLOCK_END);
    }

    public void sendPlayerAction(String action) {
        this.out.println(MessageType.PLAYERACTION);
        this.out.print(action);
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

    public void sendEmote(String emote) {
        this.out.println(MessageType.EMOTE);
        this.out.println(emote);
    }

    public void sendResetBoard() {
        this.out.println(MessageType.BOARDRESET);
    }

    public void sendTeamAssign(int team) {
        this.out.println(MessageType.TEAMASSIGN);
        this.out.println(team);
    }

    /**
     * Return whether the next read won't block
     * @return true if the next read won't block
     */
    public boolean ready() {
        try {
            return this.in.ready();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    /*
     * two parter, first use this method to determine message type, then use a
     * corresponding read...() method to finish reading the message
     * returns null if the connection was closed
     */
    public MessageType receive() {
        String header = "";
        try {
            header = in.readLine();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (header == null) { // connection closed by peer
            return null;
        }
        try {
            MessageType mtype = MessageType.valueOf(header);
            this.lastMessageType = mtype;
            return mtype;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<EventBurst> readEventBursts() {
        try {
            StringBuilder events = new StringBuilder();
            String line = in.readLine();
            while (!line.equals(Game.BLOCK_END)) {
                if (!events.isEmpty()) {
                    events.append("\n");
                }
                events.append(line);
                line = in.readLine();
            }
            String s = events.toString();
            return EventBurst.parseEventBursts(s);
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

    public String readEmote() {
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

    public int readTeamAssign() {
        try {
            return Integer.parseInt(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /*
     * If we don't care about the message we just received, we "discard" it, reading
     * the message but not doing anything with it
     */
    public void discardMessage() {
        switch (this.lastMessageType) {
            case EVENT -> this.readEventBursts();
            case PLAYERACTION -> this.readPlayerAction();
            case DECK -> this.readDecklist();
            case EMOTE -> this.readEmote();
            case TEAMASSIGN -> this.readTeamAssign();
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

    public boolean error() {
        return this.out.checkError();
    }
}
