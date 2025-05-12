package server.card.cardset.basic.havenpriest;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
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
import server.resolver.Resolver;
import server.resolver.RestoreResolver;
import server.resolver.SpendResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class TenderRabbitHealer extends MinionText {
    public static final String NAME = "Tender Rabbit Healer";
    public static final String DESCRIPTION = "<b>Battlecry</b>: Restore 1 health to all allies. <b>Spend(6)</b> to gain +4/+2/+4 and restore 4 health instead.";
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/basic/tenderrabbithealer.png"),
            CRAFT, TRAITS, RARITY, 1, 1, 1, 1, true, TenderRabbitHealer.class,
            new Vector2f(130, 168), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.SPEND),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> healTargets = b.getMinions(owner.team, true, true).toList();
                        SpendResolver sr = this.resolve(b, rq, el, new SpendResolver(effect, 6, null));
                        if (sr.wasSuccessful()) {
                            Effect buff = new Effect("+4/+2/+4 (from <b>Battlecry</b>).", EffectStats.builder()
                                    .change(Stat.ATTACK, 4)
                                    .change(Stat.MAGIC, 2)
                                    .change(Stat.HEALTH, 4)
                                    .build());
                            this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                            this.resolve(b, rq, el, new RestoreResolver(effect, healTargets, 4));
                        } else {
                            this.resolve(b, rq, el, new RestoreResolver(effect, healTargets, 1));
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_HEAL * 3;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return owner.canSpendAfterPlayed(6);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
