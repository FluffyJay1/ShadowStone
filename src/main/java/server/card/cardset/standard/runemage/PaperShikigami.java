package server.card.cardset.standard.runemage;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;

import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.event.Event;
import server.resolver.Resolver;
import server.resolver.SpellboostResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class PaperShikigami extends MinionText {
    public static final String NAME = "Paper Shikigami";
    public static final String DESCRIPTION = "<b>Last Words</b>: <b>Spellboost</b> the cards in your hand.";
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/papershikigami.png"),
            CRAFT, TRAITS, RARITY, 2, 2, 1, 3, true, PaperShikigami.class,
            new Vector2f(156, 169), 1.2, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.LASTWORDS, Tooltip.SPELLBOOST),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription lastWords() {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, new SpellboostResolver(owner.player.getHand()));
                    }
                });
            }

            @Override
            public double getLastWordsValue(int refs) {
                return AI.VALUE_OF_SPELLBOOST;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
