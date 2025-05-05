package server.card.cardset.standard.bloodwarlock;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageMagicHit;
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
import server.resolver.DrawResolver;
import server.resolver.Resolver;
import server.resolver.RestoreResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class DireBond extends AmuletText {
    public static final String NAME = "Dire Bond";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Deal 6 damage to your leader.";
    private static final String ONTURNSTART_DESCRIPTION = "At the start of your turn, restore 2 health to your leader and draw a card.";
    public static final String DESCRIPTION = "<b>Countdown(3)</b>.\n" + BATTLECRY_DESCRIPTION + "\n" + ONTURNSTART_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, () -> new Animation("card/standard/direbond.png"),
            CRAFT, TRAITS, RARITY, 3, DireBond.class,
            new Vector2f(150, 140), 1.3,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.BATTLECRY),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.COUNTDOWN, 3)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        owner.player.getLeader().ifPresent(l -> {
                            this.resolve(b, rq, el, new DamageResolver(effect, l, 6, true, new EventAnimationDamageMagicHit()));
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_DAMAGE * -6;
            }

            @Override
            public ResolverWithDescription onTurnStartAllied() {
                Effect effect = this;
                return new ResolverWithDescription(ONTURNSTART_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        owner.player.getLeader().ifPresent(l -> {
                            this.resolve(b, rq, el, new RestoreResolver(effect, l, 2));
                            this.resolve(b, rq, el, new DrawResolver(owner.player, 1));
                        });
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND * 3 + AI.VALUE_PER_HEAL * 6;
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
