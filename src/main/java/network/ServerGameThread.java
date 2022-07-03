package network;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

import client.*;
import server.*;
import server.ai.*;
import server.card.cardset.*;

/**
 * thread for p2p battles on same network, one computer acts as server, each
 * thread is a single game
 * 
 * @author Michael
 *
 */
public class ServerGameThread extends Thread {
    final DataStream[] ds; // 0 = local, 1 = external
    final boolean pvp;
    AI ai;
    final ConstructedDeck[] decks;
    private AIConfig config;
    private boolean peerConnected;

    public ServerGameThread(DataStream dslocal, boolean pvp) {
        this.ds = new DataStream[2];
        this.ds[0] = dslocal;
        this.pvp = pvp;
        this.decks = new ConstructedDeck[2];
        this.peerConnected = false;
    }

    @Override
    public void run() {
        if (this.pvp) { // if pvp, wait for a player to connect
            try {
                ServerSocket serverSocket = new ServerSocket(Game.SERVER_PORT);
                this.ds[1] = new DataStream(serverSocket.accept());
                this.peerConnected = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            DataStream dsai = new DataStream();
            this.ds[1] = new DataStream();
            DataStream.pair(dsai, this.ds[1]);
            this.ai = new AI(dsai, this.config);
            this.ai.start();
        }
        // accept decklists
        while (this.decks[0] == null || this.decks[1] == null) {
            for (int i = 0; i < 2; i++) {
                if (this.ds[i].ready()) {
                    MessageType mtype = this.ds[i].receive();
                    if (mtype == MessageType.DECK) {
                        this.setDecklist(1, this.ds[i].readDecklist());
                    } else {
                        this.ds[i].discardMessage();
                    }
                }
            }
        }
        GameController gc = new GameController(List.of(this.ds[0], this.ds[1]),
                Arrays.stream(this.decks).map(d -> CardSet.getDefaultLeader(d.craft)).collect(Collectors.toList()),
                Arrays.stream(this.decks).map(d -> CardSet.getDefaultUnleashPower(d.craft)).collect(Collectors.toList()),
                Arrays.stream(this.decks).collect(Collectors.toList()));
        gc.startInit();
        gc.startGame();
        while (gc.isGamePhase() && !this.isInterrupted()) {
            gc.updateGame();
        }
        gc.end();
        for (int i = 0; i < 2; i++) {
            this.ds[i].close();
        }
    }

    public boolean isPeerConnected() {
        return this.peerConnected;
    }

    public void setDecklist(int index, ConstructedDeck deck) {
        this.decks[index] = deck;
    }

    public void setAIConfig(AIConfig config) {
        this.config = config;
    }
}
