package server.card.cardset.basic.portalhunter;

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
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.resolver.DrawResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class MysticArtifact extends MinionText {
    public static final String NAME = "Mystic Artifact";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Draw a card.";
    public static final String DESCRIPTION = "<b>Ward</b>.\n" + BATTLECRY_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.ARTIFACT);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/basic/mysticartifact.png",
            CRAFT, TRAITS, RARITY, 2, 2, 1, 3, true, MysticArtifact.class,
            new Vector2f(150, 138), 1.4, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.WARD, Tooltip.BATTLECRY),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.WARD, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new DrawResolver(this.owner.player, 1));
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
