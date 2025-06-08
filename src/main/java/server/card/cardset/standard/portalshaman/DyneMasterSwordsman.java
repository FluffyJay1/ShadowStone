package server.card.cardset.standard.portalshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageAOESlice;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class DyneMasterSwordsman extends MinionText {
    public static final String NAME = "Dyne, Master Swordsman";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Deal 1 damage to all enemies. If the total cost of all Artifact cards in your graveyard is at least 20, deal 5 instead.";
    public static final String DESCRIPTION = "<b>Rush</b>.\n" + BATTLECRY_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.PORTALSHAMAN;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/dynemasterswordsman.png"),
            CRAFT, TRAITS, RARITY, 5, 4, 2, 5, true, DyneMasterSwordsman.class,
            new Vector2f(150, 140), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.RUSH, Tooltip.BATTLECRY),
            List.of(card -> String.format("(Sum of costs of Artifacts in graveyard: %d)",
                    card.player.getGraveyard().stream()
                            .filter(c -> c.finalTraits.contains(CardTrait.ARTIFACT))
                            .map(c -> c.finalBasicStats.get(Stat.COST)).reduce(0, Integer::sum))));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.RUSH, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int totalCost = owner.player.getGraveyard().stream()
                                .filter(c -> c.finalTraits.contains(CardTrait.ARTIFACT))
                                .map(c -> c.finalBasicStats.get(Stat.COST)).reduce(0, Integer::sum);
                        int damage = totalCost >= 20 ? 5 : 1;
                        List<Minion> targets = b.getMinions(owner.team * -1, true, true).toList();
                        this.resolve(b, rq, el, new DamageResolver(effect, targets, damage, true, new EventAnimationDamageAOESlice(owner.team * -1, true)));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_DAMAGE * 10; // idk
            }

            @Override
            public boolean battlecrySpecialConditions() {
                int totalCost = owner.player.getGraveyard().stream()
                        .filter(c -> c.finalTraits.contains(CardTrait.ARTIFACT))
                        .map(c -> c.finalBasicStats.get(Stat.COST)).reduce(0, Integer::sum);
                return totalCost >= 20;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
