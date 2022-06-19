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
    DataStream dsexternal;
    final DataStream dslocal;
    final boolean pvp;
    AI ai;
    final ConstructedDeck[] decks;
    private AIConfig config;

    public ServerGameThread(DataStream dsclient, boolean pvp) {
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
            this.ai = new AI(dsai, this.config);
            this.ai.start();
        }
        // accept decklists
        while (this.decks[0] == null || this.decks[1] == null) {
            MessageType mtype = this.dsexternal.receive();
            if (mtype == MessageType.DECK) {
                this.setDecklist(1, this.dsexternal.readDecklist());
            } else {
                this.dsexternal.discardMessage();
            }
        }
        GameController gc = new GameController(List.of(this.dslocal, this.dsexternal),
                Arrays.stream(this.decks).map(d -> CardSet.getDefaultLeader(d.craft)).collect(Collectors.toList()),
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

    public void setDecklist(int index, ConstructedDeck deck) {
        this.decks[index] = deck;
    }

    public void setAIConfig(AIConfig config) {
        this.config = config;
    }
}
