package server.card.cardpack.basic;

import java.util.*;
import java.util.stream.Collectors;

import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class WeatheredVanguard extends Minion {
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final TooltipMinion TOOLTIP = new TooltipMinion("Weathered Vanguard",
            "<b>Battlecry</b>: Summon 2 <b>Knights</b>.\n<b>Unleash</b>: Give all allied minions +1/+0/+1.",
            "res/card/basic/weatheredvanguard.png", CRAFT, 6, 4, 2, 4, false, WeatheredVanguard.class,
            new Vector2f(155, 120), 1.6, EventAnimationDamageSlash.class,
            Tooltip.BATTLECRY, Knight.TOOLTIP, Tooltip.UNLEASH);

    public WeatheredVanguard(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(TOOLTIP.description) {
            @Override
            public Resolver battlecry() {
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
                        List<Card> knights = List.of(new Knight(b), new Knight(b));
                        List<Integer> pos = List.of(owner.getIndex() + 1, owner.getIndex());
                        this.resolve(b, rl, el, new CreateCardResolver(knights, owner.team, CardStatus.BOARD, pos));
                    }
                };
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 2.;
            }

            @Override
            public Resolver unleash() {
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
                        List<Minion> minions = b.getMinions(owner.team, false, true).collect(Collectors.toList());
                        if (!minions.isEmpty()) {
                            Effect stats = new EffectStatChange("+1/+0/+1 (from <b>Weathered Vanguard's Unleash</b>).", 1, 0, 1);
                            this.resolve(b, rl, el, new AddEffectResolver(minions, stats));
                        }
                    }
                };
            }

            @Override
            public double getPresenceValue(int refs) {
                // can hit 6 units, avg probably hit half of them, and unleash costs 2
                return AI.VALUE_PER_1_1_STATS * 6 / 2. / 2.;
            }
        };
        this.addEffect(true, e);
    }

}
