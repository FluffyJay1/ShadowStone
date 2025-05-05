package server.card.cardset.standard.bloodwarlock;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
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
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.PutCardResolver;
import server.resolver.Resolver;
import server.resolver.SpendResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;

public class Baphomet extends MinionText {
    public static final String NAME = "Baphomet";
    public static final String DESCRIPTION = "<b>Battlecry</b>: Put a random Bloodwarlock minion with at least 5 attack from your deck into your hand. Then <b>Spend(3)</b> to subtract 3 from its cost.";
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/baphomet.png"),
            CRAFT, TRAITS, RARITY, 2, 3, 1, 1, true, Baphomet.class,
            new Vector2f(161, 144), 1.5, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.SPEND),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Card> relevant = owner.player.getDeck().stream()
                                .filter(c -> c instanceof Minion && c.finalStats.get(Stat.ATTACK) >= 5)
                                .toList();
                        Card selection = SelectRandom.from(relevant);
                        if (selection != null) {
                            this.resolve(b, rq, el, new PutCardResolver(selection, CardStatus.HAND, owner.team, -1, true));
                            Effect buff = new Effect("-3 cost (from <b>" + NAME + "</b>).", EffectStats.builder()
                                    .change(Stat.COST, -3)
                                    .build());
                            this.resolve(b, rq, el, new SpendResolver(effect, 3, new AddEffectResolver(selection, buff)));
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.canSpendAfterPlayed(3);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
