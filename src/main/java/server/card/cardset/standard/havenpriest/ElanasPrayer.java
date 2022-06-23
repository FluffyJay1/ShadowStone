package server.card.cardset.standard.havenpriest;

import client.tooltip.TooltipAmulet;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.common.EffectStatChange;
import server.event.Event;
import server.event.EventRestore;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class ElanasPrayer extends AmuletText {
    public static final String NAME = "Elana's Prayer";
    public static final String DESCRIPTION = "Whenver your leader's health is restored, give all allied minions +1/+0/+1.";
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, "res/card/standard/elanasprayer.png",
            CRAFT, TRAITS, RARITY, 3, ElanasPrayer.class,
            new Vector2f(150, 140), 1.5,
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event event) {
                if (event instanceof EventRestore && this.owner.player.getLeader().isPresent()
                        && ((EventRestore) event).m.contains(this.owner.player.getLeader().get())) {
                    return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                        @Override
                        public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                            List<Minion> targets = b.getMinions(owner.team, false, true).collect(Collectors.toList());
                            Effect buff = new EffectStatChange("+1/+0/+1 (from <b>Elana's Prayer</b>).", 1, 0, 1);
                            this.resolve(b, rq, el, new AddEffectResolver(targets, buff));
                        }
                    });
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
