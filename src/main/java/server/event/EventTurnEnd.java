package server.event;

import java.util.*;
import java.util.stream.Collectors;

import client.Game;
import server.*;
import server.card.Minion;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.resolver.AddEffectResolver;

public class EventTurnEnd extends Event {
    public static final int ID = 14;
    public final Player p;
    private List<Effect> addedEffects;

    public EventTurnEnd(Player p) {
        super(ID);
        this.p = p;
    }

    @Override
    public void resolve(Board b) {
        // unfreeze
        this.addedEffects = new ArrayList<>(p.getPlayArea().size() + 1);
        b.getMinions(p.team, true, true)
                .filter(Minion::shouldBeUnfrozen)
                .forEach(m -> {
                    Effect unfreeze = new Effect("", EffectStats.builder()
                            .set(Stat.FROZEN, 0)
                            .build());
                    m.addEffect(false, unfreeze);
                    this.addedEffects.add(unfreeze);
                });
    }

    @Override
    public void undo(Board b) {
        for (Effect e : this.addedEffects) {
            e.owner.removeEffect(e, true);
        }
    }

    @Override
    public String toString() {
        return this.id + " " + this.p.team + Game.EVENT_END;
    }

    public static EventTurnEnd fromString(Board b, StringTokenizer st) {
        int team = Integer.parseInt(st.nextToken());
        Player p = b.getPlayer(team);
        return new EventTurnEnd(p);
    }

    @Override
    public boolean conditions() {
        return true;
    }
}
