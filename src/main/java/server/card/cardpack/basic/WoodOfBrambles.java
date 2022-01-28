package server.card.cardpack.basic;

import java.util.*;

import client.tooltip.*;
import org.newdawn.slick.geom.Vector2f;
import server.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class WoodOfBrambles extends Amulet {
    public static final String NAME = "Wood of Brambles";
    public static final String DESCRIPTION = "<b>Countdown(2)</b>.\n<b>Battlecry</b>: add two <b>Faries</b> to your hand.\n<b>Aura</b>: friendly minions have <b>Clash</b>: deal 1 damage to the enemy minion.";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, "res/card/basic/woodofbrambles.png",
            CRAFT, 2, WoodOfBrambles.class, new Vector2f(), -1,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.BATTLECRY, Fairy.TOOLTIP, Tooltip.AURA, Tooltip.CLASH));

    public WoodOfBrambles(Board b) {
        super(b, TOOLTIP);
        Effect e = new EffectAura(TOOLTIP.description, 1, true, false, new EffectBrambles()) {
            @Override
            public boolean applyConditions(Card cardToApply) {
                return cardToApply instanceof Minion;
            }

            @Override
            public Resolver battlecry() {
                Effect effect = this;
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
                        List<Card> cards = List.of(new Fairy(effect.owner.board), new Fairy(effect.owner.board));
                        List<Integer> pos = List.of(-1, -1);
                        this.resolve(b, rl, el, new CreateCardResolver(cards, effect.owner.team, CardStatus.HAND, pos));
                    }
                };
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND * 2;
            }

            @Override
            public double getPresenceValue(int refs) {
                return 2;
            }
        };
        e.effectStats.set.setStat(EffectStats.COUNTDOWN, 2);
        this.addEffect(true, e);
    }

    public static class EffectBrambles extends Effect {

        // required for reflection
        public EffectBrambles() {
            super("<b>Clash</b>: deal 1 damage to the enemy minion (from <b>Wood of Bramble's Aura</b>).");
        }

        @Override
        public Resolver clash(Minion target) {
            return new EffectDamageResolver(this, target, 1, true, null);
        }

        @Override
        public double getPresenceValue(int refs) {
            return AI.VALUE_PER_DAMAGE * 2 / 2.;
        }
    }
}
