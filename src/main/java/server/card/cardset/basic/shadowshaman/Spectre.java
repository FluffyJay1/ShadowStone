package server.card.cardset.basic.shadowshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;

import java.util.List;

public class Spectre extends MinionText {
    public static final String NAME = "Spectre";
    public static final String DESCRIPTION = "<b>Rush</b>";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/spectre.png",
            CRAFT, RARITY, 2, 2, 1, 2, true, Spectre.class,
            new Vector2f(150, 180), 1.2, null,
            () -> List.of(Tooltip.RUSH));
    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, new EffectStats(
                new EffectStats.Setter(EffectStats.RUSH, false, 1)
        )));
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
