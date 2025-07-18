package server.card.cardset.standard.portalshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.cardset.basic.portalshaman.Icarus;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.EffectWithDependentStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.Collections;
import java.util.List;

public class ArtifactRhino extends MinionText {
    public static final String NAME = "Artifact Rhino";
    private static final String DEPENDENT_STATS_DESCRIPTION = "Has +X/+0/+0. X equals the number of remaining Artifacts in your deck.";
    private static final String STRIKE_DESCRIPTION = "<b>Strike</b>: Put 3 <b>Artifact Rhinos</b> into your deck.";
    private static final String OTHER_DESCRIPTION = "<b>Rush</b>.";
    public static final String DESCRIPTION = OTHER_DESCRIPTION + "\n" + DEPENDENT_STATS_DESCRIPTION + "\n" + STRIKE_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.PORTALSHAMAN;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.ARTIFACT);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/artifactrhino.png"),
            CRAFT, TRAITS, RARITY, 7, 0, 3, 8, true, Icarus.class,
            new Vector2f(189, 163), 1.4, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.RUSH, Tooltip.BATTLECRY, Tooltip.STRIKE),
            List.of(card -> String.format("(Artifacts in deck: %d)", card.player.getDeck().stream().filter(c -> c.finalTraits.contains(CardTrait.ARTIFACT)).count())));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(
                new EffectWithDependentStats(DEPENDENT_STATS_DESCRIPTION, true) {
                    @Override
                    public EffectStats calculateStats() {
                        int numArtifacts = (int) owner.player.getDeck().stream()
                                .filter(c -> c.finalTraits.contains(CardTrait.ARTIFACT))
                                .count();
                        return EffectStats.builder()
                                .change(Stat.ATTACK, numArtifacts)
                                .build();
                    }

                    @Override
                    public boolean isActive() {
                        return this.owner.isInPlay();
                    }
                },
                new Effect(OTHER_DESCRIPTION + "\n" + STRIKE_DESCRIPTION, EffectStats.builder()
                    .set(Stat.RUSH, 1)
                    .build()) {
                    @Override
                    public ResolverWithDescription strike(Minion target) {
                        return new ResolverWithDescription(STRIKE_DESCRIPTION, new Resolver(true) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                List<CardText> c = Collections.nCopies(3, new ArtifactRhino());
                                List<Integer> inds = SelectRandom.positionsToAdd(owner.player.getDeck().size(), 3);
                                this.resolve(b, rq, el, CreateCardResolver.builder()
                                        .withCards(c)
                                        .withTeam(owner.team)
                                        .withStatus(CardStatus.DECK)
                                        .withPos(inds)
                                        .build());
                            }
                        });
                    }

                    @Override
                    public double getPresenceValue(int refs) {
                        return 3; // nahh
                    }
                }
        );
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
