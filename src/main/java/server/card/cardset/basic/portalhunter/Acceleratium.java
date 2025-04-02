package server.card.cardset.basic.portalhunter;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.Animation;

import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.ManaChangeResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class Acceleratium extends AmuletText {
    public static final String NAME = "Acceleratium";
    private static final String ONLISTENEVENT_DESCRIPTION = "Whenever an allied Artifact minion comes into play, give it <b>Rush</b> and recover 1 mana orb.";
    public static final String DESCRIPTION = "<b>Countdown(3)</b>.\n" + ONLISTENEVENT_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, () -> new Animation("card/basic/acceleratium.png"),
            CRAFT, TRAITS, RARITY, 1, Acceleratium.class,
            new Vector2f(130, 143), 1.3,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.RUSH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.COUNTDOWN, 3)
                .build()) {
            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event e) {
                if (e != null) {
                    List<Minion> relevant = e.cardsEnteringPlay().stream()
                            .filter(bo -> bo.team == this.owner.team && bo.finalTraits.contains(CardTrait.ARTIFACT) && bo instanceof Minion)
                            .map(bo -> (Minion) bo)
                            .collect(Collectors.toList());
                    if (!relevant.isEmpty()) {
                        Effect effect = this;
                        return new ResolverWithDescription(ONLISTENEVENT_DESCRIPTION, new Resolver(false) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                Effect buff = new Effect("<b>Rush</b> (from <b>Acceleratium</b>).", EffectStats.builder()
                                        .set(Stat.RUSH, 1)
                                        .build());
                                this.resolve(b, rq, el, new AddEffectResolver(relevant, buff));
                                this.resolve(b, rq, el, new ManaChangeResolver(owner.player, relevant.size(), true, false, false));
                            }
                        });
                    }
                }
                return null;
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
