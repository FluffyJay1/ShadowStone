package server.card.cardset.standard.swordpaladin;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;

import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.cardset.basic.swordpaladin.Knight;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.event.EventMinionAttack;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LathamHonorableKnight extends MinionText {
    public static final String NAME = "Latham, Honorable Knight";
    public static final String DESCRIPTION = "<b>Battlecry</b>: Give your leader the following effects:\n" +
    "- \"Whenever an allied minion attacks, if there are no allied <b>Knights</b> in play, summon a <b>Knight</b>.\"\n" +
    "- \"Whenever an allied 1-cost minion comes into play, give it <b>Storm</b>.\"\n" +
    "(These effects are not stackable and last for the rest of the match.)";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.COMMANDER);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/lathamhonorableknight.png"),
            CRAFT, TRAITS, RARITY, 8, 8, 2, 7, true, LathamHonorableKnight.class,
            new Vector2f(145, 131), 2, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Knight.TOOLTIP, Tooltip.STORM),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        owner.player.getLeader().ifPresent(l -> {
                            this.resolve(b, rq, el, new AddEffectResolver(l, new EffectLathamStrike()));
                            this.resolve(b, rq, el, new AddEffectResolver(l, new EffectLathamStorm()));
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                // return some bullshit
                return 5;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }

    public static class EffectLathamStrike extends Effect {
        private List<Card> cachedInstances;
        private static String EFFECT_DESCRIPTION = "Whenever an allied minion attacks, if there are no allied <b>Knights</b> in play, summon a <b>Knight</b> (from <b>" + NAME + "</b>).";

        // required for reflection
        public EffectLathamStrike() {
            super(EFFECT_DESCRIPTION);
            this.stackable = false;
        }

        @Override
        public ResolverWithDescription onListenEventWhileInPlay(Event event) {
            if (!(event instanceof EventMinionAttack)) {
                return null;
            }
            EventMinionAttack ema = (EventMinionAttack) event;
            if (ema.m1.team != this.owner.team || owner.player.getPlayArea().size() >= owner.player.maxPlayAreaSize || this.owner.board.getMinions(this.owner.team, false, true).anyMatch(m -> m.getCardText() instanceof Knight)) {
                return null;
            }
            return new ResolverWithDescription(EFFECT_DESCRIPTION, new Resolver(false) {
                @Override
                public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                    this.resolve(b, rq, el, new CreateCardResolver(new Knight(), owner.team, CardStatus.BOARD, ema.m1.getIndex() + 1));
                }
            });
        }

        @Override
        public double getPresenceValue(int refs) {
            if (this.cachedInstances == null) {
                this.cachedInstances = Collections.nCopies(2, new Knight().constructInstance(this.owner.board));
            }
            return AI.valueForSummoning(this.cachedInstances, refs); // lol
        }
    }

    public static class EffectLathamStorm extends Effect {
        private static String EFFECT_DESCRIPTION = "Whenever an allied 1-cost minion comes into play, give it <b>Storm</b>. (from <b>" + NAME + "</b>).";

        // required for reflection
        public EffectLathamStorm() {
            super(EFFECT_DESCRIPTION);
            this.stackable = false;
        }

        @Override
        public ResolverWithDescription onListenEventWhileInPlay(Event event) {
            if (event != null) {
                List<Minion> relevant = event.cardsEnteringPlay().stream()
                        .filter(bo -> bo.team == this.owner.team && bo.finalBasicStats.get(Stat.COST) == 1 && bo instanceof Minion)
                        .map(bo -> (Minion) bo)
                        .collect(Collectors.toList());
                if (!relevant.isEmpty()) {
                    return new ResolverWithDescription(EFFECT_DESCRIPTION, new Resolver(false) {
                        @Override
                        public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                            Effect buff = new Effect("<b>Storm</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                    .set(Stat.STORM, 1)
                                    .build());
                            this.resolve(b, rq, el, new AddEffectResolver(relevant, buff));
                        }
                    });
                }
            }
            return null;
        }

        @Override
        public double getPresenceValue(int refs) {
            return AI.valueOfStorm(2) * 2; // lol
        }
    }
}
