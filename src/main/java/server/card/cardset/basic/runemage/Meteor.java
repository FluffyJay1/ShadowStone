package server.card.cardset.basic.runemage;

import client.tooltip.TooltipSpell;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageBigExplosion;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.ArrayList;
import java.util.List;

public class Meteor extends SpellText {
    public static final String NAME = "Meteor";
    public static final String DESCRIPTION = "Deal 15 damage to an enemy minion and 3 damage to adjacent minions.";
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/basic/meteor.png",
            CRAFT, TRAITS, RARITY, 6, Meteor.class,
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 1, 1, DESCRIPTION) {
                    @Override
                    protected boolean criteria(Card c) {
                        return c.status.equals(CardStatus.BOARD) && c instanceof Minion && c.team != this.getCreator().owner.team;
                    }
                });
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            List<Minion> m = new ArrayList<>(3);
                            List<Integer> d = new ArrayList<>(3);
                            m.add((Minion) c);
                            d.add(15);
                            for (int i = -1; i <= 1; i += 2) {
                                int offsetPos = c.getIndex() + i;
                                BoardObject adjacent = b.getPlayer(c.team).getPlayArea().get(offsetPos);
                                if (adjacent instanceof Minion) {
                                    m.add((Minion) adjacent);
                                    d.add(3);
                                }
                            }
                            this.resolve(b, rq, el, new DamageResolver(effect, m, d, true,
                                    new EventAnimationDamageBigExplosion().toString()));
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.valueOfMinionDamage(15) + AI.valueOfMinionDamage(3);
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
