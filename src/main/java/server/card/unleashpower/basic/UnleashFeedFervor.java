package server.card.unleashpower.basic;

import java.util.*;

import client.tooltip.*;
import org.newdawn.slick.geom.Vector2f;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

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
        Effect discount = new Effect("Costs 1 less because <b>Overflow</b> is active.", new EffectStats(
                new EffectStats.Setter(EffectStats.COST, true, -1)
        ));
        return List.of(new EffectAura(DESCRIPTION, 1, false, false, false, true, discount) {
            @Override
            public boolean applyConditions(Card cardToApply) {
                return cardToApply.board.getPlayer(cardToApply.team).overflow();
            }
        });
    }

    @Override
    public TooltipUnleashPower getTooltip() {
        return TOOLTIP;
    }
}
