package network;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

import client.*;
import server.*;
import server.ai.*;
import server.card.cardset.*;
import server.card.leader.*;

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
    final int localteam;
    final ConstructedDeck[] decks;

    public ServerGameThread(DataStream dsclient, int localteam, boolean pvp) {
        this.localteam = localteam;
        this.dslocal = dsclient;
        this.pvp = pvp;
        this.decks = new ConstructedDeck[2];
    }

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
            this.ai = new AI(dsai, this.localteam * -1, 0);
            this.ai.start();
        }
        // accept decklists
        while (this.decks[0] == null || this.decks[1] == null) {
            MessageType mtype = this.dsexternal.receive();
            if (mtype == MessageType.DECK) {
                this.setDecklist(this.localteam * -1, this.dsexternal.readDecklist());
            } else {
                this.dsexternal.discardMessage();
            }
        }
        GameController gc = new GameController(List.of(this.dslocal, this.dsexternal),
                List.of(new Rowen(), new Rowen()),
                Arrays.stream(this.decks).map(d -> CardSet.getDefaultUnleashPower(d.craft)).collect(Collectors.toList()),
                Arrays.stream(this.decks).collect(Collectors.toList()));
        gc.startInit();
        gc.startGame();
        while (gc.isGamePhase() && !this.isInterrupted()) {
            gc.updateGame();
        }
        gc.end();
        this.dsexternal.close();
        this.dslocal.close();
    }

    public void setDecklist(int team, ConstructedDeck deck) {
        this.decks[(team - 1) / -2] = deck;
    }
}
