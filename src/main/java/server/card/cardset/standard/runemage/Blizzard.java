package server.card.cardset.standard.runemage;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageIceFall;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class Blizzard extends SpellText {
    public static final String NAME = "Blizzard";
    public static final String BATTLECRY_DESCRIPTION = "Deal 2 damage to all enemy minions.";
    public static final String OTHER_DESCRIPTION = "<b>Freezing Touch</b>.";
    public static final String DESCRIPTION = OTHER_DESCRIPTION + "\n" + BATTLECRY_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/standard/blizzard.png"),
            CRAFT, TRAITS, RARITY, 6, Blizzard.class,
            () -> List.of(Tooltip.FREEZING_TOUCH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.FREEZING_TOUCH, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> targets = b.getMinions(owner.team * -1, false, true).toList();
                        this.resolve(b, rq, el, new DamageResolver(effect, targets, 2, true, new EventAnimationDamageIceFall()));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return (AI.valueOfMinionDamage(2) + AI.VALUE_OF_FREEZE) * 3;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
