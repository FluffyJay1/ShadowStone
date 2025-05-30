package server.card.cardset.standard.havenpriest;

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
import server.event.EventMinionAttack;
import server.resolver.Resolver;
import server.resolver.RestoreResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class Tenko extends MinionText {
    public static final String NAME = "Tenko";
    public static final String DESCRIPTION = "Whenever an allied minion attacks, restore 1 defense to your leader.";
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/tenko.png"),
            CRAFT, TRAITS, RARITY, 5, 5, 2, 5, true, Tenko.class,
            new Vector2f(135, 160), 1.3, new EventAnimationDamageSlash(),
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event event) {
                Effect effect = this;
                if (event instanceof EventMinionAttack) {
                    EventMinionAttack ema = (EventMinionAttack) event;
                    if (ema.m1.team == this.owner.team) {
                        return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                owner.player.getLeader().ifPresent(l -> {
                                    this.resolve(b, rq, el, new RestoreResolver(effect, l, 1));
                                });
                            }
                        });
                    }
                }
                return null;
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_PER_HEAL * 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
