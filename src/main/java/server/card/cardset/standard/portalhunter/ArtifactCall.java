package server.card.cardset.standard.portalhunter;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.PutCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ArtifactCall extends SpellText {
    public static final String NAME = "Artifact Call";
    public static final String DESCRIPTION = "Put a random Artifact from your deck into your hand. If <b>Resonance</b> is active for you, put 2 instead.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/standard/artifactcall.png",
            CRAFT, TRAITS, RARITY, 2, ArtifactCall.class,
            () -> List.of(Tooltip.RESONANCE),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Card> relevant = owner.player.getDeck().stream()
                                .filter(c -> c.finalTraits.contains(CardTrait.ARTIFACT))
                                .collect(Collectors.toList());
                        if (!relevant.isEmpty()) {
                            List<Card> selection = SelectRandom.from(relevant, owner.player.resonance() ? 2 : 1);
                            List<Integer> pos = Collections.nCopies(selection.size(), -1);
                            this.resolve(b, rq, el, new PutCardResolver(selection, CardStatus.HAND, owner.team, pos, true));
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND * 1.5;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return owner.player.resonance();
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
