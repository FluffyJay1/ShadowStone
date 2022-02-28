package server.card.unleashpower.basic;

import java.util.*;

import client.tooltip.*;
import org.newdawn.slick.geom.Vector2f;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;
import server.resolver.util.ResolverQueue;

public class UnleashFeedFervor extends UnleashPowerText {
    public static final String NAME = "Feed Fervor";
    public static final String DESCRIPTION = "<b>Unleash</b> an allied minion. If <b>Overflow</b> is active for you, this costs 1 less.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower(NAME, DESCRIPTION, "res/unleashpower/feedfervor.png",
            CRAFT, RARITY, 2, UnleashFeedFervor.class,
            new Vector2f(420, 210), 8,
            () -> List.of(Tooltip.UNLEASH, Tooltip.OVERFLOW));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            boolean overflow;

            @Override
            public Resolver onListenEvent(Event e) {
                Effect effect = this; // anonymous fuckery
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        if (e instanceof EventManaChange) {
                            Player p = b.getPlayer(effect.owner.team);
                            if (!overflow && p.overflow()) {
                                this.resolve(b, rq, el, new UpdateEffectStateResolver(effect, () -> overflow = true));
                                EffectStats esc = new EffectStats();
                                esc.change.setStat(EffectStats.COST, -1);
                                this.resolve(b, rq, el, new SetEffectStatsResolver(effect, esc));
                            }
                            if (overflow && !p.overflow()) {
                                this.resolve(b, rq, el, new UpdateEffectStateResolver(effect, () -> overflow = false));
                                EffectStats esc = new EffectStats();
                                esc.change.setStat(EffectStats.COST, 0);
                                this.resolve(b, rq, el, new SetEffectStatsResolver(effect, esc));
                            }
                        }
                    }
                };
            }

            @Override
            public String extraStateString() {
                return this.overflow + " ";
            }

            @Override
            public void loadExtraState(Board b, StringTokenizer st) {
                this.overflow = Boolean.parseBoolean(st.nextToken());

            }
        });
    }

    @Override
    public TooltipUnleashPower getTooltip() {
        return TOOLTIP;
    }
}
