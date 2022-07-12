package server.card.cardset.basic.portalhunter;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageDoubleSlice;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
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

import java.util.List;

public class DimensionCut extends SpellText {
    public static final String NAME = "Dimension Cut";
    public static final String DESCRIPTION = "Deal 3 damage to an enemy minion. If <b>Resonance</b> is active for you, deal 5 damage instead.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "card/basic/dimensioncut.png",
            CRAFT, TRAITS, RARITY, 2, DimensionCut.class,
            () -> List.of(Tooltip.RESONANCE),
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
                            int damage = owner.player.resonance() ? 5 : 3;
                            this.resolve(b, rq, el, new DamageResolver(effect, (Minion) c, damage, true, new EventAnimationDamageDoubleSlice().toString()));
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.valueOfMinionDamage(4); // close enough
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.player.resonance();
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
