package server.card.cardset.basic.shadowdeathknight;

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
import server.card.effect.common.EffectLastWordsSummon;

import java.util.Collections;
import java.util.List;

public class BoneChimera extends MinionText {
    public static final String NAME = "Bone Chimera";
    public static final String DESCRIPTION = "<b>Last Words</b>: Summon 2 <b>Skeletons</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWDEATHKNIGHT;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/basic/bonechimera.png"),
            CRAFT, TRAITS, RARITY, 2, 1, 1, 1, true, BoneChimera.class,
            new Vector2f(125, 152), 1.25, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.LASTWORDS, Skeleton.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new EffectLastWordsSummon(DESCRIPTION, Collections.nCopies(2, new Skeleton()), 1));
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
