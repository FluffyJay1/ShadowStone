package server.card.cardset.special.batter;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageOff;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.event.Event;
import server.resolver.Resolver;
import server.resolver.RestoreResolver;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class Omega extends MinionText {
    public static final String NAME = "Add-on: Omega";
    public static final String DESCRIPTION = "When this minion attacks, restore 5 health to your leader.";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/special/omega.png",
            CRAFT, RARITY, 4, 2, 2, 2, true, Omega.class,
            new Vector2f(), -1, EventAnimationDamageOff.class,
            () -> List.of(Tooltip.RUSH));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public Resolver onAttack(Minion target) {
                Effect effect = this;
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        b.getPlayer(owner.team).getLeader().ifPresent(l -> this.resolve(b, rq, el, new RestoreResolver(effect, l, 5)));
                    }
                };
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_PER_HEAL * 5 / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
