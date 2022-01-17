package client;

import server.Board;
import server.card.BoardObject;
import server.card.Card;
import utils.PendingListManager;
import utils.PendingManager;

import java.util.LinkedList;
import java.util.List;

/**
 * Version of the Board class that is meant to be a faithful client-side replica
 * of the server's board. Has extra things to enable interaction with the
 * animating version of the board (VisualBoard).
 */
public class ClientBoard extends Board implements
        PendingPlay.PendingPlayer, PendingPlayPositioner,
        PendingMinionAttack.PendingMinionAttacker, PendingUnleash.PendingUnleasher {
    // links cards created between board and visualboard
    public List<Card> cardsCreated;

    public PendingManager<PendingPlay> pendingPlays;
    public PendingListManager<BoardObject> pendingPlayPositions;
    public PendingManager<PendingMinionAttack> pendingMinionAttacks;
    public PendingManager<PendingUnleash> pendingUnleashes;

    public ClientBoard(int localteam, PendingManager<PendingPlay> pendingPlays,
                       PendingListManager<BoardObject> pendingPlayPositions,
                       PendingManager<PendingMinionAttack> pendingMinionAttacks,
                       PendingManager<PendingUnleash> pendingUnleashes) {
        super(localteam);
        this.pendingPlays = pendingPlays;
        this.pendingPlayPositions = pendingPlayPositions;
        this.pendingMinionAttacks = pendingMinionAttacks;
        this.pendingUnleashes = pendingUnleashes;
    }

    @Override
    public void init() {
        super.init();
        this.cardsCreated = new LinkedList<>();
        this.pendingPlayPositions = new PendingListManager<>();
    }

    @Override
    public PendingListManager.Processor<BoardObject> getPendingPlayPositionProcessor() {
        return this.pendingPlayPositions.getProducer();
    }

    @Override
    public PendingManager.Processor<PendingMinionAttack> getPendingMinionAttackProcessor() {
        return this.pendingMinionAttacks.getProducer();
    }

    @Override
    public PendingManager.Processor<PendingUnleash> getPendingUnleashProcessor() {
        return this.pendingUnleashes.getProducer();
    }

    @Override
    public PendingManager.Processor<PendingPlay> getPendingPlayProcessor() {
        return this.pendingPlays.getProducer();
    }
}
