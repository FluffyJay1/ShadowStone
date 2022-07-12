package server.card.cardset.basic.shadowshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.resolver.BanishResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class Ghost extends MinionText {
    public static final String NAME = "Ghost";
    public static final String DESCRIPTION = "<b>Storm</b>.\nAt the end of your turn, <b>Banish</b> this minion.\n" +
            "When this minion would be destroyed, <b>Banish</b> it instead.";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/basic/ghost.png",
            CRAFT, TRAITS, RARITY, 1, 1, 1, 1, false, Ghost.class,
            new Vector2f(124, 142), 1.3, null,
            () -> List.of(Tooltip.STORM, Tooltip.BANISH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.STORM, 1)
                .set(Stat.BANISH_ON_DESTROY, 1)
                .build()) {
            @Override
            public ResolverWithDescription onTurnEndAllied() {
                String resolverDescription = "At the end of your turn, <b>Banish</b> this minion.";
                return new ResolverWithDescription(resolverDescription, new BanishResolver(this.owner));
            }

            @Override
            public double getPresenceValue(int refs) {
                return -0.5;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
