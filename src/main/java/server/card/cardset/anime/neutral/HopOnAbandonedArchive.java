package server.card.cardset.anime.neutral;

import java.util.List;
import java.util.stream.Collectors;

import org.newdawn.slick.geom.Vector2f;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageFire;
import server.ServerBoard;
import server.ai.AI;
import server.card.AmuletText;
import server.card.Card;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.effect.common.EffectStatChange;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class HopOnAbandonedArchive extends AmuletText {
    public static final String NAME = "Hop on Abandoned Archive";
    private static final String ONTURNEND_DESCRIPTION = "<b>Countdown(5)</b>. At the end of your turn, give all minions in your hand +0/+1/+0.";
    public static final String DESCRIPTION = ONTURNEND_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, "card/anime/hoponabandonedarchive.png",
            CRAFT, TRAITS, RARITY, 3, HopOnAbandonedArchive.class,
            new Vector2f(143, 168), 1.3,
            () -> List.of(Tooltip.COUNTDOWN),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.COUNTDOWN, 5)
                .build()) {
            @Override
            public ResolverWithDescription onTurnEndAllied() {
                return new ResolverWithDescription(ONTURNEND_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Card> targets = owner.player.getHand().stream().filter(c -> c instanceof Minion).toList();
                        if (!targets.isEmpty()) {
                            Effect buff = new EffectStatChange("+0/+1/+0 (from <b>" + NAME + "</b>).", 0, 1, 0);
                            this.resolve(b, rq, el, new AddEffectResolver(targets, buff));
                        }
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.valueForBuff(0, 1, 0) * this.owner.player.getHand().stream().filter(c -> c instanceof Minion).count() / 2;
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
