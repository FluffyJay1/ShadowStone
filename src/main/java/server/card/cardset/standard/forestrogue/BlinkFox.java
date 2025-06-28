package server.card.cardset.standard.forestrogue;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.cardset.CardSet;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;

public class BlinkFox extends MinionText {
    public static final String NAME = "Blink Fox";
    public static final String DESCRIPTION = "<b>Battlecry</b>: Add a random card to your hand from your opponent's class.";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/blinkfox.png"),
            CRAFT, TRAITS, RARITY, 3, 3, 1, 3, true, BlinkFox.class,
            new Vector2f(150, 145), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        b.getPlayer(owner.player.team * -1).getLeader().ifPresent(l -> {
                            ClassCraft craft = l.getCardText().getTooltip().craft;
                            CardText randomCard = SelectRandom.from(CardSet.PLAYABLE_SET.get().stream().filter(ct -> ct.getTooltip().craft.equals(craft)).toList());
                            if (randomCard != null) {
                                this.resolve(b, rq, el, CreateCardResolver.builder()
                                        .withCard(randomCard)
                                        .withTeam(owner.team)
                                        .withStatus(CardStatus.HAND)
                                        .withPos(-1)
                                        .withVisibility(CardVisibility.ALLIES)
                                        .build());
                            }
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
