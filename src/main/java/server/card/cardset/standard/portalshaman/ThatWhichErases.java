package server.card.cardset.standard.portalshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.event.Event;
import server.event.EventCreateCard;
import server.event.EventPutCard;
import server.resolver.BanishResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;

public class ThatWhichErases extends MinionText {
    public static final String NAME = "That Which Erases";
    public static final String DESCRIPTION = "Whenever an effect puts cards into your deck, <b>Banish</b> a random enemy card in play.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALSHAMAN;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/thatwhicherases.png"),
            CRAFT, TRAITS, RARITY, 6, 6, 2, 4, true, ThatWhichErases.class,
            new Vector2f(160, 142), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BANISH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event event) {
                boolean putCards = false;
                if (event instanceof EventPutCard) {
                    EventPutCard epc = (EventPutCard) event;
                    if (epc.targetTeam == owner.team && epc.status.equals(CardStatus.DECK)) {
                        putCards = true;
                    }
                } else if (event instanceof EventCreateCard) {
                    EventCreateCard ecc = (EventCreateCard) event;
                    if (ecc.team == owner.team && ecc.status.equals(CardStatus.DECK)) {
                        putCards = true;
                    }
                }
                if (putCards) {
                    return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                        @Override
                        public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                            BoardObject selection = SelectRandom.from(b.getBoardObjects(owner.team * -1, false, true, true, true).toList());
                            if (selection != null) {
                                this.resolve(b, rq, el, new BanishResolver(selection));
                            }
                        }
                    });
                }
                return null;
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_OF_BANISH;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
