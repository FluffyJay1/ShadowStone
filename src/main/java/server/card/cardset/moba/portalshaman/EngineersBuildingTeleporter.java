package server.card.cardset.moba.portalshaman;

import java.util.List;

import org.newdawn.slick.geom.Vector2f;

import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageDefault;
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
import server.event.Event;
import server.resolver.PutCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

public class EngineersBuildingTeleporter extends MinionText {
    public static final String NAME = "Engineer's Building: Teleporter";
    public static final String DESCRIPTION = "At the start of your turn, put a random minion that costs M or less from your deck into play.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALSHAMAN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.ARTIFACT);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/moba/engineersbuildingteleporter.png"),
            CRAFT, TRAITS, RARITY, 1, 0, 2, 4, true, EngineersBuildingTeleporter.class,
            new Vector2f(), -1, new EventAnimationDamageDefault(),
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onTurnStartAllied() {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int x = owner.finalStats.get(Stat.MAGIC);
                        List<Card> eligible = owner.player.getDeck().stream()
                                .filter(c -> c instanceof Minion && c.finalStats.get(Stat.COST) <= x)
                                .toList();
                        if (!eligible.isEmpty()) {
                            Card target = SelectRandom.from(eligible);
                            this.resolve(b, rq, el, new PutCardResolver(target, CardStatus.BOARD, owner.team, owner.getIndex() + 1, true));
                        }
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return owner.finalStats.get(Stat.MAGIC);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
