package server.playeraction;

import java.util.*;

import server.*;
import server.card.*;
import server.resolver.*;

public class OrderAttackAction extends PlayerAction {

    public static final int ID = 3;

    public final Minion attacker;
    public final Minion victim;

    public OrderAttackAction(Minion attacker, Minion victim) {
        super(ID);
        // TODO Auto-generated constructor stub
        this.attacker = attacker;
        this.victim = victim;
    }

    @Override
    public ResolutionResult perform(ServerBoard b) {
        ResolutionResult result = new ResolutionResult();
        if (this.attacker.canAttack(this.victim) && this.attacker.canAttack()) {
            result.concat(b.resolve(new MinionAttackResolver(this.attacker, this.victim)));
        }
        return result;
    }

    @Override
    public String toString() {
        return ID + " " + this.attacker.toReference() + this.victim.toReference() + "\n";
    }

    public static OrderAttackAction fromString(Board b, StringTokenizer st) {
        Minion attacker = (Minion) Card.fromReference(b, st);
        Minion victim = (Minion) Card.fromReference(b, st);
        return new OrderAttackAction(attacker, victim);
    }
}
