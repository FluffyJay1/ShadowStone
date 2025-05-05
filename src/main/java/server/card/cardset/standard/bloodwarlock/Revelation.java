package server.card.cardset.standard.bloodwarlock;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageAOEFire;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.EffectWithDependentStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class Revelation extends SpellText {
    public static final String NAME = "Revelation";
    private static final String BATTLECRY_DESCRIPTION = "Deal 8 damage to all minions.";
    private static final String DISCOUNT_DESCRIPTION = "If <b>Vengeance</b> is active for you, this costs 4 less.";
    public static final String DESCRIPTION =  BATTLECRY_DESCRIPTION + "\n" + DISCOUNT_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/standard/revelation.png"),
            CRAFT, TRAITS, RARITY, 8, Revelation.class,
            () -> List.of(Tooltip.VENGEANCE),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new EffectWithDependentStats(DESCRIPTION, true) {
            @Override
            public EffectStats calculateStats() {
                if (this.owner.player.vengeance()) {
                    return EffectStats.builder()
                            .change(Stat.COST, -4)
                            .build();
                }
                return new EffectStats();
            }

            @Override
            public boolean isActive() {
                return this.owner.status.equals(CardStatus.HAND);
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> targets = b.getMinions(0, false, true).collect(Collectors.toList());
                        this.resolve(b, rq, el, new DamageResolver(effect, targets, 8, true, new EventAnimationDamageAOEFire(0, false)));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.valueOfMinionDamage(8) * 4;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.player.vengeance();
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
