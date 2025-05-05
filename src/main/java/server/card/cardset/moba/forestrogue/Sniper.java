package server.card.cardset.moba.forestrogue;

import java.util.List;

import org.newdawn.slick.geom.Vector2f;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageShoot;
import server.ServerBoard;
import server.ai.AI;
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
import server.event.EventPlayCard;
import server.resolver.AddEffectResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class Sniper extends MinionText {
    public static final String NAME = "Sniper";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Deal 15 damage to an enemy minion.";
    private static final String ONLISTENEVENT_DESCRIPTION = "When you play a card while this is in your hand, subtract 1 from this minion's cost.";
    public static final String DESCRIPTION = ONLISTENEVENT_DESCRIPTION + "\n" + BATTLECRY_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/moba/sniper.png"),
            CRAFT, TRAITS, RARITY, 6, 2, 2, 3, true, Sniper.class,
            new Vector2f(), -1, new EventAnimationDamageShoot(),
            () -> List.of(Tooltip.BATTLECRY),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 0, 1, DESCRIPTION) {
                    @Override
                    protected boolean criteria(Card c) {
                        return c.status.equals(CardStatus.BOARD) && c instanceof Minion && c.team != this.getCreator().owner.team;
                    }
                });
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            this.resolve(b, rq, el, new DamageResolver(effect, (Minion) c, 15, true, new EventAnimationDamageShoot()));
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.valueOfMinionDamage(15); // idk
            }

            @Override
            public ResolverWithDescription onListenEvent(Event event) {
                if (!(event instanceof EventPlayCard) || ((EventPlayCard) event).p.team != this.owner.team
                        || !this.owner.status.equals(CardStatus.HAND) || ((EventPlayCard) event).c == this.owner) {
                    return null;
                }
                Effect e = new Effect("-1 cost.", EffectStats.builder()
                        .change(Stat.COST, -1)
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

