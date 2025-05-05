package server.card.cardset.standard.bloodwarlock;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.Card;
import server.card.CardRarity;
import server.card.CardStatus;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.PutCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;
import java.util.stream.IntStream;

public class DarkAirjammer extends MinionText {
    public static final String NAME = "Dark Airjammer";
    public static final String DESCRIPTION = "<b>Battlecry</b>: Put a random minion that costs 2 or less from your deck into play. Repeat once if <b>Vengeance</b> is active for you.";
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/darkairjammer.png"),
            CRAFT, TRAITS, RARITY, 5, 4, 2, 4, true, DarkAirjammer.class,
            new Vector2f(130, 160), 1.2, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.VENGEANCE),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int num = owner.player.vengeance() ? 2 : 1;
                        List<Card> targets = SelectRandom.from(owner.player.getDeck().stream()
                                .filter(c -> c instanceof Minion && c.finalStats.get(Stat.COST) <= 2)
                                .toList(), num);
                        List<Integer> pos = IntStream.range(0, num)
                                .map(i -> owner.getIndex() + (i + 1) * ((i + 1) % 2)) // 1 0 3 0 5 0...
                                .boxed()
                                .toList();
                        if (!targets.isEmpty()) {
                            this.resolve(b, rq, el, new PutCardResolver(targets, CardStatus.BOARD, owner.team, pos, true));
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return owner.player.vengeance() ? 3 : 1.5;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return owner.player.vengeance();
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
