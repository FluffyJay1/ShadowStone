package server.card.cardset.standard.portalhunter;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.cardset.basic.portalhunter.PrimeArtifact;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.PutCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;

public class KnowerOfHistory extends MinionText {
    public static final String NAME = "Knower of History";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Put a <b>" + PrimeArtifact.NAME + "</b> into your deck.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Put a random Artifact from your deck into your hand, " +
            "and gain +M/+0/+0 and <b>Rush</b> until the end of the turn.";
    public static final String DESCRIPTION = BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/knowerofhistory.png"),
            CRAFT, TRAITS, RARITY, 2, 2, 1, 3, false, KnowerOfHistory.class,
            new Vector2f(160, 130), 1.5, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, PrimeArtifact.TOOLTIP, Tooltip.UNLEASH, Tooltip.RUSH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            List<Card> cachedInstances;

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Integer> pos = SelectRandom.positionsToAdd(owner.player.getDeck().size(), 1);
                        this.resolve(b, rq, el, new CreateCardResolver(List.of(new PrimeArtifact()), owner.team, CardStatus.DECK, pos));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = List.of(new PrimeArtifact().constructInstance(owner.board));
                }
                return AI.valueForAddingToDeck(cachedInstances, refs);
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        Card selection = SelectRandom.from(owner.player.getDeck().stream()
                                .filter(c -> c.finalTraits.contains(CardTrait.ARTIFACT))
                                .toList());
                        if (selection != null) {
                            this.resolve(b, rq, el, new PutCardResolver(selection, CardStatus.HAND, owner.team, -1, true));
                        }
                        int x = owner.finalStats.get(Stat.MAGIC);
                        Effect buff = new Effect("+" + x + "/+0/+0 and <b>Rush</b> until the end of the turn (from <b>Unleash</b>).", EffectStats.builder()
                                .change(Stat.ATTACK, x)
                                .set(Stat.RUSH, 1)
                                .build(),
                                e -> e.untilTurnEndTeam = 0);
                        this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return (AI.VALUE_PER_CARD_IN_HAND + AI.valueOfRush(this.owner.finalStats.get(Stat.MAGIC) + this.owner.finalStats.get(Stat.ATTACK))) / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
