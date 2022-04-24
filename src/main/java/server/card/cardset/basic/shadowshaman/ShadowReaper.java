package server.card.cardset.basic.shadowshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ai.AI;
import server.card.CardRarity;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.EffectUntilTurnEndEnemy;
import server.card.effect.common.EffectStatChange;
import server.card.target.TargetList;
import server.event.Event;
import server.event.EventDestroy;
import server.resolver.AddEffectResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class ShadowReaper extends MinionText {
    public static final String NAME = "Shadow Reaper";
    public static final String DESCRIPTION = "<b>Battlecry</b>: Gain <b>Stealth</b> until the end of your opponent's turn.\n" +
            "Whenever another allied minion is destroyed, gain +1/+0/+1.";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/shadowreaper.png",
            CRAFT, RARITY, 2, 1, 1, 1, true, ShadowReaper.class,
            new Vector2f(127, 125), 1.5, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.BATTLECRY, Tooltip.STEALTH));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                String resolverDescription = "<b>Battlecry</b>: Gain <b>Stealth</b> until the end of your opponent's turn.";
                Effect stealth = new EffectUntilTurnEndEnemy("<b>Stealth</b> until the end of opponent's turn (from <b>Battlecry</b>).",
                        EffectStats.builder()
                                .set(EffectStats.STEALTH, 1)
                                .build());
                return new ResolverWithDescription(resolverDescription, new AddEffectResolver(this.owner, stealth));
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_OF_STEALTH;
            }

            @Override
            public ResolverWithDescription onListenEvent(Event e) {
                if (e instanceof EventDestroy && this.owner.isInPlay()) {
                    EventDestroy ed = (EventDestroy) e;
                    int destroyed = (int) ed.cards.stream()
                            .filter(c -> c.team == this.owner.team && c != this.owner && c instanceof Minion)
                            .count();
                    if (destroyed > 0) {
                        String description = "Whenever another allied minion is destroyed, gain +1/+0/+1.";
                        Effect statGain = new EffectStatChange("+" + destroyed + "/+0/+" + destroyed + " (from allied minions being destroyed).", destroyed, 0, destroyed);
                        return new ResolverWithDescription(description, new AddEffectResolver(this.owner, statGain));
                    }
                }
                return null;
            }

            @Override
            public double getPresenceValue(int refs) {
                // suppose best case is full board dies
                return AI.valueForBuff(5, 0, 5) / 2.;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
