package server.card.cardset.moba.bloodwarlock;

import java.util.List;

import org.newdawn.slick.geom.Vector2f;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSmallExplosion;
import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class Soldier extends MinionText {
    public static final String NAME = "Soldier";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Deal 3 damage to your leader, then gain +1/+1/+0 for every 3 health your leader is missing.";
    public static final String DESCRIPTION = "<b>Rush</b>. <b>Cleave</b>.\n" + BATTLECRY_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/moba/soldier.png"),
            CRAFT, TRAITS, RARITY, 6, 3, 1, 5, true, Soldier.class,
            new Vector2f(), -1, new EventAnimationDamageSmallExplosion(),
            () -> List.of(Tooltip.RUSH, Tooltip.CLEAVE, Tooltip.BATTLECRY),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.RUSH, 1)
                .set(Stat.CLEAVE, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        owner.player.getLeader().ifPresent(l -> {
                            this.resolve(b, rq, el, new DamageResolver(effect, l, 3, true, new EventAnimationDamageSmallExplosion().toString()));
                            int missing = l.finalStats.get(Stat.HEALTH) - l.health;
                            int buffAmount = missing / 3;
                            Effect buff = new Effect("+" + buffAmount + "/+" + buffAmount + "/+0 (from <b>Battlecry</b>).", EffectStats.builder()
                                    .change(Stat.ATTACK, buffAmount)
                                    .change(Stat.MAGIC, buffAmount)
                                    .build());
                            this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                int missing = owner.player.getLeader().map(l -> l.finalStats.get(Stat.HEALTH) - l.health).orElse(0);
                int buffAmount = missing / 3;
                return AI.valueForBuff(buffAmount, buffAmount, 0) - AI.VALUE_PER_DAMAGE * 3;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
