package server.card.cardset.standard.havenpriest;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageClaw;
import org.newdawn.slick.geom.Vector2f;

import server.ServerBoard;
import server.card.Amulet;
import server.card.Card;
import server.card.CardRarity;
import server.card.CardStatus;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.DestroyResolver;
import server.resolver.PutCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class LionOfTheGoldenCity extends MinionText {
    public static final String NAME = "Lion of the Golden City";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Put an amulet that costs 5 or less from your hand into play and destroy it.";
    private static final String ONLISTENEVENT_DESCRIPTION = "When an allied Neutral minion comes into play while this is in your hand, subtract 1 from this minion's cost.";
    public static final String DESCRIPTION = ONLISTENEVENT_DESCRIPTION + "\n" + BATTLECRY_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/lionofthegoldencity.png"),
            CRAFT, TRAITS, RARITY, 7, 5, 3, 5, true, LionOfTheGoldenCity.class,
            new Vector2f(150, 150), 1.3, new EventAnimationDamageClaw(),
            () -> List.of(Tooltip.BATTLECRY),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 0, 1, "Choose an amulet in your hand that costs 5 or less.") {
                    @Override
                    protected boolean criteria(Card c) {
                        return c.status.equals(CardStatus.HAND) && c.team == this.getCreator().owner.team && c instanceof Amulet && c.finalStats.get(Stat.COST) <= 5
                                && c != this.getCreator().owner;
                    }
                });
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            PutCardResolver pcr = this.resolve(b, rq, el, new PutCardResolver(c, CardStatus.BOARD, owner.team, owner.getIndex() + 1, true));
                            if (pcr.event.successful.get(0)) {
                                this.resolve(b, rq, el, new DestroyResolver(c));
                            }
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 3;
            }

            @Override
            public ResolverWithDescription onListenEvent(Event event) {
                if (event == null || !this.owner.status.equals(CardStatus.HAND)) {
                    return null;
                }
                int count = (int) event.cardsEnteringPlay().stream()
                        .filter(bo -> bo.team == this.owner.team && bo.getCardText().getTooltip().craft.equals(ClassCraft.NEUTRAL) && bo instanceof Minion)
                        .count();
                if (count == 0) {
                    return null;
                }
                Effect e = new Effect("-" + count + " cost.", EffectStats.builder()
                        .change(Stat.COST, -count)
                        .build());
                return new ResolverWithDescription(ONLISTENEVENT_DESCRIPTION, new AddEffectResolver(this.owner, e));
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
