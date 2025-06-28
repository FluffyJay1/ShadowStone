package server.card.cardset.standard.dragondruid;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.Animation;

import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.cardset.basic.dragondruid.Dragon;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class CanyonOfTheDragons extends AmuletText {
    public static final String NAME = "Canyon of the Dragons";
    public static final String DESCRIPTION = "<b>Countdown(3)</b>.\nAt the end of your turn, summon a <b>Dragon</b>.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, () -> new Animation("card/standard/canyonofthedragons.png"),
            CRAFT, TRAITS, RARITY, 7, CanyonOfTheDragons.class,
            new Vector2f(143, 148), 1.3,
            () -> List.of(Tooltip.COUNTDOWN, Dragon.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.COUNTDOWN, 3)
                .build()) {
            private List<Card> cachedInstances; // for getPresenceValue, preview the value of the created cards
            @Override
            public ResolverWithDescription onTurnEndAllied() {
                String resolverDescription = "At the end of your turn, summon a <b>Dragon</b>.";
                return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, CreateCardResolver.builder()
                                .withCard(new Dragon())
                                .withTeam(owner.team)
                                .withStatus(CardStatus.BOARD)
                                .withPos(owner.getIndex() + 1)
                                .build());
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = List.of(new Dragon().constructInstance(this.owner.board));
                }
                return AI.valueForSummoning(this.cachedInstances, refs) * 2;
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
