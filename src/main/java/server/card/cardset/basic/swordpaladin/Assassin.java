package server.card.cardset.basic.swordpaladin;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class Assassin extends MinionText {
    public static final String NAME = "Assassin";
    public static final String DESCRIPTION = "<b>Battlecry</b>: Give an allied Commander minion <b>Stealth</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.OFFICER);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/assassin.png",
            CRAFT, TRAITS, RARITY, 2, 2, 1, 2, true, Assassin.class,
            new Vector2f(138, 155), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.STEALTH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 0, 1, "Give an allied Commander minion <b>Stealth</b>.") {
                    @Override
                    protected boolean criteria(Card c) {
                        return c instanceof Minion && c.team == this.getCreator().owner.team && c.status.equals(CardStatus.BOARD)
                                && c.finalTraits.contains(CardTrait.COMMANDER);
                    }
                });
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            Effect stealth = new Effect("<b>Stealth</b> (from <b>Assassin</b>).", EffectStats.builder()
                                    .set(Stat.STEALTH, 1)
                                    .build());
                            this.resolve(b, rq, el, new AddEffectResolver(c, stealth));
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_OF_STEALTH / 2;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.board.getTargetableCards((CardTargetingScheme) this.getBattlecryTargetingSchemes().get(0)).findFirst().isPresent();
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
