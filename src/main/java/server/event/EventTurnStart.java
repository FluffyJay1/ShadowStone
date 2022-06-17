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
    private int prevCardsPlayedThisTurn;
    private int prevTurnNum;
    private boolean prevCanUnleash;
    private List<Boolean> prevSickness;
    private List<Integer> prevAttacks;

    public EventTurnStart(Player p) {
        super(ID);
        this.p = p;
    }

    @Override
    public void resolve(Board b) {
        this.prevCurrentPlayerTurn = this.p.board.getCurrentPlayerTurn();
        this.prevUnleashesThisTurn = this.p.getUnleashPower().map(up -> up.unleashesThisTurn).orElse(0);
        this.prevCardsPlayedThisTurn = this.p.cardsPlayedThisTurn;
        this.prevSickness = new ArrayList<>();
        this.prevAttacks = new ArrayList<>();
        this.prevCanUnleash = this.p.unleashAllowed;
        this.prevTurnNum = this.p.turn;
        this.p.board.setCurrentPlayerTurn(this.p.team);
        this.p.getUnleashPower().ifPresent(up -> up.unleashesThisTurn = 0);
        this.p.cardsPlayedThisTurn = 0;
        this.p.turn++;
        if ((this.p.team == 1 && this.p.turn >= Player.UNLEASH_FIRST_TURN)
        || (this.p.team == -1 && this.p.turn >= Player.UNLEASH_SECOND_TURN)) {
            this.p.unleashAllowed = true;
        }
        for (BoardObject bo : this.p.getPlayArea()) {
            if (bo instanceof Minion) {
                this.prevSickness.add(((Minion) bo).summoningSickness);
                this.prevAttacks.add(((Minion) bo).attacksThisTurn);
                ((Minion) bo).summoningSickness = false;
                ((Minion) bo).attacksThisTurn = 0;
            } else {
                this.prevSickness.add(false);
                this.prevAttacks.add(0);
            }
        }
    }

    @Override
    public void undo(Board b) {
        b.setCurrentPlayerTurn(this.prevCurrentPlayerTurn);
        this.p.getUnleashPower().ifPresent(up -> up.unleashesThisTurn = this.prevUnleashesThisTurn);
        this.p.cardsPlayedThisTurn = this.prevCardsPlayedThisTurn;
        this.p.unleashAllowed = this.prevCanUnleash;
        this.p.turn = this.prevTurnNum;
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
