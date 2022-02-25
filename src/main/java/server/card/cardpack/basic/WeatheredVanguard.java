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

public class WeatheredVanguard extends MinionText {
    public static final String NAME = "Weathered Vanguard";
    public static final String DESCRIPTION = "<b>Battlecry</b>: Summon 2 <b>Knights</b>.\n<b>Unleash</b>: Give all allied minions +1/+0/+1.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/weatheredvanguard.png",
            CRAFT, RARITY, 6, 4, 2, 4, false, WeatheredVanguard.class,
            new Vector2f(155, 120), 1.6, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.BATTLECRY, Knight.TOOLTIP, Tooltip.UNLEASH));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public Resolver battlecry() {
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
                        List<CardText> knights = List.of(new Knight(), new Knight());
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
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
