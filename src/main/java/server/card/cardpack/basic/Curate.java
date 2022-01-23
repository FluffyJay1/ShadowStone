package server.card.cardpack.basic;

import java.util.*;

import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.card.target.CardTargetList;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetingScheme;
import server.event.*;
import server.resolver.*;

public class Curate extends Minion {
    public static final String NAME = "Curate";
    public static final String DESCRIPTION = "<b>Battlecry</b>: Restore 5 health to an ally.";
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/curate.png",
            CRAFT, 7, 5, 3, 5, true, Curate.class, new Vector2f(169, 143), 1.4, EventAnimationDamageSlash.class,
            Tooltip.BATTLECRY);

    public Curate(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 0, 1, "Restore 5 health to an ally.") {
                    @Override
                    public boolean canTarget(Card c) {
                        return c instanceof Minion && ((Minion) c).isInPlay() && c.team == this.getCreator().owner.team;
                    }
                });
            }

            @Override
            public Resolver battlecry() {
                Effect effect = this; // anonymous fuckery
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
                        getStillTargetableBattlecryCardTargets(0).findFirst().ifPresent(c -> {
                            Minion target = (Minion) c;
                            this.resolve(b, rl, el, new RestoreResolver(effect, target, 5));
                        });
                    }
                };
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_HEAL * 5 / 2.;
            }
        };
        this.addEffect(true, e);
    }
}
