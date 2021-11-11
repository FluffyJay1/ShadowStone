package server.card.unleashpower;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class UnleashMendWounds extends UnleashPower {
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower("Mend Wounds",
            "Give an allied minion +0/+0/+1, <b> Unleash </b> it, then restore 1 health to it.",
            "res/unleashpower/mendwounds.png", CRAFT, 2, UnleashMendWounds.class, Tooltip.UNLEASH);

    public UnleashMendWounds(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect("Gives a minion +0/+0/+1 pre-unleash, then restores 1 health to it post-unleash.") {
            @Override
            public Resolver onUnleashPre(Minion m) {
                return new Resolver(false) {
                    @Override
                    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                        EffectStatChange e = new EffectStatChange("+0/+0/+1 from <b> Mend Wounds. </b>", 0, 0, 1);
                        this.resolve(b, rl, el, new AddEffectResolver(m, e));
                    }
                };
            }

            @Override
            public Resolver onUnleashPost(Minion m) {
                return new RestoreResolver(this, m, 1);
            }
        };
        this.addEffect(true, e);
    }
}
