package server.card.cardset.standard.runemage;

import client.tooltip.TooltipAmulet;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.common.EffectStatChange;
import server.event.Event;
import server.event.EventPlayCard;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class ConjuringForce extends AmuletText {
    public static final String NAME = "Conjuring Force";
    public static final String DESCRIPTION = "Whenever you play a spell, give your minions +1/+0/+0.";
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, "res/card/standard/conjuringforce.png",
            CRAFT, TRAITS, RARITY, 5, ConjuringForce.class,
            new Vector2f(), -1,
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event e) {
                if (e instanceof EventPlayCard) {
                    EventPlayCard epc = (EventPlayCard) e;
                    if (epc.p.team == this.owner.team && epc.c instanceof Spell) {
                        return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                Effect buff = new EffectStatChange("+1/+0/+0 (from <b>Conjuring Force</b>).", 1, 0, 0);
                                List<Minion> minions = b.getMinions(owner.team, false, true).collect(Collectors.toList());
                                this.resolve(b, rq, el, new AddEffectResolver(minions, buff));
                            }
                        });
                    }
                }
                return null;
            }

            @Override
            public double getPresenceValue(int refs) {
                return 5;
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
