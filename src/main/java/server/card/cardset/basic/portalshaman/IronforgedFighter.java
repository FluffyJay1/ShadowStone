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
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.Collections;
import java.util.List;

public class IronforgedFighter extends MinionText {
    public static final String NAME = "Ironforged Fighter";
    public static final String DESCRIPTION = "<b>Battlecry</b>: Put 2 <b>Radiant Artifacts</b> into your deck.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALSHAMAN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/basic/ironforgedfighter.png"),
            CRAFT, TRAITS, RARITY, 4, 5, 1, 3, true, IronforgedFighter.class,
            new Vector2f(174, 139), 1.4, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, RadiantArtifact.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances; // for getBattlecryValue, preview the value of the created cards

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Integer> pos = SelectRandom.positionsToAdd(owner.player.getDeck().size(), 2);
                        List<CardText> cards = List.of(new RadiantArtifact(), new RadiantArtifact());
                        this.resolve(b, rq, el, CreateCardResolver.builder()
                                .withCards(cards)
                                .withTeam(owner.team)
                                .withStatus(CardStatus.DECK)
                                .withPos(pos)
                                .build());
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = Collections.nCopies(2, new RadiantArtifact().constructInstance(this.owner.board));
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
