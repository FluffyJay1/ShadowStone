package server.card.cardset.basic.portalshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.DrawResolver;
import server.resolver.PutCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;
import java.util.stream.Collectors;

public class RadiantArtifact extends MinionText {
    public static final String NAME = "Radiant Artifact";
    private static final String LASTWORDS_DESCRIPTION = "<b>Last Words</b>: If it's your turn and there are Artifacts in your deck, " +
            "put a random Artifact from your deck into your hand, otherwise draw a card.";
    public static final String DESCRIPTION = "<b>Storm</b>.\n" + LASTWORDS_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.PORTALSHAMAN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.ARTIFACT);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/basic/radiantartifact.png"),
            CRAFT, TRAITS, RARITY, 4, 5, 1, 3, true, RadiantArtifact.class,
            new Vector2f(140, 175), 1.1, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.STORM, Tooltip.LASTWORDS),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.STORM, 1)
                .build()) {
            @Override
            public ResolverWithDescription lastWords() {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Card> relevant = owner.player.getDeck().stream()
                                .filter(c -> c.finalTraits.contains(CardTrait.ARTIFACT))
                                .collect(Collectors.toList());
                        if (b.getCurrentPlayerTurn() == owner.team && !relevant.isEmpty()) {
                            Card selection = SelectRandom.from(relevant);
                            this.resolve(b, rq, el, new PutCardResolver(selection, CardStatus.HAND, owner.team, -1, true));
                        } else {
                            this.resolve(b, rq, el, new DrawResolver(owner.player, 1));
                        }
                    }
                });
            }

            @Override
            public double getLastWordsValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
