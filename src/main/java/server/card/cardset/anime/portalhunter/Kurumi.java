package server.card.cardset.anime.portalhunter;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageShoot;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.*;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.Collections;
import java.util.List;

public class Kurumi extends MinionText {
    public static final String NAME = "Kurumi Tokisaki";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Summon 2 <b>Kurumi Tokisakis</b>.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: <b>Choose</b> to set an enemy minion's attack to 0 until the end of their turn, or gain " +
            "the ability to attack +2 times this turn.";
    public static final String DESCRIPTION = "<b>Rush</b>.\n" + BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/anime/kurumi.png",
            CRAFT, TRAITS, RARITY, 7, 3, 3, 3, false, Kurumi.class,
            new Vector2f(152, 125), 1.8, new EventAnimationDamageShoot(),
            () -> List.of(Tooltip.RUSH, Tooltip.BATTLECRY, Tooltip.UNLEASH, Tooltip.CHOOSE),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.RUSH, 1)
                .build()) {
            private List<Card> cachedInstances;
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<CardText> texts = Collections.nCopies(2, new Kurumi());
                        List<Integer> pos = List.of(owner.getIndex(), owner.getIndex() + 2);
                        this.resolve(b, rq, el, new CreateCardResolver(texts, owner.team, CardStatus.BOARD, pos));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = Collections.nCopies(2, new Kurumi().constructInstance(this.owner.board));
                }
                return AI.valueForSummoning(this.cachedInstances, refs);
            }

            @Override
            public List<TargetingScheme<?>> getUnleashTargetingSchemes() {
                return List.of(new ModalTargetingScheme(this, 1, "<b>Choose</b> 1", List.of(
                                new ModalOption("Set an enemy minion's attack to 0 until the end of their turn.",
                                        e -> e.owner.board.getMinions(e.owner.team * -1, false, true).findAny().isPresent()),
                                new ModalOption("Gain the ability to attack +2 times this turn.")
                        )),
                        new CardTargetingScheme(this, 0, 1, "Set an enemy minion's attack to 0 until the end of their turn.") {
                            @Override
                            protected boolean criteria(Card c) {
                                return c.status.equals(CardStatus.BOARD) && c instanceof Minion && c.team != this.getCreator().owner.team;
                            }

                            @Override
                            public boolean isApplicable(List<TargetList<?>> alreadyTargeted) {
                                return alreadyTargeted.get(0).targeted.get(0).equals(0);
                            }
                        });
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int option = (int) targetList.get(0).targeted.get(0);
                        switch (option) {
                            case 0 -> {
                                getStillTargetableCards(Effect::getUnleashTargetingSchemes, targetList, 1).findFirst().ifPresent(c -> {
                                    Effect debuff = new Effect("Attack set to 0 until the end of its turn (from <b>" + NAME + "</b>).",
                                            EffectStats.builder()
                                                    .set(Stat.ATTACK, 0)
                                                    .build(),
                                            e -> e.untilTurnEndTeam = 1);
                                    this.resolve(b, rq, el, new AddEffectResolver(c, debuff));
                                });
                            }
                            case 1 -> {
                                Effect buff = new Effect("+2 attacks this turn (from <b>Unleash</b>).",
                                        EffectStats.builder()
                                                .change(Stat.ATTACKS_PER_TURN, 2)
                                                .build(),
                                        e -> e.untilTurnEndTeam = 1);
                                this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                            }
                        }
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return this.owner.finalStats.get(Stat.ATTACK) * AI.VALUE_PER_DAMAGE;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
