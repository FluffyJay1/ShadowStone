package server.card.unleashpower.basic;

import java.util.*;

import client.tooltip.*;
import client.ui.Animation;

import org.newdawn.slick.geom.Vector2f;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.card.effect.common.EffectStatChange;
import server.event.*;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class UnleashSharpenSword extends UnleashPowerText {
    public static final String NAME = "Sharpen Sword";
    public static final String DESCRIPTION = "Give an allied minion +1/+0/+0, then <b>Unleash</b> it.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower(NAME, DESCRIPTION, () -> new Animation("unleashpower/basic/sharpensword.png"),
            CRAFT, TRAITS, RARITY, 2, UnleashSharpenSword.class,
            new Vector2f(500, 330), 3,
            () -> List.of(Tooltip.UNLEASH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onUnleashPre(Minion m) {
                String resolverDescription = "Give the unleashed minion +1/+0/+0.";
                return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        EffectStatChange e = new EffectStatChange("+1/+0/+0 (from <b>Sharpen Sword</b>).", 1, 0, 0);
                        this.resolve(b, rq, el, new AddEffectResolver(m, e));
                    }
                });
            }
        });
    }

    @Override
    public TooltipUnleashPower getTooltip() {
        return TOOLTIP;
    }
}
