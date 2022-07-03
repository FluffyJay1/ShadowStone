package server.card.cardset.anime.runemage;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageMagicHit;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageOrbFall;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;
import server.resolver.AddEffectResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;
import java.util.stream.Collectors;

public class Yoshino extends MinionText {
    public static final String NAME = "Yoshino Himekawa";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Gain +0/+X/+0. X equals the number of times this card has been <b>Spellboosted</b>.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Deal 1 damage to a random enemy minion X times. X equals this minion's magic.";
    public static final String DESCRIPTION = "<b>Ward</b>. <b>Freezing Touch</b>.\n" + BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/anime/yoshino.png",
            CRAFT, TRAITS, RARITY, 5, 3, 0, 6, false, Yoshino.class,
            new Vector2f(165, 143), 1.6, new EventAnimationDamageMagicHit(),
            () -> List.of(Tooltip.WARD, Tooltip.FREEZING_TOUCH, Tooltip.BATTLECRY, Tooltip.SPELLBOOST, Tooltip.UNLEASH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.WARD, 1)
                .set(Stat.FREEZING_TOUCH, 1)
                .set(Stat.SPELLBOOSTABLE, 1)
                .build()) {

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int x = owner.spellboosts;
                        Effect buff = new Effect("+0/+" + x + "/+0 (from <b>Battlecry</b>).", EffectStats.builder()
                                .change(Stat.MAGIC, x)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                    }
                });
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        b.pushEventGroup(new EventGroup(EventGroupType.CONCURRENTDAMAGE));
                        int x = owner.finalStats.get(Stat.MAGIC);
                        for (int i = 0; i < x; i++) {
                            List<Minion> choices = owner.board.getMinions(owner.team * -1, false, true).collect(Collectors.toList());
                            if (!choices.isEmpty()) {
                                Minion choice = SelectRandom.from(choices);
                                this.resolve(b, rq, el, new DamageResolver(effect, choice, 1, true,
                                        new EventAnimationDamageMagicHit().toString()));
                            }
                        }
                        b.popEventGroup();
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.valueOfMinionDamage(1) * this.owner.finalStats.get(Stat.MAGIC) / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
