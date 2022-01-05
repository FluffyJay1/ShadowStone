package server.card.cardpack.basic;

import java.util.*;

import client.tooltip.*;
import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageFire;
import server.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class Fireball extends Spell {
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final TooltipSpell TOOLTIP = new TooltipSpell("Fireball",
            "Choose 2 enemy minions. Deal 2 damage to them and 1 damage to their adjacent minions.",
            "res/card/basic/fireball.png", CRAFT, 3, Fireball.class);
    final Effect e;

    public Fireball(Board b) {
        super(b, TOOLTIP);
        // anonymous classes within anonymous classes

        this.e = new Effect(TOOLTIP.description) {
            @Override
            public Resolver battlecry() {
                Effect effect = this;
                return new Resolver(false) {
                    @Override
                    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                        List<Card> markedForDeath = new LinkedList<>();
                        for (Card targeted : battlecryTargets.get(0).getTargets()) {
                            List<Minion> m = new LinkedList<>();
                            List<Integer> d = new LinkedList<>();
                            int pos = targeted.getIndex();
                            m.add((Minion) targeted);
                            d.add(2);
                            for (int i = -1; i <= 1; i += 2) {
                                int offsetPos = pos + i;
                                BoardObject adjacent = b.getPlayer(owner.team * -1).getPlayArea().get(offsetPos);
                                if (adjacent instanceof Minion) {
                                    m.add((Minion) adjacent);
                                    d.add(1);
                                }
                            }
                            EffectDamageResolver dr = this.resolve(b, rl, el,
                                    new EffectDamageResolver(effect, m, d, false, EventAnimationDamageFire.class));
                            markedForDeath.addAll(dr.destroyed);
                        }
                        this.resolve(b, rl, el, new DestroyResolver(markedForDeath));
                    }
                };
            }

            @Override
            public double getBattlecryValue() {
                return AI.VALUE_PER_DAMAGE * 8 / 2.;
            }
        };
        Target t = new Target(e, 2, "Deal 2 damage to an enemy minion and 1 damage to adjacent minions.") {
            @Override
            public boolean canTarget(Card c) {
                return c.status == CardStatus.BOARD && c instanceof Minion
                        && c.team != this.getCreator().owner.team;
            }
        };

        LinkedList<Target> list = new LinkedList<>();
        list.add(t);
        e.setBattlecryTargets(list);
        this.addEffect(true, e);
    }

    @Override
    public boolean conditions() {
        return this.board.getTargetableCards(this.e.battlecryTargets.get(0)).size() > 0;
    }

}
