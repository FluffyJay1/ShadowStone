package server.card.cardset.basic.havenpriest;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.DestroyResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;
import java.util.stream.Collectors;

public class TribunalOfGoodAndEvil extends AmuletText {
    public static final String NAME = "Tribunal of Good and Evil";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Destroy a random enemy minion.";
    private static final String LASTWORDS_DESCRIPTION = "<b>Last Words</b>: Destroy a random enemy minion.";
    public static final String DESCRIPTION = "<b>Countdown(3)</b>.\n" + BATTLECRY_DESCRIPTION + "\n" + LASTWORDS_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, "card/basic/tribunalofgoodandevil.png",
            CRAFT, TRAITS, RARITY, 4, TribunalOfGoodAndEvil.class,
            new Vector2f(150, 150), 1.2,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.BATTLECRY, Tooltip.LASTWORDS),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.COUNTDOWN, 3)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> possibleTargets = b.getMinions(owner.team * -1, false, true).collect(Collectors.toList());
                        if (!possibleTargets.isEmpty()) {
                            Minion target = SelectRandom.from(possibleTargets);
                            this.resolve(b, rq, el, new DestroyResolver(target));
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_OF_DESTROY / 2;
            }

            @Override
            public ResolverWithDescription lastWords() {
                return new ResolverWithDescription(LASTWORDS_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> possibleTargets = b.getMinions(owner.team * -1, false, true).collect(Collectors.toList());
                        if (!possibleTargets.isEmpty()) {
                            Minion target = SelectRandom.from(possibleTargets);
                            this.resolve(b, rq, el, new DestroyResolver(target));
                        }
                    }
                });
            }

            @Override
            public double getLastWordsValue(int refs) {
                return AI.VALUE_OF_DESTROY / 2;
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
