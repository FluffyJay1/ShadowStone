package server.card.cardset.basic.shadowdeathknight;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.effect.common.EffectLastWordsSummon;

import java.util.List;

public class AttendantOfTheNight extends MinionText {
    public static final String NAME = "Attendant of the Night";
    public static final String LASTWORDS_DESCRIPTION = "<b>Last Words</b>: Summon a <b>" + LesserLich.NAME + "</b>.";
    public static final String OTHER_DESCRIPTION = "<b>SMOrc</b>.";
    public static final String DESCRIPTION = OTHER_DESCRIPTION + "\n" + LASTWORDS_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.SHADOWDEATHKNIGHT;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/basic/attendantofthenight.png"),
            CRAFT, TRAITS, RARITY, 3, 1, 1, 1, true, AttendantOfTheNight.class,
            new Vector2f(150, 140), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.SMORC, Tooltip.LASTWORDS, LesserLich.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(
                new Effect(OTHER_DESCRIPTION, EffectStats.builder()
                        .set(Stat.SMORC, 1)
                        .build()),
                new EffectLastWordsSummon(LASTWORDS_DESCRIPTION, new LesserLich(), 1)
        );
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
