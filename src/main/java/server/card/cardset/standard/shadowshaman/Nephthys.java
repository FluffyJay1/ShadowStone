package server.card.cardset.standard.shadowshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import client.ui.game.visualboardanimation.eventanimation.destroy.EventAnimationDestroyDarkElectro;
import client.ui.game.visualboardanimation.eventanimation.putcard.EventAnimationPutCardNephthys;

import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.DestroyResolver;
import server.resolver.PutCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Nephthys extends MinionText {
    public static final String NAME = "Nephthys";
    public static final String DESCRIPTION = "<b>Battlecry</b>: Randomly put minions of different costs (excluding <b>" + NAME + "</b>) from your deck into play until your area is full. Then destroy those minions.";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/nephthys.png"),
            CRAFT, TRAITS, RARITY, 8, 5, 3, 5, true, Nephthys.class,
            new Vector2f(143, 137), 1.6, new EventAnimationDamageSlash(),
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
                        List<Card> selectable = owner.player.getDeck().stream().filter(c -> c instanceof Minion && !(c.getCardText() instanceof Nephthys)).toList();
                        int numOpenSpots = owner.player.maxPlayAreaSize - owner.player.getPlayArea().size();
                        List<Card> chosen = SelectRandom.havingDifferent(selectable, c -> c.finalBasicStats.get(Stat.COST), numOpenSpots);
                        List<Integer> pos = IntStream.range(0, chosen.size())
                                .map(i -> owner.getIndex() + (i + 1) * ((i + 1) % 2)) // 1 0 3 0 5 0...
                                .boxed()
                                .toList();
                        if (!chosen.isEmpty()) {
                            PutCardResolver pcr = this.resolve(b, rq, el, new PutCardResolver(chosen, CardStatus.BOARD, owner.team, pos, true, new EventAnimationPutCardNephthys()));
                            List<Card> successful = new ArrayList<>(chosen.size());
                            for (int i = 0; i < pcr.event.cards.size(); i++) {
                                if (pcr.event.successful.get(i)) {
                                    successful.add(pcr.event.cards.get(i));
                                }
                            }
                            this.resolve(b, rq, el, new DestroyResolver(successful, new EventAnimationDestroyDarkElectro()));
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
