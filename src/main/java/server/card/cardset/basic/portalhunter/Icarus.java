package server.card.cardset.basic.portalhunter;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.EffectUntilTurnEnd;
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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Icarus extends MinionText {
    public static final String NAME = "Icarus";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Put 2 <b>Ancient Artifacts</b> into your deck.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Put a random Artifact from your deck into your hand, " +
            "and gain +X/+0/+0 and <b>Rush</b> until the end of the turn. X equals this minion's magic.";
    public static final String DESCRIPTION = BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/icarus.png",
            CRAFT, TRAITS, RARITY, 2, 2, 1, 3, false, Icarus.class,
            new Vector2f(122, 119), 1.55, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.BATTLECRY, AncientArtifact.TOOLTIP, Tooltip.UNLEASH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances; // for getBattlecryValue, preview the value of the created cards

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Integer> pos = SelectRandom.positionsToAdd(owner.player.getDeck().size(), 2);
                        List<CardText> cards = List.of(new AncientArtifact(), new AncientArtifact());
                        this.resolve(b, rq, el, new CreateCardResolver(cards, owner.team, CardStatus.DECK, pos));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = Collections.nCopies(2, new AncientArtifact().constructInstance(this.owner.board));
                }
                return AI.valueForAddingToDeck(this.cachedInstances, refs);
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Card> relevant = owner.player.getDeck().stream()
                                .filter(c -> c.finalTraits.contains(CardTrait.ARTIFACT))
                                .collect(Collectors.toList());
                        if (!relevant.isEmpty()) {
                            Card selection = SelectRandom.from(relevant);
                            this.resolve(b, rq, el, new PutCardResolver(selection, CardStatus.HAND, owner.team, -1, true));
                        }
                        int x = owner.finalStats.get(Stat.MAGIC);
                        Effect buff = new EffectUntilTurnEnd("+" + x + "/+0/+0 and <b>Rush</b> until the end of the turn (from <b>Unleash</b>).", EffectStats.builder()
                                .change(Stat.ATTACK, x)
                                .set(Stat.RUSH, 1)
                                .build());
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
