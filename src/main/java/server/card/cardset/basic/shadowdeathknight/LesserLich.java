package server.card.cardset.basic.shadowdeathknight;

import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;

import java.util.List;

public class LesserLich extends MinionText {
    public static final String NAME = "Lesser Lich";
    public static final String DESCRIPTION = "";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWDEATHKNIGHT;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/basic/lesserlich.png"),
            CRAFT, TRAITS, RARITY, 4, 4, 2, 4, true, LesserLich.class,
            new Vector2f(144, 144), 1.5, new EventAnimationDamageSlash(),
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of();
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
