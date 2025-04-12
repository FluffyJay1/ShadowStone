package server.card.cardset.basic.portalhunter;

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

public class MagisteelLion extends MinionText {
    public static final String NAME = "Magisteel Lion";
    public static final String DESCRIPTION = "<b>Battlecry</b>: Put 2 <b>Analyzing Artifacts</b> into your deck.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/basic/magisteellion.png"),
            CRAFT, TRAITS, RARITY, 2, 3, 1, 2, true, MagisteelLion.class,
            new Vector2f(187, 128), 1.5, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, AnalyzingArtifact.TOOLTIP),
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
                        List<CardText> cards = List.of(new AnalyzingArtifact(), new AnalyzingArtifact());
                        this.resolve(b, rq, el, new CreateCardResolver(cards, owner.team, CardStatus.DECK, pos));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = Collections.nCopies(2, new AnalyzingArtifact().constructInstance(this.owner.board));
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
