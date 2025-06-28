package server.card.cardset.basic.dragondruid;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.*;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class GaluaOfTwoBreaths extends MinionText {
    public static final String NAME = "Galua of Two Breaths";
    public static final String DESCRIPTION = "<b>Battlecry</b>: <b>Choose</b> to put a <b>White Breath</b> or a <b>Black Breath</b> into your hand.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/basic/galuaoftwobreaths.png"),
            CRAFT, TRAITS, RARITY, 5, 4, 2, 5, true, GaluaOfTwoBreaths.class,
            new Vector2f(156, 179), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.CHOOSE, WhiteBreath.TOOLTIP, BlackBreath.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new ModalTargetingScheme(this, 1, "<b>Choose</b> 1", List.of(
                        new ModalOption("Put a <b>White Breath</b> into your hand."),
                        new ModalOption("Put a <b>Black Breath</b> into your hand.")
                )));
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int option = (int) targetList.get(0).targeted.get(0);
                        CardText ct = option == 0 ? new WhiteBreath() : new BlackBreath();
                        this.resolve(b, rq, el, CreateCardResolver.builder()
                                .withCard(ct)
                                .withTeam(owner.team)
                                .withStatus(CardStatus.HAND)
                                .withPos(-1)
                                .withVisibility(CardVisibility.ALLIES)
                                .build());
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
