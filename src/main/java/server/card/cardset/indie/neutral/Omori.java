package server.card.cardset.indie.neutral;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageOmoriStab;
import org.newdawn.slick.geom.Vector2f;

import server.ServerBoard;
import server.card.BoardObject;
import server.card.Card;
import server.card.CardRarity;
import server.card.CardStatus;
import server.card.CardText;
import server.card.CardTrait;
import server.card.ClassCraft;
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
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.TransformResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class Omori extends MinionText {
    public static final String NAME = "OMORI";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: <b>Choose</b> an <b>Emotion</b> card to summon.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Select another card in play, and <b>Choose</b> an <b>Emotion</b> card to <b>Transform</b> it into. Set its <b>Countdown</b> to M.";
    public static final String DESCRIPTION = BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final Tooltip EMOTIONS_TOOLTIP = new Tooltip("Emotion Cards",
                    "<b>" + EmotionSad.NAME + "</b>, <b>" + EmotionAngry.NAME + "</b>, and <b>" + EmotionHappy.NAME + "</b>.",
                    () -> List.of(EmotionSad.TOOLTIP, EmotionAngry.TOOLTIP, EmotionHappy.TOOLTIP));

    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/indie/omori.png"),
            CRAFT, TRAITS, RARITY, 4, 3, 2, 5, false, Omori.class,
            new Vector2f(178, 131), 1.6, new EventAnimationDamageOmoriStab(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.CHOOSE, EMOTIONS_TOOLTIP, Tooltip.UNLEASH, Tooltip.TRANSFORM, Tooltip.COUNTDOWN),
            List.of());


    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(
                        new ModalTargetingScheme(this, 1, "<b>Choose</b> 1", List.of(
                                new ModalOption("Summon <b>" + EmotionSad.NAME + "</b>."),
                                new ModalOption("Summon <b>" + EmotionAngry.NAME + "</b>."),
                                new ModalOption("Summon <b>" + EmotionHappy.NAME + "</b>.")
                        ))
                );
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<?> options = targetList.get(0).targeted;
                        if (!options.isEmpty()) {
                            int option = (int) options.get(0);
                            CardText ct = List.of(new EmotionSad(), new EmotionAngry(), new EmotionHappy()).get(option);
                            this.resolve(b, rq, el, new CreateCardResolver(ct, owner.team, CardStatus.BOARD, owner.getIndex() + 1));
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 3; // idk
            }

            @Override
            public List<TargetingScheme<?>> getUnleashTargetingSchemes() {
                return List.of(
                        new CardTargetingScheme(this, 0, 1, "Select a card to <b>Transform</b>.") {
                            @Override
                            protected boolean criteria(Card c) {
                                return c instanceof BoardObject && c.isInPlay() && c.status.equals(CardStatus.BOARD) && c != this.getCreator().owner;
                            }

                        },
                        new ModalTargetingScheme(this, 1, "<b>Choose</b> 1", List.of(
                                new ModalOption("<b>Transform</b> the card into <b>" + EmotionSad.NAME + "</b>."),
                                new ModalOption("<b>Transform</b> the card into <b>" + EmotionAngry.NAME + "</b>."),
                                new ModalOption("<b>Transform</b> the card into <b>" + EmotionHappy.NAME + "</b>.")
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
                                CardText ct = List.of(new EmotionSad(), new EmotionAngry(), new EmotionHappy()).get(option);
                                TransformResolver tr = this.resolve(b, rq, el, new TransformResolver(targeted, ct));
                                int m = owner.finalStats.get(Stat.MAGIC);
                                Effect countdown = new Effect("<b>Countdown(" + m + ")</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                        .set(Stat.COUNTDOWN, m)
                                        .build());
                                this.resolve(b, rq, el, new AddEffectResolver(tr.event.into.get(0), countdown));
                            }
                        });
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return 3; // idk
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
