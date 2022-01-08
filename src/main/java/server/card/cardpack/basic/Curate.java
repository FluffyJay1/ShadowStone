package server.card.cardpack.basic;

import java.util.*;

import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class Curate extends Minion {
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final TooltipMinion TOOLTIP = new TooltipMinion("Curate",
            "<b>Battlecry</b>: Restore 5 health to an ally.", "res/card/basic/curate.png", CRAFT, 7, 5, 3, 5, true,
            Curate.class, new Vector2f(169, 143), 1.4, EventAnimationDamageSlash.class, Tooltip.BATTLECRY);

    public Curate(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(TOOLTIP.description) {
            @Override
            public Resolver battlecry() {
                Effect effect = this; // anonymous fuckery
                return new Resolver(false) {
                    @Override
                    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                        // TODO Auto-generated method stub
                        List<Card> targets = battlecryTargets.get(0).getTargetedCards();
                        if (!targets.isEmpty()) {
                            Minion target = (Minion) targets.get(0);
                            this.resolve(b, rl, el, new RestoreResolver(effect, target, 5));
                        }
                    }
                };
            }

            @Override
            public double getBattlecryValue() {
                return AI.VALUE_PER_HEAL * 5 / 2.;
            }
        };
        Target t = new Target(e, 1, "Restore 5 health to an ally.") {
            @Override
            public boolean canTarget(Card c) {
                return c instanceof Minion && ((Minion) c).isInPlay() && c.team == this.getCreator().owner.team;
            }
        };
        List<Target> list = new LinkedList<>();
        list.add(t);
        e.setBattlecryTargets(list);
        this.addEffect(true, e);
    }
}
