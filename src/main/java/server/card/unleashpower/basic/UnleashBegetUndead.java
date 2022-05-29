package server.card.unleashpower.basic;

import java.util.*;

import client.tooltip.*;
import org.newdawn.slick.geom.Vector2f;
import server.*;
import server.card.*;
import server.card.cardset.basic.shadowshaman.Skeleton;
import server.card.effect.*;
import server.card.effect.common.EffectLastWordsSummon;
import server.event.*;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class UnleashBegetUndead extends UnleashPowerText {
    public static final String NAME = "Beget Undead";
    public static final String DESCRIPTION = "Give an allied minion <b>Last Words</b>: summon a <b>Skeleton</b>. <b>Unleash</b> it. Then deal 1 damage to it.";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower(NAME, DESCRIPTION, "res/unleashpower/basic/begetundead.png",
            CRAFT, TRAITS, RARITY, 2, UnleashBegetUndead.class,
            new Vector2f(410, 460), 4,
            () -> List.of(Tooltip.UNLEASH, Tooltip.LASTWORDS, Skeleton.TOOLTIP));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onUnleashPre(Minion m) {
                String resolverDescription = "Give the unleashed minion <b>Last Words</b>: summon a <b>Skeleton</b>.";
                return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        EffectLastWordsSummon elws = new EffectLastWordsSummon(
                                "<b>Last Words</b>: summon a <b>Skeleton</b> (from <b>Beget Undead</b>).", new Skeleton(), 1);
                        this.resolve(b, rq, el, new AddEffectResolver(m, elws));
                    }
                });
            }

            @Override
            public ResolverWithDescription onUnleashPost(Minion m) {
                // we rely on this anonymous fuckery in case if the effect gets copied
                Effect effect = this; // anonymous fuckery
                String resolverDescription = "Deal 1 damage to the unleashed minion.";
                return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, new DamageResolver(effect, m, 1, true, null));
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
