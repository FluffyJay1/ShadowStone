package server.playeraction;

import java.util.*;

import server.*;
import server.card.*;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.resolver.*;

public class UnleashMinionAction extends PlayerAction {

    public static final int ID = 2;

    public final Player p;
    public final Minion m;
    final List<TargetList<?>> unleashTargets;

    public UnleashMinionAction(Player p, Minion m, List<TargetList<?>> unleashTargets) {
        super(ID);
        // TODO Auto-generated constructor stub
        this.p = p;
        this.m = m;
        this.unleashTargets = unleashTargets;
    }

    // remember to set targets to unleash upon
    @Override
    public ResolutionResult perform(ServerBoard b) {
        ResolutionResult result = new ResolutionResult();
        if (!p.canUnleashCard(m) || !this.m.validateTargets(this.m.getUnleashTargetingSchemes(), this.unleashTargets)) {
            return result;
        }
        // get special unleash power thing
        result.concat(b.resolve(new UnleashResolver(this.p.getUnleashPower().orElse(null), this.m, this.unleashTargets)));
        return result;
    }

    @Override
    public String toString() {
        return ID + " " + p.team + " " + m.toReference() + this.m.unleashTargetsToString(this.unleashTargets) + "\n";
    }

    public static UnleashMinionAction fromString(Board b, StringTokenizer st) {
        Player p = b.getPlayer(Integer.parseInt(st.nextToken()));
        Minion m = (Minion) Card.fromReference(b, st);
        assert m != null;
        List<TargetList<?>> unleashTargets = m.parseUnleashTargets(st);
        return new UnleashMinionAction(p, m, unleashTargets);
    }

}
