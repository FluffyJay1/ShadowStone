package server.card.cardset.basic.portalhunter;

import client.Game;
import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.MinionText;
import server.card.effect.Effect;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class Camieux extends MinionText {
    public static final String NAME = "Camieux, Gunpowder Gal";
    public static final String DESCRIPTION = "<b>Last Words</b>: Deal 1 damage to a random enemy. Do this 3 times.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/camieux.png",
            CRAFT, RARITY, 2, 2, 1, 1, true, Camieux.class,
            new Vector2f(143, 150), 1.4, null,
            () -> List.of(Tooltip.LASTWORDS));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription lastWords() {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        for (int i = 0; i < 3; i++) {
                            List<Minion> relevant = b.getMinions(owner.team * -1, true, true).collect(Collectors.toList());
                            this.resolve(b, rq, el, new DamageResolver(effect, Game.selectRandom(relevant), 1, true, null));
                        }
                    }
                });
            }

            @Override
            public double getLastWordsValue(int refs) {
                return AI.VALUE_PER_DAMAGE * 3;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
