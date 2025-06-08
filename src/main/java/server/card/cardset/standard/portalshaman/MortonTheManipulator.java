package server.card.cardset.standard.portalshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.Stat;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.PutCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class MortonTheManipulator extends MinionText {
    public static final String NAME = "Morton the Manipulator";
    public static final String DESCRIPTION = "<b>Unleash</b>: Take control of an enemy minion that costs M or less.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALSHAMAN;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/mortonthemanipulator.png"),
            CRAFT, TRAITS, RARITY, 3, 3, 2, 3, false, MortonTheManipulator.class,
            new Vector2f(153, 137), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.UNLEASH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getUnleashTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 0, 1, "Take control of an enemy minion that costs M or less.") {
                    @Override
                    protected boolean criteria(Card c) {
                        return c.status.equals(CardStatus.BOARD) && c instanceof Minion && c.team != this.getCreator().owner.team
                                && c.finalBasicStats.get(Stat.COST) <= owner.finalStats.get(Stat.MAGIC);
                    }
                });
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getUnleashTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            this.resolve(b, rq, el, new PutCardResolver(c, CardStatus.BOARD, owner.team, owner.getIndex() + 1, true));
                        });
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
