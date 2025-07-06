package server.card.cardset.moba.forestrogue;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageDoubleSlice;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.*;
import server.card.cardset.basic.neutral.NotCoin;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.EffectWithDependentStats;
import server.card.effect.Stat;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.Collections;
import java.util.List;

public class BountyHunter extends MinionText {
    public static final String NAME = "Bounty Hunter";
    private static final String GIVEN_EFFECT_DESCRIPTION = "<b>Last Words</b>: Put 2 <b>Not Coins</b> into your opponent's hand.";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Give an enemy minion \"" + GIVEN_EFFECT_DESCRIPTION + "\"";
    private static final String DEPENDENT_STATS_DESCRIPTION = "Has +2/+0/+0 while <b>Stealthed</b>.";
    private static final String OTHER_DESCRIPTION = "<b>Stealth</b>.";
    public static final String DESCRIPTION = OTHER_DESCRIPTION + "\n" + DEPENDENT_STATS_DESCRIPTION + "\n" + BATTLECRY_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/moba/bountyhunter.png"),
            CRAFT, TRAITS, RARITY, 3, 2, 1, 3, true, BountyHunter.class,
            new Vector2f(132, 162), 1.2, new EventAnimationDamageDoubleSlice(),
            () -> List.of(Tooltip.STEALTH, Tooltip.BATTLECRY, Tooltip.LASTWORDS, NotCoin.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(
                new EffectWithDependentStats(DEPENDENT_STATS_DESCRIPTION, true) {
                    @Override
                    public EffectStats calculateStats() {
                        if (owner.finalStats.get(Stat.STEALTH) > 0) {
                            return EffectStats.builder()
                                    .change(Stat.ATTACK, 2)
                                    .build();
                        }
                        return this.baselineStats;
                    }

                    @Override
                    public boolean isActive() {
                        return this.owner.isInPlay();
                    }
                },
                new Effect(OTHER_DESCRIPTION + BATTLECRY_DESCRIPTION, EffectStats.builder()
                        .set(Stat.STEALTH, 1)
                        .build()) {
                    @Override
                    public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                        return List.of(new CardTargetingScheme(this, 0, 1, "Give an enemy minion \"" + GIVEN_EFFECT_DESCRIPTION + "\"") {
                            @Override
                            protected boolean criteria(Card c) {
                                return c.status.equals(CardStatus.BOARD) && c instanceof Minion && c.team != this.getCreator().owner.team;
                            }
                        });
                    }

                    @Override
                    public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                        return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                                    this.resolve(b, rq, el, new AddEffectResolver(c, new EffectTrack()));
                                });
                            }
                        });
                    }

                    @Override
                    public double getBattlecryValue(int refs) {
                        return 2;
                    }
                }
        );
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }

    public static class EffectTrack extends Effect {
        // required for reflection
        public EffectTrack() {
            super(GIVEN_EFFECT_DESCRIPTION);
        }

        @Override
        public ResolverWithDescription lastWords() {
            return new ResolverWithDescription(GIVEN_EFFECT_DESCRIPTION, CreateCardResolver.builder()
                    .withCards(Collections.nCopies(2, new NotCoin()))
                    .withTeam(owner.team * -1)
                    .withStatus(CardStatus.HAND)
                    .withPos(-1)
                    .build());
        }

        @Override
        public double getLastWordsValue(int refs) {
            return -2;
        }
    }
}
