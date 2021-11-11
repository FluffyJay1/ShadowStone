package server.card.unleashpower;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.cardpack.basic.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class UnleashBegetUndead extends UnleashPower {
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower("Beget Undead",
            "Give an allied minion <b> Last Words: </b> summon a <b> Skeleton. Unleash </b> it. Then deal 1 damage to it.",
            "res/unleashpower/begetundead.png", CRAFT, 2, UnleashBegetUndead.class, Tooltip.UNLEASH, Tooltip.LASTWORDS,
            Skeleton.TOOLTIP);

    public UnleashBegetUndead(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(
                "Gives an allied minion <b> Last Words: </b> summon a <b> Skeleton </b> pre-unleash, then deal 1 damage to it post-unleash.") {
            @Override
            public Resolver onUnleashPre(Minion m) {
                return new Resolver(false) {
                    @Override
                    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                        EffectLastWordsSummon elws = new EffectLastWordsSummon(
                                "Summons a <b> Skeleton </b> upon death.", Skeleton.class, m.team);
                        this.resolve(b, rl, el, new AddEffectResolver(m, elws));
                    }
                };
            }

            @Override
            public Resolver onUnleashPost(Minion m) {
                // we rely on this anonymous fuckery in case if the effect gets copied
                Effect effect = this; // anonymous fuckery
                return new Resolver(false) {
                    @Override
                    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                        this.resolve(b, rl, el, new EffectDamageResolver(effect, m, 1, true));
                    }
                };
            }
        };
        this.addEffect(true, e);
    }
}
