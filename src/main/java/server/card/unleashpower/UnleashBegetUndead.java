package server.card.unleashpower;

import java.util.*;

import client.tooltip.*;
import org.newdawn.slick.geom.Vector2f;
import server.*;
import server.card.*;
import server.card.cardpack.basic.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class UnleashBegetUndead extends UnleashPower {
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower("Beget Undead",
            "Give an allied minion <b>Last Words</b>: summon a <b>Skeleton</b>. <b>Unleash</b> it. Then deal 1 damage to it.",
            "res/unleashpower/begetundead.png", CRAFT, 2, UnleashBegetUndead.class, new Vector2f(410, 460), 4,
            () -> List.of(Tooltip.UNLEASH, Tooltip.LASTWORDS, Skeleton.TOOLTIP));

    public UnleashBegetUndead(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(TOOLTIP.description) {
            @Override
            public Resolver onUnleashPre(Minion m) {
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
                        EffectLastWordsSummon elws = new EffectLastWordsSummon(
                                "<b>Last Words</b>: summon a <b>Skeleton</b> (from <b>Beget Undead</b>).", Skeleton.class, 1);
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
                    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
                        this.resolve(b, rl, el, new EffectDamageResolver(effect, m, 1, true, null));
                    }
                };
            }
        };
        this.addEffect(true, e);
    }
}
