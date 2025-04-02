package server.card.cardset.standard.dragondruid;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.effect.common.EffectStatChange;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class Rahab extends MinionText {
    public static final String NAME = "Rahab";
    public static final String DESCRIPTION = "<b>Ward</b>.\nAt the end of your turn, gain +1/+0/+0 for each of your remaining mana orbs.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/rahab.png"),
            CRAFT, TRAITS, RARITY, 4, 2, 2, 6, true, Rahab.class,
            new Vector2f(151, 160), 1.2, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.WARD),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.WARD, 1)
                .build()) {
            @Override
            public ResolverWithDescription onTurnEndAllied() {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int amount = owner.player.mana;
                        if (amount > 0) {
                            Effect buff = new EffectStatChange("+" + amount + "/+0/+0 (from end of turn effect).", amount, 0, 0);
                            this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                        }
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                // idk how to evaluate this
                return AI.valueForBuff(1, 0, 0);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
