package server.card.cardset.basic.bloodwarlock;

import java.util.*;

import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.card.effect.common.EffectLastWordsAlliedBlast;
import server.card.target.TargetList;
import server.event.*;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class CursedStone extends MinionText {
    public static final String NAME = "Cursed Stone";
    public static final String DESCRIPTION = "<b>Unleash</b>: <b>Blast(X)</b> and gain <b>Last Words</b>: Deal X damage to a random allied minion. X equals the amount of health your leader is missing.";
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/cursedstone.png",
            CRAFT, RARITY, 8, 1, 5, 8, false, CursedStone.class,
            new Vector2f(), -1, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.UNLEASH, Tooltip.BLAST, Tooltip.LASTWORDS));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                Effect effect = this; // anonymous fuckery
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        Player player = owner.board.getPlayer(owner.team);
                        int missing = player.getLeader().map(l -> l.finalStatEffects.getStat(EffectStats.HEALTH) - l.health).orElse(0);
                        this.resolve(b, rq, el, new BlastResolver(effect, missing, null));
                        Effect lw = new EffectLastWordsAlliedBlast("<b>Unleash</b>", missing);
                        this.resolve(b, rq, el, new AddEffectResolver(effect.owner, lw));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                Player player = owner.board.getPlayer(owner.team);
                int missing = player.getLeader().map(l ->l.finalStatEffects.getStat(EffectStats.HEALTH) - l.health).orElse(0);
                return AI.VALUE_PER_DAMAGE * missing / 2.;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
