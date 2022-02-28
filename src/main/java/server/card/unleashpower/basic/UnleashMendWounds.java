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

public class UnleashMendWounds extends UnleashPowerText {
    public static final String NAME = "Mend Wounds";
    public static final String DESCRIPTION = "Give an allied minion +0/+0/+1, <b>Unleash</b> it, then restore 1 health to it.";
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower(NAME, DESCRIPTION, "res/unleashpower/mendwounds.png",
            CRAFT, RARITY, 2, UnleashMendWounds.class,
            new Vector2f(655, 535), 4.6,
            () -> List.of(Tooltip.UNLEASH));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public Resolver onUnleashPre(Minion m) {
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        EffectStatChange e = new EffectStatChange("+0/+0/+1 (from <b>Mend Wounds</b>).", 0, 0, 1);
                        this.resolve(b, rq, el, new AddEffectResolver(m, e));
                    }
                };
            }

            @Override
            public Resolver onUnleashPost(Minion m) {
                return new RestoreResolver(this, m, 1);
            }
        });
    }

    @Override
    public TooltipUnleashPower getTooltip() {
        return TOOLTIP;
    }
}
