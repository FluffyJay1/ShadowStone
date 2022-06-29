package server.card.cardset.indie.swordpaladin;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
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

import java.util.List;

public class Susie extends MinionText {
    public static final String NAME = "Susie";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Deal X + Y damage to an enemy. X equals this minion's attack, Y equals this minion's magic.";
    public static final String DESCRIPTION = "<b>Ward</b>.\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.OFFICER);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/indie/susie.png",
            CRAFT, TRAITS, RARITY, 6, 3, 1, 6, false, Susie.class,
            new Vector2f(151, 141), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.WARD, Tooltip.UNLEASH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.WARD, 1)
                .build()) {
            @Override
            public List<TargetingScheme<?>> getUnleashTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 0, 1, UNLEASH_DESCRIPTION) {
                    @Override
                    protected boolean criteria(Card c) {
                        return c.isInPlay() && c instanceof Minion && c.team != this.getCreator().owner.team;
                    }
                });
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getUnleashTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            int x = owner.finalStats.get(Stat.ATTACK);
                            int y = owner.finalStats.get(Stat.MAGIC);
                            this.resolve(b, rq, el, new DamageResolver(effect, (Minion) c, x + y, true, new EventAnimationDamageSlash().toString()));
                        });
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                int x = owner.finalStats.get(Stat.ATTACK);
                int y = owner.finalStats.get(Stat.MAGIC);
                return AI.VALUE_PER_DAMAGE * (x + y);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
