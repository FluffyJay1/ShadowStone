package server.card.cardset.basic.runemage;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.Resolver;
import server.resolver.SpellboostResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class MagicOwl extends MinionText {
    public static final String NAME = "Magic Owl";
    public static final String DESCRIPTION = "<b>Unleash</b>: <b>Spellboost</b> the cards in your hand 2 times.";
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/magicowl.png",
            CRAFT, TRAITS, RARITY, 2, 2, 1, 2, false, MagicOwl.class,
            new Vector2f(155, 195), 1.5, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.UNLEASH, Tooltip.SPELLBOOST));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        for (int i = 0; i < 2; i++) {
                            this.resolve(b, rq, el, new SpellboostResolver(owner.player.getHand()));
                        }
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_OF_SPELLBOOST;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
