package server.card.cardset.standard.swordpaladin;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectAura;
import server.card.effect.EffectStats;
import server.card.effect.Stat;

import java.util.List;

public class CaptainLecia extends MinionText {
    public static final String NAME = "Captain Lecia";
    public static final String DESCRIPTION = "<b>Aura</b>: Allied Officer minions have <b>Stalwart</b> and <b>Repel</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.COMMANDER);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/captainlecia.png"),
            CRAFT, TRAITS, RARITY, 3, 3, 1, 3, true, CaptainLecia.class,
            new Vector2f(140, 150), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.AURA, Tooltip.STALWART, Tooltip.REPEL),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new EffectAura(DESCRIPTION, 1, true, false,
                new Effect("<b>Stalwart</b> and <b>Repel</b> (from </b>" + NAME + "</b>).", EffectStats.builder()
                        .set(Stat.STALWART, 1)
                        .set(Stat.REPEL, 1)
                        .build())) {
            @Override
            public boolean applyConditions(Card cardToApply) {
                return cardToApply instanceof Minion && cardToApply.finalTraits.contains(CardTrait.OFFICER);
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_OF_STALWART + AI.VALUE_OF_REPEL;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
