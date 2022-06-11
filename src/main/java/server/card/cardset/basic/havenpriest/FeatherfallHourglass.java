package server.card.cardset.basic.havenpriest;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import org.newdawn.slick.geom.Vector2f;
import server.ai.AI;
import server.card.AmuletText;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.effect.common.EffectLastWordsSummon;
import server.card.target.TargetList;
import server.resolver.AddEffectResolver;
import server.resolver.SpendResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class FeatherfallHourglass extends AmuletText {
    public static final String NAME = "Featherfall Hourglass";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: <b>Spend(2)</b> to subtract 3 from this amulet's <b>Countdown</b>.";
    private static final String LASTWORDS_DESCRIPTION = "<b>Last Words</b>: Summon a <b>Time Owl</b>.";
    public static final String DESCRIPTION = "<b>Countdown(3)</b>.\n" + BATTLECRY_DESCRIPTION + "\n" + LASTWORDS_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, "res/card/basic/featherfallhourglass.png",
            CRAFT, TRAITS, RARITY, 1, FeatherfallHourglass.class,
            new Vector2f(145, 143), 1.2,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.BATTLECRY, Tooltip.SPEND, Tooltip.LASTWORDS, TimeOwl.TOOLTIP));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect("<b>Countdown(3)</b>.\n" + BATTLECRY_DESCRIPTION, EffectStats.builder()
                .set(Stat.COUNTDOWN, 3)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect countdownSubtract = new Effect("", EffectStats.builder()
                        .change(Stat.COUNTDOWN, -3)
                        .build());
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION,
                        new SpendResolver(this, 2, new AddEffectResolver(this.owner, countdownSubtract)));
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.valueOfRush(TimeOwl.TOOLTIP.attack) / 2;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.canSpendAfterPlayed(2);
            }
        }, new EffectLastWordsSummon(LASTWORDS_DESCRIPTION, new TimeOwl(), 1));
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
