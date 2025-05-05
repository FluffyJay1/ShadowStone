package server.card.cardset.standard.havenpriest;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageFire;

import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;

public class RedHotBoots extends AmuletText {
    public static final String NAME = "Red Hot Boots";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Deal 4 damage to an enemy minion.";
    private static final String LASTWORDS_DESCRIPTION = "<b>Last Words</b>: Deal 2 damage to a random enemy minion.";
    public static final String DESCRIPTION = "<b>Countdown(3)</b>.\n" + BATTLECRY_DESCRIPTION + "\n" + LASTWORDS_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, () -> new Animation("card/standard/redhotboots.png"),
            CRAFT, TRAITS, RARITY, 2, RedHotBoots.class,
            new Vector2f(145, 150), 1.5,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.BATTLECRY, Tooltip.LASTWORDS),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.COUNTDOWN, 3)
                .build()){
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 0, 1, "Deal 4 damage to an enemy minion.") {
                    @Override
                    protected boolean criteria(Card c) {
                        return c.status.equals(CardStatus.BOARD) && c instanceof Minion && c.team != this.getCreator().owner.team;
                    }
                });
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            this.resolve(b, rq, el, new DamageResolver(effect, (Minion) c, 4, true, new EventAnimationDamageFire()));
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.valueOfMinionDamage(4);
            }

            @Override
            public ResolverWithDescription lastWords() {
                Effect effect = this;
                return new ResolverWithDescription(LASTWORDS_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> possibleTargets = b.getMinions(owner.team * -1, false, true).toList();
                        if (!possibleTargets.isEmpty()) {
                            Minion target = SelectRandom.from(possibleTargets);
                            this.resolve(b, rq, el, new DamageResolver(effect, target, 2, true, new EventAnimationDamageFire()));
                        }
                    }
                });
            }

            @Override
            public double getLastWordsValue(int refs) {
                return AI.valueOfMinionDamage(2);
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
