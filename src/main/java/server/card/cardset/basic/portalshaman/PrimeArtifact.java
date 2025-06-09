package server.card.cardset.basic.portalshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;

import server.ServerBoard;
import server.ai.AI;
import server.card.Card;
import server.card.CardRarity;
import server.card.CardStatus;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.RemoveEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class PrimeArtifact extends MinionText {
    public static final String NAME = "Prime Artifact";
    public static final String DESCRIPTION = "<b>Last Words</b>: At the start of your next turn, summon a <b>" + NAME + "</b>.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALSHAMAN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.ARTIFACT);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/basic/primeartifact.png"),
            CRAFT, TRAITS, RARITY, 7, 5, 2, 5, true, PrimeArtifact.class,
            new Vector2f(150, 160), 1.2, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.LASTWORDS),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            List<Card> cachedInstances;

            @Override
            public ResolverWithDescription lastWords() {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        Effect buff = new EffectPrimeArtifactResummon();
                        owner.player.getLeader().ifPresent(l -> {
                            this.resolve(b, rq, el, new AddEffectResolver(l, buff));
                        });
                    }
                });
            }

            @Override
            public double getLastWordsValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = List.of(new PrimeArtifact().constructInstance(owner.board));
                }
                return AI.valueForSummoning(cachedInstances, refs) / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }

    public static class EffectPrimeArtifactResummon extends Effect {
        public static final String EFFECT_DESCRIPTION = "At the start of your turn, summon a <b>" + NAME + "</b> (from <b>" + NAME + "</b>).";

        public EffectPrimeArtifactResummon() {
            super(EFFECT_DESCRIPTION);
        }

        @Override
        public ResolverWithDescription onTurnStartAllied() {
            Effect effect = this;
            return new ResolverWithDescription(EFFECT_DESCRIPTION, new Resolver(true) {
                @Override
                public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                    this.resolve(b, rq, el, new CreateCardResolver(new PrimeArtifact(), owner.team, CardStatus.BOARD, -1));
                    this.resolve(b, rq, el, new RemoveEffectResolver(List.of(effect)));
                }
            });
        }
    }
}
