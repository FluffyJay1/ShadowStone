package server;

import network.DataStream;
import network.Emote;
import network.MessageType;
import server.card.Card;
import server.card.CardStatus;
import server.card.CardVisibility;
import server.card.LeaderText;
import server.card.cardset.ConstructedDeck;
import server.event.Event;
import server.event.EventCreateCard;
import server.resolver.DrawResolver;
import server.resolver.Resolver;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.SynchronousQueue;

/**
 * Controller for one round
 * Interfaces with each player thru a datastream
 * Has different phases:
 * Init -> Game -> End
 * Init: put all the cards in place (decks, leaders, etc)
 * Game: game loop where each player plays cards, orders minions to attack, etc.
 * End: a player has won, terminate the game
 * So usage of the game controller would look like:
 *  startInit();
 *  startGame();
 *  while (isGamePhase()) {
 *      updateGame();
 *  }
 *  end();
 */
public class GameController {
    final ServerBoard b;
    List<DataStream> players;
    List<LeaderText> leaders;
    List<UnleashPowerText> unleashPowers;
    List<ConstructedDeck> decks;
    private boolean[] crashed;
    private final SynchronousQueue<Runnable> bufferedUpdates; // stuff that the dsReadingThread wants to do on this thread
    int teamMultiplier; // if 1, then first index is team 1, if -1 then first index is team -1

    public GameController(List<DataStream> players, List<LeaderText> leaders, List<UnleashPowerText> unleashPowers, List<ConstructedDeck> decks) {
        this.b = new ServerBoard(0);
        this.players = players;
        this.leaders = leaders;
        this.unleashPowers = unleashPowers;
        this.decks = decks;
        for (int i = 0; i <= 1; i++) {
            int finalI = i;
            Thread dsReadingThread = new Thread(() -> {
                while (true) {
                    try {
                        this.handleGameInput(this.players.get(finalI), indexToTeam(finalI));
                    } catch (IOException e) {
                        this.crashed[finalI] = true;
                        break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            dsReadingThread.start();
        }
        this.crashed = new boolean[2];
        this.bufferedUpdates = new SynchronousQueue<>();
    }

    public void startInit() throws IOException {
        this.teamMultiplier = Math.random() > 0.5 ? 1 : -1;
        this.sendTeamAssignments();
        Resolver startResolver = new Resolver(false) {
            @Override
            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                for (int i = 0; i <= 1; i++) { // deckbuilding 101
                    int team = indexToTeam(i);
                    List<Card> cards = decks.get(i).convertToCards(b);
                    List<Card> shuffledCards = SelectRandom.from(cards, cards.size());
                    List<Integer> inds = new ArrayList<>(shuffledCards.size());
                    for (int j = 0; j < shuffledCards.size(); j++) {
                        inds.add(j);
                    }
                    b.processEvent(rq, null,
                            new EventCreateCard(shuffledCards, team, CardStatus.DECK, inds, CardVisibility.NONE));
                    UnleashPower up = unleashPowers.get(i).constructInstance(b);
                    b.processEvent(rq, null,
                            new EventCreateCard(List.of(up), team, CardStatus.UNLEASHPOWER, List.of(0), CardVisibility.ALL));
                    b.processEvent(rq, null,
                            new EventCreateCard(List.of(leaders.get(i).constructInstance(b)), team, CardStatus.LEADER, List.of(0), CardVisibility.ALL));
                }
            }
        };
        this.b.resolve(startResolver, 0);
        this.sendEvents();
    }

    public void startGame() throws IOException {
        this.b.resolve(new DrawResolver(this.b.player1, 3), 0);
        this.b.resolve(new DrawResolver(this.b.player2, 4), 0);
        this.sendEvents();
    }

    public boolean isGamePhase() {
        return this.b.getWinner() == 0;
    }

    public void updateGame() throws IOException {
        for (Runnable update = bufferedUpdates.poll(); update != null; update = bufferedUpdates.poll()) {
            update.run();
        }
        for (int i = 0; i <= 1; i++) {
            if (this.crashed[i]) {
                throw new IOException();
            }
        }
    }

    // TODO
    public void end() {

    }

    public void resolve(Resolver r, int team) throws IOException {
        this.b.resolve(r, team);
        this.sendEvents();
    }

    public int getWinner() {
        return this.b.getWinner();
    }

    // blocking, so this is called from the dsReadingThread
    private void handleGameInput(DataStream ds, int team) throws IOException, InterruptedException {
        // okay exception handling is ugly
        MessageType mtype = ds.receive();
        switch (mtype) {
            case PLAYERACTION:
                if (this.b.getCurrentPlayerTurn() != team * -1) {
                    String action = ds.readPlayerAction();
                    System.out.println("team " + team + " action: " + action);
                    this.bufferedUpdates.put(() -> {
                        this.b.executePlayerAction(new StringTokenizer(action));
                        try {
                            this.sendEvents();
                        } catch (IOException e) {
                            this.crashed[teamToIndex(team)] = true;
                        }
                    });
                } else {
                    ds.discardMessage();
                }
                break;
            case COMMAND:
                String command = ds.readCommand();
                if (command.equals("save")) {
                    this.bufferedUpdates.put(this.b::saveBoardState);
                }
                if (command.equals("load")) {
                    System.out.println("BITES ZA DUSTO");
                    this.bufferedUpdates.put(() -> {
                        try {
                            this.b.loadBoardState();
                            for (DataStream outds : this.players) {
                                outds.sendCommand("reset");
                            }
                            this.sendEvents();
                        } catch (IOException e) {
                            this.crashed[teamToIndex(team)] = true;
                        }
                    });
                }
                break;
            case EMOTE:
                // forward to other player
                Emote emote = ds.readEmote();
                if (emote != null) {
                    this.players.get(this.teamToIndex(team * -1)).sendEmote(emote);
                }
                break;
            default:
                ds.discardMessage();
                break;
        }
    }

    private void sendTeamAssignments() throws IOException {
        for (int i = 0; i <= 1; i++) {
            this.players.get(i).sendTeamAssign(this.indexToTeam(i));
        }
    }

    private void sendEvents() throws IOException {
        String eventBurstString = this.b.retrieveEventBurstString();
        if (!eventBurstString.isEmpty()) {
            for (DataStream ds : this.players) {
                ds.sendEventBurstString(eventBurstString);
            }
        }
    }

    public int teamToIndex(int team) {
        return (team * this.teamMultiplier - 1) / -2;
    }

    public int indexToTeam(int index) {
        return ((index * -2) + 1) * this.teamMultiplier;
    }
}
