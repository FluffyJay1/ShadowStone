package server.card.unleashpower.basic;

import java.util.*;

import client.tooltip.*;
import client.ui.Animation;

import org.newdawn.slick.geom.Vector2f;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class UnleashEchoExistence extends UnleashPowerText {
    public static final String NAME = "Echo Existence";
    public static final String DESCRIPTION = "<b>Unleash</b> an allied minion. If it has already attacked this turn, first put a copy of it on the top of your deck and subtract 3 from its cost.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALSHAMAN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower(NAME, DESCRIPTION, () -> new Animation("unleashpower/basic/echoexistence.png"),
            CRAFT, TRAITS, RARITY, 2, UnleashEchoExistence.class,
            new Vector2f(430, 445), 1.5,
            () -> List.of(Tooltip.UNLEASH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of( new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onUnleashPre(Minion m) {
                Effect effect = this; // anonymous fuckery
                String resolverDescription = "If the unleashed minion has attacked this turn, put a copy of it on the top of your deck and subtract 3 from its cost.";
                return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        if (m.attacksThisTurn > 0) {
                            Effect esc = new Effect("-3 cost (from <b>Echo Existence</b>).");
                            esc.effectStats.change.set(Stat.COST, -3);
                            this.resolve(b, rq, el, CreateCardResolver.builder()
                                    .withCard(m.getCardText())
                                    .withTeam(effect.owner.team)
                                    .withStatus(CardStatus.DECK)
                                    .withPos(0)
                                    .withAdditionalEffectForAll(esc)
                                    .build());
                        }
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
