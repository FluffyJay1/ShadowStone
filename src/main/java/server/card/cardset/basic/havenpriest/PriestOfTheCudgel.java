package server.card.cardset.basic.havenpriest;

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
import server.resolver.BanishResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class PriestOfTheCudgel extends MinionText {
    public static final String NAME = "Priest of the Cudgel";
    public static final String DESCRIPTION = "<b>Unleash</b>: <b>Banish</b> an enemy minion with M health or less.";
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/basic/priestofthecudgel.png"),
            CRAFT, TRAITS, RARITY, 4, 3, 3, 4, false, PriestOfTheCudgel.class,
            new Vector2f(162, 140), 1.4, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.UNLEASH, Tooltip.BANISH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getUnleashTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 0, 1, "<b>Banish</b> an enemy minion with M health or less.") {
                    @Override
                    protected boolean criteria(Card c) {
                        return c.status.equals(CardStatus.BOARD) && c instanceof Minion
                                && c.team != this.getCreator().owner.team && ((Minion) c).health <= owner.finalStats.get(Stat.MAGIC);
                    }
                });
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getUnleashTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            this.resolve(b, rq, el, new BanishResolver(c));
                        });
                    }
                });
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
