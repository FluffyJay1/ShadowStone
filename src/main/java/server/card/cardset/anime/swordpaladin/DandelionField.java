package server.card.cardset.anime.swordpaladin;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageWind;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.PutCardResolver;
import server.resolver.Resolver;
import server.resolver.RestoreResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DandelionField extends AmuletText {
    public static final String NAME = "Dandelion Field";
    private static final String ONENTERPLAY_DESCRIPTION = "Whenever this card enters play, restore 5 health to all allies.";
    private static final String LISTENER_DESCRIPTION = "Whenever an enemy minion comes into play, if it has 1 or less health, return it to the opponent's hand, otherwise deal 1 damage to it.";
    private static final String ONTURNEND_DESCRIPTION = "At the end of your turn, restore 1 health to all allies.";
    public static final String DESCRIPTION = "<b>Countdown(3)</b>.\n" + ONENTERPLAY_DESCRIPTION + "\n" + LISTENER_DESCRIPTION + "\n" + ONTURNEND_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, () -> new Animation("card/anime/dandelionfield.png"),
            CRAFT, TRAITS, RARITY, 3, DandelionField.class,
            new Vector2f(), -1,
            () -> List.of(Tooltip.COUNTDOWN),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.COUNTDOWN, 3)
                .build()) {
            @Override
            public ResolverWithDescription onEnterPlay() {
                Effect effect = this;
                return new ResolverWithDescription(ONENTERPLAY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> alliedTargets = b.getMinions(owner.team, true, true).collect(Collectors.toList());
                        this.resolve(b, rq, el, new RestoreResolver(effect, alliedTargets, 5));
                    }
                });
            }

            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event event) {
                if (event != null) {
                    Effect effect = this;
                    Map<Boolean, List<Minion>> groups = event.cardsEnteringPlay().stream()
                            .filter(bo -> bo instanceof Minion && bo.team != owner.team)
                            .map(bo -> (Minion) bo)
                            .collect(Collectors.partitioningBy(m -> m.health <= 1));
                    List<Minion> minionsToReturn = groups.get(true);
                    List<Minion> minionsToDamage = groups.get(false);
                    if (!minionsToReturn.isEmpty()) {
                        List<Integer> pos = Collections.nCopies(minionsToReturn.size(), -1);
                        return new ResolverWithDescription(LISTENER_DESCRIPTION, new PutCardResolver(minionsToReturn, CardStatus.HAND, owner.team * -1, pos, true));
                    }
                    if (!minionsToDamage.isEmpty()) {
                        return new ResolverWithDescription(LISTENER_DESCRIPTION, new DamageResolver(effect, minionsToDamage, 1, true, new EventAnimationDamageWind()));
                    }
                }
                return null;
            }

            @Override
            public ResolverWithDescription onTurnEndAllied() {
                Effect effect = this;
                return new ResolverWithDescription(ONTURNEND_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> alliedTargets = b.getMinions(owner.team, true, true).collect(Collectors.toList());
                        this.resolve(b, rq, el, new RestoreResolver(effect, alliedTargets, 1));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_PER_HEAL * 3 + AI.valueOfMinionDamage(1) * 3;
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
