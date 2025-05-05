package server.card.cardset.standard.bloodwarlock;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageDoubleSlice;
import server.ServerBoard;
import server.card.Card;
import server.card.CardRarity;
import server.card.CardStatus;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.SpellText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.EffectWithDependentStats;
import server.card.effect.Stat;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class DiabolicDrain extends SpellText {
    public static final String NAME = "Diabolic Drain";
    public static final String BATTLECRY_DESCRIPTION = "Deal 5 damage to an enemy minion.";
    public static final String DEPENDENT_STATS_DESCRIPTION = "Costs 4 less while <b>Vengeance</b> is active for you.";
    public static final String OTHER_DESCRIPTION = "<b>Lifesteal</b>.";
    public static final String DESCRIPTION = OTHER_DESCRIPTION + "\n" + DEPENDENT_STATS_DESCRIPTION + "\n" + BATTLECRY_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/standard/diabolicdrain.png"),
            CRAFT, TRAITS, RARITY, 5, DiabolicDrain.class,
            () -> List.of(Tooltip.LIFESTEAL, Tooltip.VENGEANCE),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(
                new EffectWithDependentStats(DEPENDENT_STATS_DESCRIPTION, true) {
                    @Override
                    public EffectStats calculateStats() {
                        if (owner.player.vengeance()) {
                            return EffectStats.builder()
                                    .change(Stat.COST, -4)
                                    .build();
                        }
                        return new EffectStats();
                    }

                    @Override
                    public boolean isActive() {
                        return this.owner.status.equals(CardStatus.HAND) || this.owner.status.equals(CardStatus.DECK);
                    }
                },
                new Effect(OTHER_DESCRIPTION + "\n" + BATTLECRY_DESCRIPTION, EffectStats.builder()
                        .set(Stat.LIFESTEAL, 1)
                        .build()) {
                    @Override
                    public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                        return List.of(new CardTargetingScheme(this, 1, 1, "Deal 5 damage to an enemy minion.") {
                            @Override
                            protected boolean criteria(Card c) {
                                return c.status.equals(CardStatus.BOARD) && c instanceof Minion && c.team != this.getCreator().owner.team;
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
                                    this.resolve(b, rq, el, new DamageResolver(effect, (Minion) c, 5, true, new EventAnimationDamageDoubleSlice()));
                                });
                            }
                        });
                    }
                }
        );
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
