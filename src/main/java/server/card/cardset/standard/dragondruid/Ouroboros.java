package server.card.cardset.standard.dragondruid;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageFire;

import org.newdawn.slick.geom.Vector2f;
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
import server.resolver.RestoreResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class Ouroboros extends MinionText {
    public static final String NAME = "Ouroboros";
    public static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Deal 4 damage to an enemy.";
    public static final String LASTWORDS_DESCRIPTION = "<b>Last Words</b>: Restore 4 health to your leader, then put an <b>" + NAME + "</b> in your hand.";
    public static final String DESCRIPTION = BATTLECRY_DESCRIPTION + "\n" + LASTWORDS_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/ouroboros.png"),
            CRAFT, TRAITS, RARITY, 8, 8, 4, 4, true, Ouroboros.class,
            new Vector2f(176, 137), 1.5, new EventAnimationDamageFire(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.LASTWORDS),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances;

            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 0, 1, "Deal 4 damage to an enemy.") {
                    @Override
                    protected boolean criteria(Card c) {
                        return c.isInPlay() && c instanceof Minion && c.team != this.getCreator().owner.team;
                    }
                });
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            this.resolve(b, rq, el, new DamageResolver(effect, (Minion) c, 4, true, new EventAnimationDamageFire()));
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_DAMAGE * 4;
            }

            @Override
            public ResolverWithDescription lastWords() {
                Effect effect = this;
                return new ResolverWithDescription(LASTWORDS_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        owner.player.getLeader().ifPresent(leader -> {
                            this.resolve(b, rq, el, new RestoreResolver(effect, leader, 4));
                        });
                        this.resolve(b, rq, el, new CreateCardResolver(new Ouroboros(), owner.team, CardStatus.HAND, -1));
                    }
                });
            }

            @Override
            public double getLastWordsValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = List.of(new Ouroboros().constructInstance(owner.board));
                }
                return AI.valueForAddingToHand(cachedInstances, refs) + AI.VALUE_PER_HEAL * 4;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
