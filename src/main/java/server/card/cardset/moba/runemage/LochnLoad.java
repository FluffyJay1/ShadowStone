package server.card.cardset.moba.runemage;

import java.util.*;

import client.tooltip.*;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageBigExplosion;
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

public class LochnLoad extends SpellText {
    public static final String NAME = "Loch-n-Load";
    public static final String DESCRIPTION = "Choose 2 enemy minions. Deal 2 damage to them and 1 damage to their adjacent minions.";
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/moba/lochnload.png",
            CRAFT, TRAITS, RARITY, 3, LochnLoad.class,
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 1, 2, DESCRIPTION) {
                    @Override
                    protected boolean criteria(Card c) {
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
                                    new DamageResolver(effect, m, d, false, new EventAnimationDamageBigExplosion().toString()));
                            markedForDeath.addAll(dr.destroyed);
                        });
                        this.resolve(b, rq, el, new DestroyResolver(markedForDeath, EventDestroy.Cause.NATURAL));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return (AI.valueOfMinionDamage(2) * 2 + AI.valueOfMinionDamage(1) * 4) / 2.;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
