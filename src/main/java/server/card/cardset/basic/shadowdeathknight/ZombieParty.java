package server.card.cardset.basic.shadowdeathknight;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageMagicHit;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.Collections;
import java.util.List;

public class ZombieParty extends SpellText {
    public static final String NAME = "Zombie Party";
    public static final String DESCRIPTION = "Deal 4 damage to an enemy minion. Then <b>Spend(5)</b> to summon 3 <b>Zombies</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWDEATHKNIGHT;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/basic/zombieparty.png"),
            CRAFT, TRAITS, RARITY, 2, ZombieParty.class,
            () -> List.of(Tooltip.SPEND, Zombie.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances; // for getBattlecryValue, preview the value of the created cards
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
                        this.resolve(b, rq, el, new DamageResolver(effect, (Minion) c, 4, true, new EventAnimationDamageMagicHit()));
                        });
                        List<CardText> zombies = Collections.nCopies(3, new Zombie());
                        List<Integer> pos = Collections.nCopies(3, -1);
                        this.resolve(b, rq, el, new SpendResolver(effect, 5, new CreateCardResolver(zombies, owner.team, CardStatus.BOARD, pos)));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = Collections.nCopies(3, new Zombie().constructInstance(this.owner.board));
                }
                return AI.valueOfMinionDamage(4) + AI.valueForSummoning(this.cachedInstances, refs) / 5;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.canSpendAfterPlayed(5);
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
