package server.card.cardset.standard.runemage;

import client.tooltip.TooltipSpell;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageFireball;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.cardset.CardSet;
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
import utils.SelectRandom;

import java.util.List;

public class FirelandsPortal extends SpellText {
    public static final String NAME = "Firelands Portal";
    public static final String DESCRIPTION = "Deal 6 damage to an enemy. Summon a random 6-cost minion.";
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/standard/firelandsportal.png"),
            CRAFT, TRAITS, RARITY, 7, FirelandsPortal.class,
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 1, 1, "Deal 6 damage to an enemy.") {
                    @Override
                    protected boolean criteria(Card c) {
                        return c.isInPlay() && c instanceof Minion && c.team != this.getCreator().owner.team;
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
                            this.resolve(b, rq, el, new DamageResolver(effect, (Minion) c, 6, true, new EventAnimationDamageFireball()));
                        });
                        CardText randomCard = SelectRandom.from(CardSet.PLAYABLE_SET.get().stream().filter(ct -> ct instanceof MinionText && ct.getTooltip().cost == 6).toList());
                        if (randomCard != null) {
                            this.resolve(b, rq, el, CreateCardResolver.builder()
                                    .withCard(randomCard)
                                    .withTeam(owner.team)
                                    .withStatus(CardStatus.BOARD)
                                    .withPos(-1)
                                    .build());
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_DAMAGE * 6 + 6;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
