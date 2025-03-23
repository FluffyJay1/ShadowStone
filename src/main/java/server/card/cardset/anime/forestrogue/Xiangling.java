package server.card.cardset.anime.forestrogue;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageAOEFire;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class Xiangling extends MinionText {
    public static final String NAME = "Xiangling";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Give your leader the following effect: "
            + "\"At the end of your even-numbered turns, deal 1 damage to all enemy minions.\" (This effect stacks and lasts for the rest of the match).";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Summon a <b>Guoba</b> and set its <b>Countdown</b> to M.";
    public static final String DESCRIPTION = BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/anime/xiangling.png",
            CRAFT, TRAITS, RARITY, 4, 2, 2, 2, false, Xiangling.class,
            new Vector2f(153, 135), 1.4, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.UNLEASH, Guoba.TOOLTIP, Tooltip.COUNTDOWN),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        owner.player.getLeader().ifPresent(l -> {
                            this.resolve(b, rq, el, new AddEffectResolver(l, new EffectPyronado()));
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_DAMAGE * 4; // some bullshit
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        CreateCardResolver ccr = this.resolve(b, rq, el, new CreateCardResolver(new Guoba(), owner.team, CardStatus.BOARD, owner.getIndex() + 1));
                        int x = owner.finalStats.get(Stat.MAGIC);
                        Effect countdown = new Effect("<b>Countdown(" + x + ") (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .set(Stat.COUNTDOWN, x)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(ccr.event.successfullyCreatedCards, countdown));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.valueForSummoning(List.of(new Guoba().constructInstance(owner.board)), refs) * owner.finalStats.get(Stat.MAGIC) / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }

    public static class EffectPyronado extends Effect {
        private static String EFFECT_DESCRIPTION = "At the end of your even-numbered turns, deal 1 damage to all enemy minions (from <b>Xiangling</b>).";

        // required for reflection
        public EffectPyronado() {
            super(EFFECT_DESCRIPTION);
        }

        @Override
        public ResolverWithDescription onTurnEndAllied() {
            if (this.owner.player.turn % 2 == 0) {
                Effect effect = this;
                return new ResolverWithDescription(EFFECT_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> relevant = owner.board.getMinions(owner.team * -1, false, true).collect(Collectors.toList());
                        this.resolve(b, rq, el, new DamageResolver(effect, relevant, 1, true, new EventAnimationDamageAOEFire(owner.team * -1, false).toString()));
                    }
                });
            } else {
                return null;
            }
        }

        @Override
        public double getPresenceValue(int refs) {
            return AI.VALUE_PER_DAMAGE * 4; // lol
        }
    }
}
