package server.card.cardset.basic.forestrogue;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.common.EffectStatChange;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class SukunaBraveAndSmall extends MinionText {
    public static final String NAME = "Sukuna, Brave and Small";
    public static final String DESCRIPTION = "<b>Unleash</b>: Gain +2/+1/+2. If at least 3 cards were played this turn, gain an additional +2/+1/+2.";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/sukunabraveandsmall.png",
            CRAFT, TRAITS, RARITY, 2, 2, 1, 2, false, SukunaBraveAndSmall.class,
            new Vector2f(160, 160), 1.2, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.UNLEASH));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        Effect buff = new EffectStatChange("+2/+1/+2 (from <b>Unleash</b>).", 2, 1, 2);
                        this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                        if (owner.player.cardsPlayedThisTurn >= 3) {
                            this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                        }
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.valueForBuff(2, 1, 2) / 2;
            }

            @Override
            public boolean unleashSpecialConditions() {
                return this.owner.player.cardsPlayedThisTurn >= 3;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
