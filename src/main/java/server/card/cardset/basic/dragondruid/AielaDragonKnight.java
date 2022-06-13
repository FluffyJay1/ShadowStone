package server.card.cardset.basic.dragondruid;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.resolver.ManaChangeResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class AielaDragonKnight extends MinionText {
    public static final String NAME = "Aiela, Dragon Knight";
    public static final String DESCRIPTION = "<b>Last Words</b>: Gain an empty mana orb.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/aieladragonknight.png",
            CRAFT, TRAITS, RARITY, 3, 2, 1, 2, true, AielaDragonKnight.class,
            new Vector2f(138, 150), 1.5, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.LASTWORDS),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription lastWords() {
                return new ResolverWithDescription(DESCRIPTION, new ManaChangeResolver(this.owner.player, 1, false, true));
            }

            @Override
            public double getLastWordsValue(int refs) {
                return AI.VALUE_PER_RAMP;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
