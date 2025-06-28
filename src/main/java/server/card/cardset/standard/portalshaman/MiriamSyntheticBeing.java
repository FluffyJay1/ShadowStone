package server.card.cardset.standard.portalshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.*;
import server.card.cardset.basic.portalshaman.PrimeArtifact;
import server.card.cardset.basic.portalshaman.RadiantArtifact;
import server.card.effect.Effect;
import server.card.target.ModalOption;
import server.card.target.ModalTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.PutCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;

public class MiriamSyntheticBeing extends MinionText {
    public static final String NAME = "Miriam, Synthetic Being";
    private static final String DESCRIPTION = "<b>Battlecry</b>: If <b>Resonance</b> is active for you, <b>Choose</b> to either put a <b>" + RadiantArtifact.NAME + "</b> and a <b>" + PrimeArtifact.NAME + "</b> into your deck, " +
            "or put a random Artifact card from your deck into your hand.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALSHAMAN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/miriamsyntheticbeing.png"),
            CRAFT, TRAITS, RARITY, 2, 3, 1, 1, true, MiriamSyntheticBeing.class,
            new Vector2f(140, 140), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.RESONANCE, Tooltip.CHOOSE, RadiantArtifact.TOOLTIP, PrimeArtifact.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new ModalTargetingScheme(this, 1, "<b>Choose</b> 1", List.of(
                        new ModalOption("Put a <b>" + RadiantArtifact.NAME + "</b> and <b>" + PrimeArtifact.NAME + "</b> into your deck."),
                        new ModalOption("Put a random Artifact card from your deck into your hand.")
                )) {
                    @Override
                    public boolean isApplicable(List<TargetList<?>> alreadyTargeted) {
                        return owner.player.resonance();
                    }
                });
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, java.util.List<Event> el) {
                        if (owner.player.resonance()) {
                            targetList.get(0).targeted.stream().findFirst().ifPresent(o -> {
                                int selection = (int) o;
                                if (selection == 0) {
                                    List<CardText> cards = List.of(new RadiantArtifact(), new PrimeArtifact());
                                    List<Integer> pos = SelectRandom.positionsToAdd(owner.player.getDeck().size(), 2);
                                    this.resolve(b, rq, el, CreateCardResolver.builder()
                                            .withCards(cards)
                                            .withTeam(owner.team)
                                            .withStatus(CardStatus.DECK)
                                            .withPos(pos)
                                            .build());
                                } else {
                                    Card artifact = SelectRandom.from(owner.player.getDeck().stream()
                                            .filter(c -> c.finalTraits.contains(CardTrait.ARTIFACT))
                                            .toList());
                                    if (artifact != null) {
                                        this.resolve(b, rq, el, new PutCardResolver(artifact, CardStatus.HAND, owner.team, -1, true));
                                    }
                                }
                            });
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 1;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return owner.player.resonance();
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
