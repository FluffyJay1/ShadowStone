package server.card.cardset.anime.shadowshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.RestoreResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class HeraldOfFrost extends AmuletText {
    public static final String NAME = "Herald of Frost";
    private static final String ONTURNEND_DESCRIPTION = "At the end of each player's turn, <b>Freeze</b> a random enemy minion that isn't already <b>Frozen</b>, " +
            "and restore 1 health to all allies.";
    public static final String DESCRIPTION = "<b>Countdown(1)</b>.\n" + ONTURNEND_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, "card/anime/heraldoffrost.png",
            CRAFT, TRAITS, RARITY, 3, HeraldOfFrost.class,
            new Vector2f(), -1,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.FROZEN),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.COUNTDOWN, 1)
                .build()) {
            Effect effect = this;
            Supplier<ResolverWithDescription> turnend = () -> new ResolverWithDescription(ONTURNEND_DESCRIPTION, new Resolver(false) {
                @Override
                public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                    Effect freezer = new Effect("", EffectStats.builder()
                            .set(Stat.FROZEN, 1)
                            .build());
                    Minion choice = SelectRandom.from(b.getMinions(owner.team * -1, false, true)
                            .filter(m -> m.finalStats.get(Stat.FROZEN) == 0)
                            .collect(Collectors.toList()));
                    if (choice != null) {
                        this.resolve(b, rq, el, new AddEffectResolver(choice, freezer));
                    }
                    List<Minion> alliedTargets = b.getMinions(owner.team, true, true).collect(Collectors.toList());
                    this.resolve(b, rq, el, new RestoreResolver(effect, alliedTargets, 1));
                }
            });

            @Override
            public ResolverWithDescription onTurnEndAllied() {
                return turnend.get();
            }

            @Override
            public ResolverWithDescription onTurnEndEnemy() {
                return turnend.get();
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_PER_HEAL * 6 + AI.VALUE_OF_FREEZE * 2;
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
