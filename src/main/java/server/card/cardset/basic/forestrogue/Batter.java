package server.card.cardset.basic.forestrogue;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageOff;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.*;
import server.card.cardset.basic.shadowshaman.Spectre;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.event.Event;
import server.resolver.Resolver;
import server.resolver.TransformResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class Batter extends MinionText {
    public static final String NAME = "The Batter";
    public static final String DESCRIPTION = "<b>Rush</b>.\n<b>Minion Strike</b>: <b>Transform</b> the attacked minion into a <b>Spectre</b> instead.";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/batter.png",
            CRAFT, TRAITS, RARITY, 5, 2, 2, 2, true, Batter.class,
            new Vector2f(112, 120), 2, EventAnimationDamageOff.class,
            () -> List.of(Tooltip.RUSH, Tooltip.MINIONSTRIKE, Tooltip.TRANSFORM, Spectre.TOOLTIP));
    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(EffectStats.RUSH, 1)
                .build()
        ) {
            @Override
            public ResolverWithDescription minionStrike(Minion target) {
                String resolverDescription = "<b>Minion Strike</b>: <b>Transform</b> it into a <b>Spectre</b> instead.";
                return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        if (target.status.equals(CardStatus.BOARD)) {
                            this.resolve(b, rq, el, new TransformResolver(target, new Spectre()));
                        }
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                // can potentially reduce a 10 cost minion to a 2 cost
                return 8 / 2.;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
