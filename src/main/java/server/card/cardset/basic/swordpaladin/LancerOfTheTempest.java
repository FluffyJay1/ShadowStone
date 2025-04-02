package server.card.cardset.basic.swordpaladin;

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
import server.resolver.AddEffectResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.SpendResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class LancerOfTheTempest extends MinionText {
    public static final String NAME = "Lancer of the Tempest";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: <b>Spend(4)</b> to gain " + EffectLancerStrike.DESCRIPTION;
    public static final String DESCRIPTION = "<b>Rush</b>.\n" + BATTLECRY_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.COMMANDER);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/basic/lancerofthetempest.png"),
            CRAFT, TRAITS, RARITY, 3, 3, 1, 3, true, LancerOfTheTempest.class,
            new Vector2f(122, 198), 1.4, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.RUSH, Tooltip.BATTLECRY, Tooltip.SPEND, Tooltip.STRIKE),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.RUSH, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new SpendResolver(this, 4, new AddEffectResolver(this.owner, new EffectLancerStrike())));
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.valueOfMinionDamage(2) * 3 / 4;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.canSpendAfterPlayed(4);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }

    public static class EffectLancerStrike extends Effect {
        public static final String DESCRIPTION = "<b>Strike</b>: Deal 2 damage to all enemy minions.";

        public EffectLancerStrike() {
            super(DESCRIPTION);
        }

        @Override
        public ResolverWithDescription strike(Minion target) {
            Effect effect = this;
            return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                @Override
                public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                    List<Minion> targets = b.getMinions(owner.team * -1, false, true).collect(Collectors.toList());
                    this.resolve(b, rq, el, new DamageResolver(effect, targets, 2, true,
                            new EventAnimationDamageAOESlice(owner.team * -1, false).toString()));
                }
            });
        }

        @Override
        public double getPresenceValue(int refs) {
            return AI.valueOfMinionDamage(2) * 3;
        }
    }
}
