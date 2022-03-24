package server.card.unleashpower.basic;

import java.util.*;

import client.tooltip.*;
import org.newdawn.slick.geom.Vector2f;
import server.*;
import server.card.*;
import server.card.effect.*;

public class UnleashFeedFervor extends UnleashPowerText {
    public static final String NAME = "Feed Fervor";
    public static final String DESCRIPTION = "<b>Unleash</b> an allied minion. If <b>Overflow</b> is active for you, this costs 1 less.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower(NAME, DESCRIPTION, "res/unleashpower/basic/feedfervor.png",
            CRAFT, RARITY, 2, UnleashFeedFervor.class,
            new Vector2f(420, 210), 8,
            () -> List.of(Tooltip.UNLEASH, Tooltip.OVERFLOW));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new EffectWithDependentStats("If <b>Overflow</b> is active for you, this costs 1 less.", true) {
            @Override
            public EffectStats calculateStats() {
                Player p = this.owner.board.getPlayer(this.owner.team);
                if (p.overflow()) {
                    return new EffectStats(
                            new EffectStats.Setter(EffectStats.COST, true, -1)
                    );
                }
                return new EffectStats();
            }

            @Override
            public boolean isActive() {
                return this.owner.isInPlay();
            }
        });
    }

    @Override
    public TooltipUnleashPower getTooltip() {
        return TOOLTIP;
    }
}
