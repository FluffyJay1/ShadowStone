package server;

import client.Game;
import network.DataStream;
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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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

    public GameController(List<DataStream> players, List<LeaderText> leaders, List<UnleashPowerText> unleashPowers, List<ConstructedDeck> decks) {
        this.b = new ServerBoard(1);
        this.players = players;
        this.leaders = leaders;
        this.unleashPowers = unleashPowers;
        this.decks = decks;
    }

    public void startInit() {
        Resolver startResolver = new Resolver(false) {
            @Override
            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                for (int i = 0; i <= 1; i++) { // deckbuilding 101
                    int team = indexToTeam(i);
                    List<Card> cards = decks.get(i).convertToCards(b);
                    List<Card> shuffledCards = Game.selectRandom(cards, cards.size());
                    List<Integer> inds = new ArrayList<>(shuffledCards.size());
                    for (int j = 0; j < shuffledCards.size(); j++) {
                        inds.add(j);
                    }
                    b.processEvent(rq, null,
                            new EventCreateCard(shuffledCards, inds, team, CardStatus.DECK, CardVisibility.NONE));
                    UnleashPower up = unleashPowers.get(i).constructInstance(b);
                    b.processEvent(rq, null,
                            new EventCreateCard(List.of(up), List.of(0), team, CardStatus.UNLEASHPOWER));
                    b.processEvent(rq, null,
                            new EventCreateCard(List.of(leaders.get(i).constructInstance(b)), List.of(0), team, CardStatus.LEADER));
                }
            }
        };
        this.b.resolve(startResolver, 0);
        this.sendEvents();
    }

    public void startGame() {
        this.b.resolve(new DrawResolver(this.b.player1, 3), 0);
        this.b.resolve(new DrawResolver(this.b.player2, 4), 0);
        this.sendEvents();
    }

    public boolean isGamePhase() {
        return this.b.winner == 0;
    }

    public void updateGame() {
        for (int i = 0; i <= 1; i++) {
            this.handleGameInput(this.players.get(i), indexToTeam(i));
        }
    }

    // TODO
    public void end() {

    }

    public void resolve(Resolver r, int team) {
        this.b.resolve(r, team);
        this.sendEvents();
    }

    public int getWinner() {
        return this.b.winner;
    }

    private void handleGameInput(DataStream ds, int team) {
        while (ds.ready()) {
            MessageType mtype = ds.receive();
            switch (mtype) {
                case PLAYERACTION:
                    if (this.b.currentPlayerTurn != team * -1) {
                        String action = ds.readPlayerAction();
                        System.out.println("team " + team + " action: " + action);
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
                        for (DataStream outds : this.players) {
                            outds.sendResetBoard();
                        }
                        this.sendEvents();
                    }
                    break;
                default:
                    ds.discardMessage();
                    break;
            }
        }
    }

    private void sendEvents() {
        String eventBurstString = this.b.retrieveEventBurstString();
        if (!eventBurstString.isEmpty()) {
            this.sendEventBurst(1, eventBurstString);
            this.sendEventBurst(-1, eventBurstString);
        }
    }

    private void sendEventBurst(int team, String eventBurstString) {
        this.players.get(teamToIndex(team)).sendEventBurstString(eventBurstString);
    }

    private static int teamToIndex(int team) {
        return (team - 1) / -2;
    }

    private static int indexToTeam(int index) {
        return (index * -2) + 1;
    }
}
