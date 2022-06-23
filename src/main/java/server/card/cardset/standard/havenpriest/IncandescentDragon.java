package server.card.cardset.standard.havenpriest;

import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageEnergyBeam;
import org.newdawn.slick.geom.Vector2f;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.event.EventMinionAttack;
import server.resolver.AddEffectResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class IncandescentDragon extends MinionText {
    public static final String NAME = "Incandescent Dragon";
    public static final String DESCRIPTION = "Whenever an enemy minion attacks, give it -2/-0/-0.";
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/standard/incandescentdragon.png",
            CRAFT, TRAITS, RARITY, 8, 8, 2, 6, true, IncandescentDragon.class,
            new Vector2f(142, 160), 1.3, new EventAnimationDamageEnergyBeam(),
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event event) {
                if (event instanceof EventMinionAttack) {
                    EventMinionAttack ema = (EventMinionAttack) event;
                    if (ema.m1.team != this.owner.team) {
                        Effect debuff = new Effect("-2/-0/-0 (from <b>Incandescent Dragon</b>).", EffectStats.builder()
                                .change(Stat.ATTACK, -2)
                                .build());
                        return new ResolverWithDescription(DESCRIPTION, new AddEffectResolver(ema.m1, debuff));
                    }
                }
                return null;
            }

            @Override
            public double getPresenceValue(int refs) {
                return -AI.valueForBuff(-2, 0, 0) * 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
