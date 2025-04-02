package server.card.cardset.standard.swordpaladin;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.Animation;

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
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class LeonidasResolve extends AmuletText {
    public static final String NAME = "Leonidas' Resolve";
    public static final String DESCRIPTION = "Whenever an allied minion comes into play, give it +3/+0/+3 and <b>Rush</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.COMMANDER);
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, () -> new Animation("card/standard/leonidasresolve.png"),
            CRAFT, TRAITS, RARITY, 9, LeonidasResolve.class,
            new Vector2f(), -1,
            () -> List.of(Tooltip.RUSH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event e) {
                if (e != null) {
                    List<BoardObject> relevant = e.cardsEnteringPlay().stream()
                            .filter(bo -> bo instanceof Minion && bo.team == this.owner.team)
                            .collect(Collectors.toList());
                    if (!relevant.isEmpty()) {
                        return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                Effect buff = new Effect("+3/+0/+3 and <b>Rush</b> (from <b>Leonidas' Resolve</b>).", EffectStats.builder()
                                        .change(Stat.ATTACK, 3)
                                        .change(Stat.HEALTH, 3)
                                        .set(Stat.RUSH, 1)
                                        .build());
                                List<BoardObject> stillInPlay = relevant.stream()
                                        .filter(BoardObject::isInPlay)
                                        .collect(Collectors.toList());
                                this.resolve(b, rq, el, new AddEffectResolver(stillInPlay, buff));
                            }
                        });
                    }
                }
                return null;
            }

            @Override
            public double getPresenceValue(int refs) {
                // oh it very valuable
                return (AI.valueForBuff(3, 0, 3) + AI.valueOfRush(5)) * 5;
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
