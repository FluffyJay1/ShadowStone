package server.card.cardset.basic.havenpriest;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;

import java.util.List;

public class HolyFalcon extends MinionText {
    public static final String NAME = "Holy Falcon";
    public static final String DESCRIPTION = "<b>Storm</b>.";
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/basic/holyfalcon.png"),
            CRAFT, TRAITS, RARITY, 3, 3, 0, 1, true, HolyFalcon.class,
            new Vector2f(150, 150), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.STORM),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.STORM, 1)
                .build()
        ));
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
