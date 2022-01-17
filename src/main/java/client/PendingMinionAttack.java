package client;

import server.card.Minion;
import utils.PendingManager;

public class PendingMinionAttack {
    public Minion m1, m2;
    public PendingMinionAttack(Minion m1, Minion m2) {
        this.m1 = m1;
        this.m2 = m2;
    }

    public interface PendingMinionAttacker {
        PendingManager.Processor<PendingMinionAttack> getPendingMinionAttackProcessor();
    }
}
