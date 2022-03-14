package server.card.unleashpower.basic;

import java.util.*;

import client.tooltip.*;
import org.newdawn.slick.geom.Vector2f;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;
import server.resolver.util.ResolverQueue;

public class UnleashEchoExistence extends UnleashPowerText {
    public static final String NAME = "Echo Existence";
    public static final String DESCRIPTION = "<b>Unleash</b> an allied minion. If it has already attacked this turn, add a copy of it to your deck and subtract 2 from its cost.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower(NAME, DESCRIPTION, "res/unleashpower/basic/echoexistence.png",
            CRAFT, RARITY, 2, UnleashEchoExistence.class,
            new Vector2f(430, 445), 1.5,
            () -> List.of(Tooltip.UNLEASH));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of( new Effect(DESCRIPTION) {
            @Override
            public Resolver onUnleashPost(Minion m) {
                Effect effect = this; // anonymous fuckery
                return new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        if (m.attacksThisTurn > 0) {
                            CreateCardResolver ccr = new CreateCardResolver(m.cardText, effect.owner.team, CardStatus.DECK,
                                    (int) (effect.owner.board.getPlayer(effect.owner.team).getDeck().size() * Math.random()));
                            this.resolve(b, rq, el, ccr);
                            Effect esc = new Effect("-2 cost (from <b>Echo Existence</b>).");
                            esc.effectStats.change.setStat(EffectStats.COST, -2);
                            this.resolve(b, rq, el, new AddEffectResolver(ccr.event.successfullyCreatedCards, esc));
                        }
                    }
                };
            }
        });
    }

    @Override
    public TooltipUnleashPower getTooltip() {
        return TOOLTIP;
    }
}
