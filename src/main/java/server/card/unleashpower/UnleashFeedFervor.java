package server.card.unleashpower;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class UnleashFeedFervor extends UnleashPower {
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower("Feed Fervor",
            "<b> Unleash </b> an allied minion. If <b> Overflow </b> is active for you, this costs 1 less.",
            "res/unleashpower/feedfervor.png", CRAFT, 2, UnleashFeedFervor.class, Tooltip.UNLEASH, Tooltip.OVERFLOW);

    public UnleashFeedFervor(Board b) {
        super(b, TOOLTIP);
        Effect overflowDiscount = new Effect("When Overflow is active, this costs 1 less") {
            boolean overflow;

            @Override
            public Resolver onListenEvent(Event e) {
                Effect effect = this; // anonymous fuckery
                return new Resolver(false) {
                    @Override
                    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                        if (e instanceof EventManaChange) {
                            if (!overflow && p.overflow()) {
                                this.resolve(b, rl, el, new UpdateEffectStateResolver(effect, () -> overflow = true));
                                EffectStats esc = new EffectStats();
                                esc.change.setStat(EffectStats.COST, -1);
                                this.resolve(b, rl, el, new SetEffectStatsResolver(effect, esc));
                            }
                            if (overflow && !p.overflow()) {
                                this.resolve(b, rl, el, new UpdateEffectStateResolver(effect, () -> overflow = false));
                                EffectStats esc = new EffectStats();
                                esc.change.setStat(EffectStats.COST, 0);
                                this.resolve(b, rl, el, new SetEffectStatsResolver(effect, esc));
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
        };
        this.addEffect(true, overflowDiscount);
    }
}
