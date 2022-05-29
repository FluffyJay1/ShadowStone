package server.card.cardset.basic.swordpaladin;

import java.util.*;
import java.util.stream.Collectors;

import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.card.effect.common.EffectStatChange;
import server.card.target.TargetList;
import server.event.*;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

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
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                String resolverDescription = "<b>Battlecry</b>: Summon 2 <b>Knights</b>.";
                return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<CardText> knights = List.of(new Knight(), new Knight());
                        List<Integer> pos = List.of(owner.getIndex() + 1, owner.getIndex());
                        this.resolve(b, rq, el, new CreateCardResolver(knights, owner.team, CardStatus.BOARD, pos));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 2.;
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                String resolverDescription = "<b>Unleash</b>: Give all allied minions +1/+0/+1.";
                return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> minions = b.getMinions(owner.team, false, true).collect(Collectors.toList());
                        if (!minions.isEmpty()) {
                            Effect stats = new EffectStatChange("+1/+0/+1 (from <b>Weathered Vanguard's Unleash</b>).", 1, 0, 1);
                            this.resolve(b, rq, el, new AddEffectResolver(minions, stats));
                        }
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                // can hit 6 units, avg probably hit half of them, and unleash costs 2
                return AI.valueForBuff(1, 0, 1) * 6 / 2. / 2.;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
