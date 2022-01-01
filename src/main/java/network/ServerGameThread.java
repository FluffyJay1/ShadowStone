package network;

import java.io.*;
import java.net.*;
import java.util.*;

import client.*;
import server.*;
import server.ai.*;
import server.card.*;
import server.card.cardpack.*;
import server.card.leader.*;
import server.card.unleashpower.*;
import server.event.*;
import server.playeraction.*;
import server.resolver.*;

/**
 * thread for p2p battles on same network, one computer acts as server, each
 * thread is a single game
 * 
 * @author Michael
 *
 */
public class ServerGameThread extends Thread {
    DataStream dsexternal;
    final DataStream dslocal;
    final boolean pvp;
    AI ai;
    final Board b;
    final VisualBoard localBoard;
    final ConstructedDeck[] decks;

    public ServerGameThread(DataStream dsclient, boolean pvp, VisualBoard localBoard) {
        this.b = new Board();
        this.localBoard = localBoard;
        this.dslocal = dsclient;
        this.pvp = pvp;
        this.b.localteam = localBoard.localteam;
        this.decks = new ConstructedDeck[2];
    }

    // TODO REWORK GAME LOGIC INTO ITS OWN CLASS
    @Override
    public void run() {
        if (this.pvp) { // if pvp, wait for a player to connect
            try {
                ServerSocket serverSocket = new ServerSocket(Game.SERVER_PORT);
                this.dsexternal = new DataStream(serverSocket.accept());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            DataStream dsai = new DataStream();
            this.dsexternal = new DataStream();
            DataStream.pair(dsai, this.dsexternal);
            this.ai = new AI(dsai, this.b.localteam * -1, 0);
            this.ai.start();
        }
        // accept decklists
        while (this.decks[0] == null && this.decks[1] == null) {
            if (this.dsexternal.ready()) {
                MessageType mtype = this.dsexternal.receive();
                if (mtype == MessageType.DECK) {
                    this.setDecklist(this.localBoard.localteam * -1, this.dsexternal.readDecklist());
                } else {
                    this.dsexternal.discardMessage();
                }
            }
        }
        this.initializeGame();
        // TODO mulligan phase
        // game loop
        while (this.b.winner == 0) {
            this.handleGameInput(this.dsexternal, this.localBoard.localteam * -1);
            this.handleGameInput(this.dslocal, this.localBoard.localteam);
        }
        if (this.dsexternal.socket != null) { // aka if pvp
            try {
                this.dsexternal.socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void initializeGame() {
        for (int team = 1; team >= -1; team -= 2) { // deckbuilding 101
            ArrayList<Card> cards = new ArrayList<>(this.decks[(team - 1) / -2].convertToCards(this.b));
            while (!cards.isEmpty()) {
                Card selected = Game.selectRandom(cards);
                this.b.processEvent(null, null,
                        new EventCreateCard(List.of(selected), team, CardStatus.DECK, List.of(0), null));
                cards.remove(selected);
            }
            UnleashPower up = (UnleashPower) Card.createFromConstructor(this.b,
                    CardSet.getDefaultUnleashPower(this.decks[(team - 1) / -2].craft));
            this.b.processEvent(null, null,
                    new EventCreateCard(List.of(up), team, CardStatus.UNLEASHPOWER, List.of(0), null));
            // TODO change leader
            this.b.processEvent(null, null,
                    new EventCreateCard(List.of(new Rowen(this.b)), team, CardStatus.LEADER, List.of(0), null));
        }
        this.b.resolve(new DrawResolver(this.b.player1, 3));
        this.b.resolve(new DrawResolver(this.b.player2, 3));
        this.b.resolve(new TurnStartResolver(this.b.player1));
        this.sendEvents();
    }

    private void handleGameInput(DataStream ds, int team) {
        while (ds.ready()) {
            MessageType mtype = ds.receive();
            switch (mtype) {
            case PLAYERACTION:
                if (this.b.currentPlayerTurn == team) {
                    String action = ds.readPlayerAction();
                    System.out.println("team" + team + " action: " + action);
                    this.b.executePlayerAction(new StringTokenizer(action));
                    this.sendEvents();
                } else {
                    ds.discardMessage();
                }
                break;
            case EMOTE:
                String emote = ds.readEmote();
                if (emote.equals("save")) {
                    this.b.saveBoardState();
                }
                if (emote.equals("load")) {
                    System.out.println("BITES ZA DUSTO");
                    this.b.loadBoardState();
                    this.dsexternal.sendResetBoard();
                    this.dslocal.sendResetBoard();
                    this.sendEvents();
                }
                break;
            default:
                ds.discardMessage();
                break;
            }
        }
    }

    public void sendEvents() {
        String eventstring = this.b.retrieveEventString();
        this.sendEvent(1, eventstring);
        this.sendEvent(-1, eventstring);
    }

    public void sendEvent(int team, String eventstring) {
        if (team == this.localBoard.localteam) {
            // this.localBoard.parseEventString(eventstring);
            this.dslocal.sendEvent(eventstring);
        } else {
            this.dsexternal.sendEvent(eventstring);
        }
    }

    public void sendPlayerAction(int team, PlayerAction action) {
        if (team == this.localBoard.localteam) {
            // display something idk
        } else {
            this.dsexternal.sendPlayerAction(action.toString());
        }
    }

    public void sendDecklist(int team, ConstructedDeck deck) {
        if (team == this.localBoard.localteam) {
            // display something idk
        } else {
            this.dsexternal.sendDecklist(deck);
        }
    }

    public void setDecklist(int team, ConstructedDeck deck) {
        this.decks[(team - 1) / -2] = deck;
    }
}
