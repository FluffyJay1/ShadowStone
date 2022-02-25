package server.card.cardpack.basic;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import org.newdawn.slick.geom.Vector2f;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectLastWordsSummon;
import server.card.effect.EffectStats;

import java.util.List;

public class BeastcallAria extends AmuletText {
    public static final String NAME = "Beastcall Aria";
    public static final String DESCRIPTION = "<b>Countdown(3)</b>.\n<b>Last Words</b>: Summon a <b>Holy Falcon</b> and a <b>Holyflame Tiger</b>.";
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, "res/card/basic/beastcallaria.png",
            CRAFT, RARITY, 2, BeastcallAria.class,
            new Vector2f(150, 130), 1.4,
            () -> List.of(Tooltip.COUNTDOWN, HolyFalcon.TOOLTIP, HolyflameTiger.TOOLTIP));

    @Override
    protected List<Effect> getSpecialEffects() {
        Effect e = new EffectLastWordsSummon(DESCRIPTION, List.of(new HolyFalcon(), new HolyflameTiger()), 1);
        e.effectStats.set.setStat(EffectStats.COUNTDOWN, 3);
        return List.of(e);
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
