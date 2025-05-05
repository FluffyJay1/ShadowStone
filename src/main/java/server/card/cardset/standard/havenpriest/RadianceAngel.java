package server.card.cardset.standard.havenpriest;

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
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.DrawResolver;
import server.resolver.Resolver;
import server.resolver.RestoreResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class RadianceAngel extends MinionText {
    public static final String NAME = "Radiance Angel";
    private static final String DESCRIPTION = "<b>Battlecry</b>: Restore 4 health to your leader. Draw a card.";
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/radianceangel.png"),
            CRAFT, TRAITS, RARITY, 5, 3, 2, 4, true, RadianceAngel.class,
            new Vector2f(143, 130), 1.5, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        owner.player.getLeader().ifPresent(l -> {
                            this.resolve(b, rq, el, new RestoreResolver(effect, l, 4));
                        });
                        this.resolve(b, rq, el, new DrawResolver(owner.player, 1));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_HEAL * 4 + AI.VALUE_PER_CARD_IN_HAND;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
