package server.card.cardset.basic.shadowshaman;

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
import server.resolver.DamageResolver;
import server.resolver.NecromancyResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class UndyingResentment extends SpellText {
    public static final String NAME = "Undying Resentment";
    public static final String DESCRIPTION = "Deal 4 damage to an enemy minion. <b>Necromancy(2)</b>: Deal 6 damage instead.";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/basic/undyingresentment.png"),
            CRAFT, TRAITS, RARITY, 2, UndyingResentment.class,
            () -> List.of(Tooltip.NECROMANCY),
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
                            NecromancyResolver necromancyResolver = new NecromancyResolver(effect, 2, new DamageResolver(effect, (Minion) c, 6, true, new EventAnimationDamageMagicHit()));
                            this.resolve(b, rq, el, necromancyResolver);
                            if (!necromancyResolver.wasSuccessful()) {
                                this.resolve(b, rq, el, new DamageResolver(effect, (Minion) c, 4, true, new EventAnimationDamageMagicHit()));
                            }
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return this.owner.player.shadows >= 2 ? AI.valueOfMinionDamage(5) : AI.valueOfMinionDamage(3);
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.player.shadows >= 2;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
