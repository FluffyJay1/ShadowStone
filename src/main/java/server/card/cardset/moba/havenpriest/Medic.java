package server.card.cardset.moba.havenpriest;

import java.util.List;

import org.newdawn.slick.geom.Vector2f;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import server.ServerBoard;
import server.ai.AI;
import server.card.BoardObject;
import server.card.Card;
import server.card.CardRarity;
import server.card.CardStatus;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.CardTargetList;
import server.card.target.CardTargetingScheme;
import server.card.target.ModalOption;
import server.card.target.ModalTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.RestoreResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

public class Medic extends MinionText {
    public static final String NAME = "Medic";
    private static final String ONTURNEND_DESCRIPTION = "At the end of your turn, restore M health to a random ally with the most missing health, twice.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Select another allied minion. <b>Choose</b> which effect to give until the end of your turn: <b>Rush</b> and +M/+0/+0, or <b>Rush</b> and <b>Invulnerable</b>.";
    public static final String DESCRIPTION = ONTURNEND_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/moba/medic.png"),
            CRAFT, TRAITS, RARITY, 4, 2, 3, 4, false, Medic.class,
            new Vector2f(), -1, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.UNLEASH, Tooltip.CHOOSE, Tooltip.RUSH, Tooltip.INVULNERABLE),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getUnleashTargetingSchemes() {
                return List.of(
                        new CardTargetingScheme(this, 0, 1, "Select a card to buff.") {
                            @Override
                            protected boolean criteria(Card c) {
                                return c instanceof Minion && c.isInPlay() && c.team == owner.team && c.status.equals(CardStatus.BOARD) && c != this.getCreator().owner;
                            }

                        },
                        new ModalTargetingScheme(this, 1, "<b>Choose</b> 1", List.of(
                                new ModalOption("Give the minion <b>Rush</b> and +M/+0/+0 until the end of the turn."),
                                new ModalOption("Give the minion <b>Rush</b> and <b>Invulnerable</b> until the end of the turn.")
                        )) {
                            @Override
                            public boolean isApplicable(List<TargetList<?>> alreadyTargeted) {
                                // only applicable if there is a card to target
                                return ((CardTargetList) alreadyTargeted.get(0)).targeted.size() > 0;
                            }
                        }
                );
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getUnleashTargetingSchemes, targetList, 0).findFirst().ifPresent(targeted -> {
                            List<?> options = targetList.get(1).targeted;
                            if (!options.isEmpty()) {
                                int option = (int) options.get(0);
                                if (option == 0) {
                                    // kritz
                                    int m = owner.finalStats.get(Stat.MAGIC);
                                    Effect buff = new Effect("<b>Rush</b> and +" + m + "/+0/+0 (from <b>" + NAME + "</b>).", EffectStats.builder()
                                            .set(Stat.RUSH, 1)
                                            .change(Stat.ATTACK, m)
                                            .build(),
                                            e -> e.setUntilTurnEnd(0, 1));
                                    this.resolve(b, rq, el, new AddEffectResolver(targeted, buff));
                                } else {
                                    // normal uber
                                    Effect buff = new Effect("<b>Rush</b> and <b>Invulnerable</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                            .set(Stat.RUSH, 1)
                                            .set(Stat.INVULNERABLE, 1)
                                            .build(),
                                            e -> e.setUntilTurnEnd(0, 1));
                                    this.resolve(b, rq, el, new AddEffectResolver(targeted, buff));
                                }
                            }
                        });
                    }
                });
            }

            @Override
            public ResolverWithDescription onTurnEndAllied() {
                Effect effect = this;
                return new ResolverWithDescription(ONTURNEND_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        for (int i = 0; i < 2; i++) {
                            Minion target = SelectRandom.oneOfWith(b.getMinions(owner.team, true, true).toList(),
                                    m -> m.finalStats.get(Stat.HEALTH) - m.health, Integer::max);
                            if (target != null) {
                                this.resolve(b, rq, el, new RestoreResolver(effect, target, owner.finalStats.get(Stat.MAGIC)));
                            }
                        }
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return owner.finalStats.get(Stat.MAGIC) * AI.VALUE_PER_HEAL * 2 + 4; // idk
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
