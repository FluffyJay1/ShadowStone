package server.card.cardset.standard.shadowshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.effect.common.EffectStatChange;
import server.card.target.TargetList;
import server.event.Event;
import server.event.EventDestroy;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class ShadowReaper extends MinionText {
    public static final String NAME = "Shadow Reaper";
    public static final String DESCRIPTION = "<b>Battlecry</b>: Gain <b>Stealth</b> until the end of your opponent's turn.\n" +
            "Whenever another allied minion is destroyed, gain +1/+0/+1.";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/standard/shadowreaper.png",
            CRAFT, TRAITS, RARITY, 2, 1, 1, 1, true, ShadowReaper.class,
            new Vector2f(127, 125), 1.5, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.STEALTH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                String resolverDescription = "<b>Battlecry</b>: Gain <b>Stealth</b> until the end of your opponent's turn.";
                Effect stealth = new Effect("<b>Stealth</b> until the end of opponent's turn (from <b>Battlecry</b>).",
                        EffectStats.builder()
                                .set(Stat.STEALTH, 1)
                                .build(),
                        e -> e.untilTurnEndTeam = -1);
                return new ResolverWithDescription(resolverDescription, new AddEffectResolver(this.owner, stealth));
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_OF_STEALTH;
            }

            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event e) {
                if (e instanceof EventDestroy) {
                    EventDestroy ed = (EventDestroy) e;
                    int destroyed = (int) ed.cards.stream()
                            .filter(c -> c.team == this.owner.team && c != this.owner && c instanceof Minion)
                            .count();
                    if (destroyed > 0) {
                        String description = "Whenever another allied minion is destroyed, gain +1/+0/+1.";
                        Effect statGain = new EffectStatChange("+1/+0/+1 (from allied minions being destroyed).", 1, 0, 1);
                        return new ResolverWithDescription(description, new Resolver(false) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                for (int i = 0; i < destroyed; i++) {
                                    this.resolve(b, rq, el, new AddEffectResolver(owner, statGain));
                                }
                            }
                        });
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
