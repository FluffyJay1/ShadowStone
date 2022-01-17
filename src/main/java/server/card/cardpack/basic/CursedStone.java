package server.card.cardpack.basic;

import java.util.*;

import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class CursedStone extends Minion {
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final TooltipMinion TOOLTIP = new TooltipMinion("Cursed Stone",
            "<b>Unleash</b>: <b>Blast(X)</b> and gain <b>Last Words</b>: Deal X damage to a random allied minion. X equals the amount of health your leader is missing.",
            "res/card/basic/cursedstone.png", CRAFT, 5, 1, 5, 5, false, CursedStone.class, new Vector2f(), -1,
            EventAnimationDamageSlash.class,
            Tooltip.UNLEASH, Tooltip.BLAST, Tooltip.LASTWORDS);

    public CursedStone(Board b) {
        super(b, TOOLTIP);
        /*
         * it's called cursed stone not because of the stone itself, but because of the
         * anonymous classes
         */
        Effect e = new Effect(TOOLTIP.description) {
            @Override
            public Resolver unleash() {
                Effect effect = this; // anonymous fuckery
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
                        Player player = owner.board.getPlayer(owner.team);
                        int missing = player.getLeader().map(l -> l.finalStatEffects.getStat(EffectStats.HEALTH) - l.health).orElse(0);
                        this.resolve(b, rl, el, new BlastResolver(effect, missing, null));
                        Effect lw = new EffectLastWordsAlliedBlast("<b>Unleash</b>", missing);
                        this.resolve(b, rl, el, new AddEffectResolver(effect.owner, lw));
                    }
                };
            }

            @Override
            public double getPresenceValue() {
                Player player = owner.board.getPlayer(owner.team);
                int missing = player.getLeader().map(l ->l.finalStatEffects.getStat(EffectStats.HEALTH) - l.health).orElse(0);
                return AI.VALUE_PER_DAMAGE * missing / 2.;
            }
        };
        this.addEffect(true, e);
    }

}
