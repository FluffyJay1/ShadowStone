package server.card.cardset.standard.dragondruid;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageDoubleSlice;
import org.newdawn.slick.geom.Vector2f;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.EffectWithDependentStats;
import server.card.effect.Stat;

import java.util.List;

public class DarkDragoonForte extends MinionText {
    public static final String NAME = "Dark Dragoon Forte";
    public static final String DESCRIPTION = "<b>Storm</b>. Has <b>Intimidate</b> if <b>Overflow</b> is active for you.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/darkdragoonforte.png"),
            CRAFT, TRAITS, RARITY, 5, 5, 2, 1, true, DarkDragoonForte.class,
            new Vector2f(142, 126), 1.5, new EventAnimationDamageDoubleSlice(),
            () -> List.of(Tooltip.STORM, Tooltip.INTIMIDATE, Tooltip.OVERFLOW),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new EffectWithDependentStats(DESCRIPTION, true, EffectStats.builder()
                .set(Stat.STORM, 1)
                .build()) {

            @Override
            public EffectStats calculateStats() {
                if (owner.player.overflow()) {
                    return EffectStats.builder()
                            .set(Stat.STORM, 1)
                            .set(Stat.INTIMIDATE, 1)
                            .build();
                }
                return this.baselineStats;
            }

            @Override
            public boolean isActive() {
                return owner.isInPlay();
            }
            
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
