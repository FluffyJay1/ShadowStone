package server.card.cardset.basic.bloodwarlock;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageClaw;
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

public class BloodWolf extends MinionText {
    public static final String NAME = "Blood Wolf";
    public static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Deal 2 damage to your leader.";
    public static final String DESCRIPTION = "<b>Storm</b>.\n" + BATTLECRY_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/basic/bloodwolf.png"),
            CRAFT, TRAITS, RARITY, 2, 3, 1, 1, true, BloodWolf.class,
            new Vector2f(168, 212), 1.5, new EventAnimationDamageClaw(),
            () -> List.of(Tooltip.STORM, Tooltip.BATTLECRY),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.STORM, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        owner.player.getLeader().ifPresent(l -> {
                            this.resolve(b, rq, el, new DamageResolver(effect, l, 2, true, new EventAnimationDamageClaw()));
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                // uh
                return -AI.VALUE_PER_DAMAGE * 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
