package server.card.cardset.basic.swordpaladin;

import client.tooltip.TooltipSpell;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageDoubleSlice;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class WholeSouledSwing extends SpellText {
    public static final String NAME = "Whole-Souled Swing";
    public static final String DESCRIPTION = "If there are fewer allied minions than enemy minions, summon a <b>Knight</b>. Deal 3 damage to an enemy minion";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/basic/wholesouledswing.png"),
            CRAFT, TRAITS, RARITY, 2, WholeSouledSwing.class,
            () -> List.of(Knight.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances; // for getBattlecryValue, preview the value of the created cards
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 1, 1, "Deal 3 damage to an enemy minion.") {
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
                        int enemySize = (int) b.getMinions(owner.team * -1, false, true).count();
                        int alliedSize = (int) b.getMinions(owner.team, false, true).count();
                        if (enemySize > alliedSize) {
                            this.resolve(b, rq, el, new CreateCardResolver(new Knight(), owner.team, CardStatus.BOARD, -1));
                        }
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            this.resolve(b, rq, el, new DamageResolver(effect, (Minion) c, 3, true, new EventAnimationDamageDoubleSlice().toString()));
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = List.of(new Knight().constructInstance(this.owner.board));
                }
                return AI.valueOfMinionDamage(3) + AI.valueForSummoning(this.cachedInstances, refs) / 2;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                int enemySize = (int) this.owner.board.getMinions(owner.team * -1, false, true).count();
                int alliedSize = (int) this.owner.board.getMinions(owner.team, false, true).count();
                return enemySize > alliedSize;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
