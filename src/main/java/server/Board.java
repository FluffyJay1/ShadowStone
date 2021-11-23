package server;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.event.eventgroup.EventGroup;
import server.playeraction.*;
import server.resolver.*;

public class Board {
    public boolean isServer = true; // true means it is the center of game logic
    public boolean isClient; // true means has a visualboard
    // links cards created between board and visualboard
    public List<Card> cardsCreated;

    public Player player1, player2;
    // localteam is the team of the player, i.e. at the bottom of the screen
    public int currentPlayerTurn, localteam, winner;

    protected List<BoardObject> player1side;
    protected List<BoardObject> player2side;
    protected List<Card> player1graveyard;
    protected List<Card> player2graveyard;
    public List<Card> banished;
    private List<Resolver> resolveList;

    // the hierarchy of groups we are under
    private List<EventGroup> eventGroups;

    StringBuilder output, history;

    public Board() {
        this.init();
    }

    // reset state
    public void init() {
        this.currentPlayerTurn = 1;
        this.localteam = 1;
        this.winner = 0;
        this.cardsCreated = new LinkedList<Card>();
        this.player1 = new Player(this, 1);
        this.player2 = new Player(this, -1);
        this.player1side = new ArrayList<BoardObject>();
        this.player2side = new ArrayList<BoardObject>();
        this.resolveList = new LinkedList<>();
        this.player1graveyard = new ArrayList<Card>();
        this.player2graveyard = new ArrayList<Card>();
        this.banished = new ArrayList<Card>();
        this.output = new StringBuilder();
        this.history = new StringBuilder();
        this.eventGroups = new LinkedList<>();
    }

    public Board(int localteam) {
        this();
        this.localteam = localteam;
    }

    public Player getPlayer(int team) {
        return team == 1 ? player1 : player2;
    }

    public List<Card> getTargetableCards() {
        List<Card> ret = new ArrayList<Card>();
        ret.addAll(this.getBoardObjects());
        ret.addAll(this.player1.hand.cards);
        ret.addAll(this.player2.hand.cards);
        return ret;
    }

    // cards that can be added to a Target object
    public List<Card> getTargetableCards(Target t) {
        List<Card> list = new LinkedList<Card>();
        if (t == null) {
            return list;
        }
        for (Card c : this.getTargetableCards()) {
            if (t.canTarget(c)) {
                list.add(c);
            }
        }
        return list;
    }

    public List<Card> getCards() {
        List<Card> ret = new ArrayList<Card>();
        ret.addAll(this.getBoardObjects());
        ret.addAll(this.player1.hand.cards);
        ret.addAll(this.player1.deck.cards);
        ret.addAll(this.player2.hand.cards);
        ret.addAll(this.player2.deck.cards);
        ret.addAll(this.player1graveyard);
        ret.addAll(this.player2graveyard);
        if (this.player1.unleashPower != null) {
            ret.add(this.player1.unleashPower);
        }
        if (this.player2.unleashPower != null) {
            ret.add(this.player2.unleashPower);
        }
        return ret;
    }

    // i don't even know what this is
    public List<Card> getCollection(int team, CardStatus status) {
        switch (status) {
        case HAND:
            return this.getPlayer(team).hand.cards;
        case BOARD:
            List<Card> cards = new ArrayList<Card>();
            cards.addAll(this.getBoardObjects(team)); // gottem
            return cards;
        case DECK:
            return this.getPlayer(team).deck.cards;
        case GRAVEYARD:
            return this.getGraveyard(team);
        case UNLEASHPOWER:
            List<Card> cards2 = new ArrayList<Card>();
            cards2.add(this.getPlayer(team).unleashPower);
            return cards2;
        case LEADER:
            List<Card> cards3 = new ArrayList<Card>();
            cards3.add(this.getPlayer(team).leader);
            return cards3;
        default:
            return null;
        }
    }

    // with leaders
    public List<BoardObject> getBoardObjects() {
        ArrayList<BoardObject> ret = new ArrayList<BoardObject>();
        ret.addAll(this.player1side);
        ret.addAll(this.player2side);
        if (this.player1.leader != null) {
            ret.add(this.player1.leader);
        }
        if (this.player2.leader != null) {
            ret.add(this.player2.leader);
        }
        return ret;
    }

    // no leader
    public List<BoardObject> getBoardObjects(int team) {
        if (team > 0) {
            return this.player1side;
        } else {
            return this.player2side;
        }
    }

    public List<BoardObject> getBoardObjects(int team, boolean leader, boolean minion, boolean amulet) {
        ArrayList<BoardObject> ret = new ArrayList<>();
        if (leader && this.getPlayer(team).leader != null) {
            ret.add(this.getPlayer(team).leader);
        }
        if (team >= 0) {
            ret.addAll(this.player1side);
        }
        if (team <= 0) {
            ret.addAll(this.player2side);
        }
        if (!minion) {
            for (int i = 0; i < ret.size(); i++) {
                if (ret.get(i) instanceof Minion) {
                    ret.remove(i); // don't worry about this
                    i--;
                }
            }
        }
        if (!amulet) {
            for (int i = 0; i < ret.size(); i++) {
                if (ret.get(i) instanceof Amulet) {
                    ret.remove(i);
                    i--;
                }
            }
        }
        return ret;
    }

    public List<Minion> getMinions(int team, boolean leader, boolean health) {
        ArrayList<Minion> ret = new ArrayList<>();
        List<BoardObject> relevantSide = this.getBoardObjects(team);
        for (BoardObject bo : relevantSide) {
            if (bo instanceof Minion && (!health || ((Minion) bo).health > 0)) {
                ret.add((Minion) bo);
            }
        }
        if (leader && (!health || this.getPlayer(team).leader.health > 0)) {
            ret.add(this.getPlayer(team).leader);
        }
        return ret;
    }

    // returns null if out of bounds
    public BoardObject getBoardObject(int team, int position) {
        List<BoardObject> relevantSide = team > 0 ? player1side : player2side;
        if (position >= relevantSide.size() || position < 0) {
            return null;
        }
        return relevantSide.get(position);
    }

    public void addBoardObject(BoardObject bo, int team, int position) {
        List<BoardObject> relevantSide = team > 0 ? player1side : player2side;
        if (position > relevantSide.size()) {
            position = relevantSide.size();
        }
        bo.cardpos = position;
        bo.status = CardStatus.BOARD;
        bo.team = team;
        relevantSide.add(position, bo);
        for (int i = position + 1; i < relevantSide.size(); i++) {
            relevantSide.get(i).cardpos++;
        }
    }

    public void addBoardObjectToSide(BoardObject bo, int team) {
        bo.team = team;
        if (team > 0) {
            addBoardObject(bo, 1, player1side.size());
        }
        if (team < 0) {
            addBoardObject(bo, -1, player2side.size());
        }
    }

    public void removeBoardObject(BoardObject bo) {
        if (this.player1side.contains(bo)) {
            this.player1side.remove(bo);
        }
        if (this.player2side.contains(bo)) {
            this.player2side.remove(bo);
        }
        this.updatePositions();
    }

    public void removeBoardObject(int team, int position) {
        BoardObject bo;
        List<BoardObject> relevantSide = team > 0 ? player1side : player2side;
        if (position >= relevantSide.size()) {
            return;
        }
        bo = relevantSide.remove(position);
        for (int i = position; i < relevantSide.size(); i++) {
            relevantSide.get(i).cardpos--;
        }
    }

    public void updatePositions() {
        for (int i = 0; i < player1side.size(); i++) {
            player1side.get(i).cardpos = i;
        }
        for (int i = 0; i < player2side.size(); i++) {
            player2side.get(i).cardpos = i;
        }
    }

    public List<Card> getGraveyard(int team) {
        if (team == 1) {
            return this.player1graveyard;
        } else {
            return this.player2graveyard;
        }
    }

    // TODO: optimize by caching the result
    public List<Effect> getEffects() {
        List<Effect> effects = new LinkedList<>();
        for (Card c : this.getCards()) {
            effects.addAll(c.getEffects(true));
            effects.addAll(c.getEffects(false));
        }
        return effects;
    }

    public String stateToString() {
        StringBuilder builder = new StringBuilder();
        builder.append("State----------------------------+\n");
        builder.append("player turn: ");
        builder.append(this.currentPlayerTurn);
        builder.append(", winner: ");
        builder.append(this.winner);
        builder.append("\n");
        builder.append(this.player1.toString()).append("\n");
        builder.append(this.player2.toString()).append("\n");
        for (Card c : this.getCards()) {
            builder.append(c.toString() + "\n");
        }
        builder.append("---------------------------------+\n");
        return builder.toString();
    }

    // quality helper methods
    public ResolutionResult resolve(Resolver r) {
        List<Event> l = new LinkedList<>();
        r.onResolve(this, this.resolveList, l);
        ResolutionResult result = new ResolutionResult(l, r.rng);
        result.concat(this.resolveAll());
        return result;
    }

    public ResolutionResult resolveAll() {
        return this.resolveAll(this.resolveList);
    }

    // Only used by server, i.e. isServer == true
    public ResolutionResult resolveAll(List<Resolver> resolveList) {
        List<Event> l = new LinkedList<>();
        ResolutionResult result = new ResolutionResult(l, false);
        while (!resolveList.isEmpty()) {
            if (this.winner != 0) {
                break;
            }
            Resolver r = resolveList.remove(0);
            r.onResolve(this, resolveList, l);
            if (r.rng) {
                result.rng = true;
            }
        }
        return result;
    }

    /*
     * Process an event, resolving it, alerting listeners, and registering it in the
     * board's history of events
     * 
     * rl is the resolver list to enqueue listener responses to
     * 
     * el is a parameter for a cheeky space-saving measure
     * 
     * in summary don't add the return value to el, the method does it for you
     * 
     */
    public <T extends Event> T processEvent(List<Resolver> rl, List<Event> el, T e) {
        if (this.winner != 0 || !e.conditions()) {
            return e;
        }
        EventGroup eg = this.peekEventGroup();
        if (eg != null) {
            if (eg.numChildren == 0) {
                // commit this event group to output if it isn't empty
                this.output.append(eg.toString());
                this.history.append(eg.toString());
            }
            eg.numChildren++;
        }
        String eventString = e.toString();
        e.resolve();
        if (el != null) {
            el.add(e);
        }
        // TODO make the AI board not do this
        if (!eventString.isEmpty() && e.send) {
            this.output.append(eventString);
            this.history.append(eventString);
        }
        if (rl != null) {
            if (e.cardsEnteringPlay() != null) {
                for (BoardObject bo : e.cardsEnteringPlay()) {
                    rl.add(new FlagResolver(bo, bo.onEnterPlay()));
                }
            }
            if (e.cardsLeavingPlay() != null) {
                for (BoardObject bo : e.cardsLeavingPlay()) {
                    rl.addAll(bo.onLeavePlay());
                }
            }
            for (Card c : this.getCards()) {
                List<Resolver> listenEventResolvers = new LinkedList<>();
                for (Effect listener : c.listeners) {
                    listenEventResolvers.add(listener.onListenEvent(e));
                }
                rl.add(new FlagResolver(c, listenEventResolvers));
            }
        }
        return e;
    }

    public String retrieveEventString() {
        String temp = this.output.toString();
        this.output.delete(0, this.output.length());
        return temp;
    }

    public String getHistory() {
        return this.history.toString();
    }

    public void saveBoardState() {
        File f = new File("board.dat");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            PrintWriter pw = new PrintWriter(f);
            pw.print(this.getHistory());
            pw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void loadBoardState() {
        File f = new File("board.dat");
        if (f.exists()) {
            try {
                this.init();
                String state = new String(Files.readAllBytes(f.toPath()));
                this.parseEventString(state);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            try {
                f.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    
    /**
     * Parses a set of events/eventgroups and applies their changes to the board state
     * @param s The string to parse
     */
    public synchronized void parseEventString(String s) {
        if (!s.isEmpty()) {
            String[] lines = s.split("\n");
            for (String line : lines) {
                StringTokenizer st = new StringTokenizer(line);
                char firstChar = line.charAt(0);
                if (firstChar == EventGroup.PUSH_TOKEN) {
                    this.pushEventGroup(EventGroup.fromString(this, st));
                } else if (firstChar == EventGroup.POP_TOKEN) {
                    this.popEventGroup();
                } else {
                    Event e = Event.createFromString(this, st);
                    this.processEvent(null, null, e);
                }
            }
        }
    }

    public void pushEventGroup(EventGroup group) {
        this.eventGroups.add(group);
        // don't commit to output just yet, we don't know if this group is empty or not
    }

    public EventGroup popEventGroup() {
        EventGroup eg = this.eventGroups.remove(this.eventGroups.size() - 1);
        if (eg.numChildren > 0) {
            this.output.append(EventGroup.POP_TOKEN + "\n");
            this.history.append(EventGroup.POP_TOKEN + "\n");

            // if this wasn't empty, we consider this group as a legitimate child
            EventGroup parent = this.peekEventGroup();
            if (parent != null) {
                parent.numChildren++;
            }
        }
        return eg;
    }

    public EventGroup peekEventGroup() {
        if (this.eventGroups.isEmpty()) {
            return null;
        }
        return this.eventGroups.get(this.eventGroups.size() - 1);
    }

    public ResolutionResult executePlayerAction(StringTokenizer st) {
        PlayerAction pa = PlayerAction.createFromString(this, st);
        return pa.perform(this);
    }

    public ResolutionResult endCurrentPlayerTurn() {
        ResolutionResult result = this.resolve(new TurnEndResolver(this.getPlayer(this.currentPlayerTurn)));
        result.concat(this.resolve(new TurnStartResolver(this.getPlayer(this.currentPlayerTurn * -1))));
        return result;
    }
}
