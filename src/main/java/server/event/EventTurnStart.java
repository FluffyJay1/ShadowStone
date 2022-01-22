package server.event;

import java.util.*;

import client.Game;
import server.*;
import server.card.*;

public class EventTurnStart extends Event {
    public static final int ID = 15;
    public final Player p;
    private int prevCurrentPlayerTurn;
    private int prevUnleashesThisTurn;
    private List<Boolean> prevSickness;
    private List<Integer> prevAttacks;

    public EventTurnStart(Player p) {
        super(ID);
        this.p = p;
    }

    @Override
    public void resolve() {
        this.prevCurrentPlayerTurn = this.p.board.currentPlayerTurn;
        this.prevUnleashesThisTurn = this.p.getUnleashPower().map(up -> up.unleashesThisTurn).orElse(0);
        this.prevSickness = new ArrayList<>();
        this.prevAttacks = new ArrayList<>();
        this.p.board.currentPlayerTurn = this.p.team;
        this.p.getUnleashPower().ifPresent(up -> up.unleashesThisTurn = 0);
        for (BoardObject b : this.p.getPlayArea()) {
            if (b instanceof Minion) {
                this.prevSickness.add(((Minion) b).summoningSickness);
                this.prevAttacks.add(((Minion) b).attacksThisTurn);
                ((Minion) b).summoningSickness = false;
                ((Minion) b).attacksThisTurn = 0;
            } else {
                this.prevSickness.add(false);
                this.prevAttacks.add(0);
            }
        }
    }

    @Override
    public void undo() {
        this.p.board.currentPlayerTurn = this.prevCurrentPlayerTurn;
        this.p.getUnleashPower().ifPresent(up -> up.unleashesThisTurn = this.prevUnleashesThisTurn);
        List<BoardObject> boardObjects = this.p.getPlayArea();
        for (int i = 0; i < boardObjects.size(); i++) {
            BoardObject bo = boardObjects.get(i);
            if (bo instanceof Minion) {
                Minion m = (Minion) bo;
                m.summoningSickness = this.prevSickness.get(i);
                m.attacksThisTurn = this.prevAttacks.get(i);
            }
        }
    }

    @Override
    public String toString() {
        return this.id + " " + this.p.team + Game.EVENT_END;
    }

    public static EventTurnStart fromString(Board b, StringTokenizer st) {
        int team = Integer.parseInt(st.nextToken());
        Player p = b.getPlayer(team);
        return new EventTurnStart(p);
    }

    @Override
    public boolean conditions() {
        return true;
    }
}
