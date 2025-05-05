package server.card.cardset.standard.portalhunter;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;

import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.PutCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;
import java.util.stream.IntStream;

public class TylleTheWorldgate extends MinionText {
    public static final String NAME = "Tylle, the Worldgate";
    public static final String DESCRIPTION = "<b>Battlecry</b>: Randomly put different Artifact minions from your deck into play until your area is full.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/tylletheworldgate.png"),
            CRAFT, TRAITS, RARITY, 9, 2, 3, 2, true, TylleTheWorldgate.class,
            new Vector2f(130, 150), 1.4, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Card> selectable = owner.player.getDeck().stream().filter(c -> c instanceof Minion && c.finalTraits.contains(CardTrait.ARTIFACT)).toList();
                        int numOpenSpots = owner.player.maxPlayAreaSize - owner.player.getPlayArea().size();
                        List<Card> chosen = SelectRandom.havingDifferent(selectable, Card::getCardText, numOpenSpots);
                        List<Integer> pos = IntStream.range(0, chosen.size())
                                .map(i -> owner.getIndex() + (i + 1) * ((i + 1) % 2)) // 1 0 3 0 5 0...
                                .boxed()
                                .toList();
                        if (!chosen.isEmpty()) {
                            this.resolve(b, rq, el, new PutCardResolver(chosen, CardStatus.BOARD, owner.team, pos, true));
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                // it does some serious shit
                return 8;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
