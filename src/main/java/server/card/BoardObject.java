package server.card;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.effect.*;
import server.resolver.*;

public class BoardObject extends Card {
    public int lastBoardPos = 0; // After leaving board (e.g. to graveyard), keep a record of where it was last

    public BoardObject(Board b, TooltipCard tooltip) {
        super(b, tooltip);
    }

    public boolean isInPlay() {
        return this.alive && this.status.equals(CardStatus.BOARD);
    }

    // don't look below
    public List<Resolver> lastWords() {
        List<Resolver> list = new LinkedList<>();
        for (Effect e : this.getFinalEffects(true)) {
            Resolver temp = e.lastWords();
            if (temp != null) {
                list.add(temp);
            }
        }
        return list;
    }

    public List<Resolver> onTurnStart() {
        List<Resolver> list = new LinkedList<>();
        for (Effect e : this.getFinalEffects(true)) {
            Resolver temp = e.onTurnStart();
            if (temp != null) {
                list.add(temp);
            }
        }
        return list;
    }

    public List<Resolver> onTurnEnd() {
        List<Resolver> list = new LinkedList<>();
        for (Effect e : this.getFinalEffects(true)) {
            Resolver temp = e.onTurnEnd();
            if (temp != null) {
                list.add(temp);
            }
        }
        return list;
    }

    public List<Resolver> onTurnStartEnemy() {
        List<Resolver> list = new LinkedList<>();
        for (Effect e : this.getFinalEffects(true)) {
            Resolver temp = e.onTurnStartEnemy();
            if (temp != null) {
                list.add(temp);
            }
        }
        return list;
    }

    public List<Resolver> onTurnEndEnemy() {
        List<Resolver> list = new LinkedList<>();
        for (Effect e : this.getFinalEffects(true)) {
            Resolver temp = e.onTurnEndEnemy();
            if (temp != null) {
                list.add(temp);
            }
        }
        return list;
    }

    public List<Resolver> onEnterPlay() {
        List<Resolver> list = new LinkedList<>();
        for (Effect e : this.getFinalEffects(true)) {
            Resolver temp = e.onEnterPlay();
            if (temp != null) {
                list.add(temp);
            }
        }
        return list;
    }

    public List<Resolver> onLeavePlay() {
        List<Resolver> list = new LinkedList<>();
        for (Effect e : this.getFinalEffects(true)) {
            Resolver temp = e.onLeavePlay();
            if (temp != null) {
                list.add(temp);
            }
        }
        return list;
    }

    @Override
    public String toString() {
        return super.toString() + this.lastBoardPos + " ";
    }
}
