package server.card.cardset.basic.forestrogue;

import java.util.*;

import client.tooltip.*;
import org.newdawn.slick.geom.Vector2f;
import server.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;
import server.resolver.util.ResolverQueue;

public class WoodOfBrambles extends AmuletText {
    public static final String NAME = "Wood of Brambles";
    public static final String DESCRIPTION = "<b>Countdown(2)</b>.\n<b>Battlecry</b>: add two <b>Faries</b> to your hand.\n<b>Aura</b>: friendly minions have <b>Clash</b>: deal 1 damage to the enemy minion.";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, "res/card/basic/woodofbrambles.png",
            CRAFT, RARITY, 2, WoodOfBrambles.class,
            new Vector2f(), -1,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.BATTLECRY, Fairy.TOOLTIP, Tooltip.AURA, Tooltip.CLASH));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new EffectAura(TOOLTIP.description, 1, true, false, new EffectBrambles()) {
            @Override
            public boolean applyConditions(Card cardToApply) {
                return cardToApply instanceof Minion;
            }

            @Override
            public Resolver battlecry() {
                Effect effect = this;
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<CardText> cards = List.of(new Fairy(), new Fairy());
                        List<Integer> pos = List.of(-1, -1);
                        this.resolve(b, rq, el, new CreateCardResolver(cards, effect.owner.team, CardStatus.HAND, pos));
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
        }, new Effect("", new EffectStats(
                new EffectStats.Setter(EffectStats.COUNTDOWN, false, 2)
        )));
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }

    public static class EffectBrambles extends Effect {

        // required for reflection
        public EffectBrambles() {
            super("<b>Clash</b>: deal 1 damage to the enemy minion (from <b>Wood of Bramble's Aura</b>).");
        }

        @Override
        public Resolver clash(Minion target) {
            return new DamageResolver(this, target, 1, true, null);
        }

        @Override
        public double getPresenceValue(int refs) {
            return AI.VALUE_PER_DAMAGE * 2 / 2.;
        }
    }
}
