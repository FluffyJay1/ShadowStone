package server.card.unleashpower.special;

import client.tooltip.Tooltip;
import client.tooltip.TooltipUnleashPower;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.UnleashPowerText;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class EpitaphsProtection extends UnleashPowerText {
    public static final String NAME = "Epitaph's Protection";
    public static final String DESCRIPTION = "Give an allied minion <b>Shield(1)</b> until the end of the opponent's turn. <b>Unleash</b> it.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower(NAME, DESCRIPTION, "unleashpower/special/epitaphsprotection.png",
            CRAFT, TRAITS, RARITY, 2, EpitaphsProtection.class,
            new Vector2f(154, 92), 3,
            () -> List.of(Tooltip.SHIELD, Tooltip.UNLEASH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onUnleashPre(Minion target) {
            String resolverDescription = "Give the unleashed minion <b>Shield(1)</b> until the end of the opponent's turn.";
                return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        Effect shield = new Effect("<b>Shield(1)</b> until the end of the opponent's turn (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .change(Stat.SHIELD, 1)
                                .build(),
                                e -> e.untilTurnEndTeam = -1);
                        this.resolve(b, rq, el, new AddEffectResolver(target, shield));
                    }
                });
            }
        });
    }

    @Override
    public TooltipUnleashPower getTooltip() {
        return TOOLTIP;
    }
}
