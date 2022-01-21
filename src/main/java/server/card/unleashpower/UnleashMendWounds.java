package server.card.unleashpower;

import java.util.*;

import client.tooltip.*;
import org.newdawn.slick.geom.Vector2f;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class UnleashMendWounds extends UnleashPower {
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower("Mend Wounds",
            "Give an allied minion +0/+0/+1, <b>Unleash</b> it, then restore 1 health to it.",
            "res/unleashpower/mendwounds.png", CRAFT, 2, UnleashMendWounds.class, new Vector2f(655, 535), 4.6,
            Tooltip.UNLEASH);

    public UnleashMendWounds(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(TOOLTIP.description) {
            @Override
            public Resolver onUnleashPre(Minion m) {
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
                        EffectStatChange e = new EffectStatChange("+0/+0/+1 (from <b>Mend Wounds</b>).", 0, 0, 1);
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
