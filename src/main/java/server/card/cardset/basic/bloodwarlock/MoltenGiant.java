package server.card.cardset.basic.bloodwarlock;

import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageFire;
import org.newdawn.slick.geom.Vector2f;
import server.Player;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.EffectWithDependentStats;

import java.util.List;

public class MoltenGiant extends MinionText {
    public static final String NAME = "Molten Giant";
    public static final String DESCRIPTION = "Costs X less. X equals the amount of health your leader is missing.";
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/moltengiant.png",
            CRAFT, TRAITS, RARITY, 15, 8, 4, 8, true, MoltenGiant.class,
            new Vector2f(150, 145), 1.3, EventAnimationDamageFire.class,
            List::of);
    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new EffectWithDependentStats(DESCRIPTION, true) {

            @Override
            public EffectStats calculateStats() {
                Player player = owner.board.getPlayer(owner.team);
                int missing = player.getLeader().map(l -> l.finalStatEffects.getStat(EffectStats.HEALTH) - l.health).orElse(0);
                return EffectStats.builder()
                        .change(EffectStats.COST, -missing)
                        .build();
            }

            @Override
            public boolean isActive() {
                return owner.status.equals(CardStatus.HAND);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
