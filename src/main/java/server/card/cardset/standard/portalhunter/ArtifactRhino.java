package server.card.cardset.standard.portalhunter;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.cardset.basic.portalhunter.Icarus;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.effect.common.EffectStatChange;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.Collections;
import java.util.List;

public class ArtifactRhino extends MinionText {
    public static final String NAME = "Artifact Rhino";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Gain +X/+0/+0. X equals the number of remaining Artifacts in your deck.";
    private static final String STRIKE_DESCRIPTION = "<b>Strike</b>: Put 3 <b>Artifact Rhinos</b> into your deck.";
    public static final String DESCRIPTION = "<b>Rush</b>.\n" + BATTLECRY_DESCRIPTION + "\n" + STRIKE_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.ARTIFACT);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/standard/artifactrhino.png",
            CRAFT, TRAITS, RARITY, 7, 0, 3, 7, true, Icarus.class,
            new Vector2f(189, 163), 1.4, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.RUSH, Tooltip.BATTLECRY, Tooltip.STRIKE),
            List.of(card -> String.format("(Artifacts in deck: %d)", card.player.getDeck().stream().filter(c -> c.finalTraits.contains(CardTrait.ARTIFACT)).count())));

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
                        int numArtifacts = (int) owner.player.getDeck().stream()
                                .filter(c -> c.finalTraits.contains(CardTrait.ARTIFACT))
                                .count();
                        Effect buff = new EffectStatChange("+" + numArtifacts + "/+0/+0 (from <b>Battlecry</b>).", numArtifacts, 0, 0);
                        this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                int numArtifacts = (int) owner.player.getDeck().stream()
                        .filter(c -> c.finalTraits.contains(CardTrait.ARTIFACT))
                        .count();
                return AI.valueForBuff(numArtifacts, 0, 0);
            }

            @Override
            public ResolverWithDescription strike(Minion target) {
                return new ResolverWithDescription(STRIKE_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<CardText> c = Collections.nCopies(3, new ArtifactRhino());
                        List<Integer> inds = SelectRandom.positionsToAdd(owner.player.getDeck().size(), 3);
                        this.resolve(b, rq, el, new CreateCardResolver(c, owner.team, CardStatus.DECK, inds));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = Collections.nCopies(3, new ArtifactRhino().constructInstance(this.owner.board));
                }
                return AI.valueForAddingToDeck(this.cachedInstances, refs);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
