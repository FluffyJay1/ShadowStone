package server.card.cardset.basic.runemage;

import java.util.*;

import client.tooltip.*;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageFire;
import server.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.*;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class Fireball extends SpellText {
    public static final String NAME = "Fireball";
    public static final String DESCRIPTION = "Choose 2 enemy minions. Deal 2 damage to them and 1 damage to their adjacent minions.";
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/basic/fireball.png",
            CRAFT, RARITY, 3, Fireball.class,
            List::of);

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 1, 2, DESCRIPTION) {
                    @Override
                    public boolean canTarget(Card c) {
                        return c.status == CardStatus.BOARD && c instanceof Minion
                                && c.team != this.getCreator().owner.team;
                    }
                });
            }
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Card> markedForDeath = new LinkedList<>();
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).forEach(targeted -> {
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
                            DamageResolver dr = this.resolve(b, rq, el,
                                    new DamageResolver(effect, m, d, false, EventAnimationDamageFire.class));
                            markedForDeath.addAll(dr.destroyed);
                        });
                        this.resolve(b, rq, el, new DestroyResolver(markedForDeath));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_DAMAGE * 8 / 2.;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
