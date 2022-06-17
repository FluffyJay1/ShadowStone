package server.card.cardset.basic.bloodwarlock;

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
import server.event.Event;
import server.event.EventDamage;
import server.resolver.Resolver;
import server.resolver.RestoreResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class DemonicRam extends MinionText {
    public static final String NAME = "Demonic Ram";
    public static final String DESCRIPTION = "Whenever your leader takes damage during your turn, restore 2 health to your leader.";
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/demonicram.png",
            CRAFT, TRAITS, RARITY, 2, 2, 1, 2, true, DemonicRam.class,
            new Vector2f(150, 170), 1.2, EventAnimationDamageSlash.class,
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onListenEvent(Event event) {
                Effect effect = this;
                if (event instanceof EventDamage && this.owner.isInPlay() && this.owner.board.getCurrentPlayerTurn() == this.owner.team) {
                    EventDamage ed = (EventDamage) event;
                    if(this.owner.player.getLeader().isPresent()) {
                        if (ed.m.contains(this.owner.player.getLeader().get())) {
                            return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                                @Override
                                public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                    owner.player.getLeader().ifPresent(l -> {
                                        this.resolve(b, rq, el, new RestoreResolver(effect, l, 2));
                                    });
                                }
                            });
                        }
                    }
                }
                return null;
            }

            @Override
            public double getPresenceValue(int refs) {
                // i really dont know how to evaluate this
                return AI.VALUE_PER_HEAL * 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
