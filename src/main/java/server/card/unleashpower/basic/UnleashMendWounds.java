package server.card.unleashpower.basic;

import java.util.*;

import client.tooltip.*;
import org.newdawn.slick.geom.Vector2f;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.card.effect.common.EffectStatChange;
import server.event.*;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class UnleashMendWounds extends UnleashPowerText {
    public static final String NAME = "Mend Wounds";
    public static final String DESCRIPTION = "Give an allied minion +0/+0/+1, <b>Unleash</b> it, then restore 1 health to it.";
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower(NAME, DESCRIPTION, "res/unleashpower/basic/mendwounds.png",
            CRAFT, RARITY, 2, UnleashMendWounds.class,
            new Vector2f(655, 535), 4.6,
            () -> List.of(Tooltip.UNLEASH));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onUnleashPre(Minion m) {
                String resolverDescription = "Give the unleashed minion +0/+0/+1.";
                return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        EffectStatChange e = new EffectStatChange("+0/+0/+1 (from <b>Mend Wounds</b>).", 0, 0, 1);
                        this.resolve(b, rq, el, new AddEffectResolver(m, e));
                    }
                });
            }

            @Override
            public ResolverWithDescription onUnleashPost(Minion m) {
                String resolverDescription = "Restore 1 health to the unleashed minion.";
                return new ResolverWithDescription(resolverDescription, new RestoreResolver(this, m, 1));
            }
        });
    }

    @Override
    public TooltipUnleashPower getTooltip() {
        return TOOLTIP;
    }
}
