package server.card.cardset.basic.swordpaladin;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.DestroyResolver;
import server.resolver.Resolver;
import server.resolver.SpendResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class SellswordLucius extends MinionText {
    public static final String NAME = "Sellsword Lucius";
    public static final String DESCRIPTION = "<b>Battlecry</b>: <b>Spend(4)</b> to destroy an enemy minion.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.OFFICER);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/sellswordlucius.png",
            CRAFT, TRAITS, RARITY, 1, 1, 1, 1, true, SellswordLucius.class,
            new Vector2f(155, 149), 1.22, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.BATTLECRY, Tooltip.SPEND));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 0, 1, "Destroy an enemy minion.") {
                    @Override
                    protected boolean criteria(Card c) {
                        return c instanceof Minion && c.team != this.getCreator().owner.team && c.status.equals(CardStatus.BOARD);
                    }

                    @Override
                    public boolean isApplicable(List<TargetList<?>> alreadyTargeted) {
                        return super.isApplicable(alreadyTargeted) && this.getCreator().owner.canSpendAfterPlayed(4);
                    }
                });
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            this.resolve(b, rq, el, new SpendResolver(effect, 4, new DestroyResolver(c)));
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_OF_DESTROY / 4;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.canSpendAfterPlayed(4)
                        && this.owner.board.getTargetableCards((CardTargetingScheme) this.getBattlecryTargetingSchemes().get(0)).findAny().isPresent();
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
