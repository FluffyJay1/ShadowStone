package server.playeraction;

import java.util.*;

import server.*;
import server.card.*;
import server.card.target.TargetList;
import server.resolver.*;

public class PlayCardAction extends PlayerAction {

    public static final int ID = 1;
    public final Player p;
    public final Card c;
    public final int pos;
    final List<List<TargetList<?>>> battlecryTargets;

    public PlayCardAction(Player p, Card c, int pos, List<List<TargetList<?>>> battlecryTargets) {
        super(ID);

        this.p = p;
        this.c = c;
        this.pos = pos;
        this.battlecryTargets = battlecryTargets;
    }

    // remember to set battlecry targets
    @Override
    public ResolutionResult perform(ServerBoard b) {
        ResolutionResult result = new ResolutionResult();
        if (!this.p.canPlayCard(this.c) || !this.c.validateTargets(this.c.getBattlecryTargetingSchemes(), this.battlecryTargets)) { // just to be safe
            return result;
        }
        result.concat(b.resolve(new PlayCardResolver(this.p, this.c, this.pos, this.battlecryTargets), this.p.team));
        return result;
    }

    @Override
    public String toString() {
        return ID + " " + this.p.team + " " + this.c.toReference() + this.pos + " " + Card.targetsToString(this.battlecryTargets); // YEAHH
    }

    public static PlayCardAction fromString(Board b, StringTokenizer st) {
        Player p = b.getPlayer(Integer.parseInt(st.nextToken()));
        Card c = Card.fromReference(b, st);
        int pos = Integer.parseInt(st.nextToken());
        assert c != null;
        List<List<TargetList<?>>> battlecryTargets = Card.targetsFromString(b, st);
        return new PlayCardAction(p, c, pos, battlecryTargets);
    }

}
