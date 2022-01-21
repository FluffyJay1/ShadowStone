package server.card.unleashpower;

import java.util.*;

import client.tooltip.*;
import org.newdawn.slick.geom.Vector2f;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class UnleashTapSoul extends UnleashPower {
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower("Tap Soul",
            "Deal 2 damage to your leader. <b>Unleash</b> an allied minion. If <b>Vengeance</b> is active for you, this can be used once more per turn.",
            "res/unleashpower/tapsoul.png", CRAFT, 1, UnleashTapSoul.class, new Vector2f(445, 515), 1,
            Tooltip.UNLEASH, Tooltip.VENGEANCE);

    public UnleashTapSoul(Board b) {
        super(b, TOOLTIP);
        Effect vengeanceBonus = new Effect(TOOLTIP.description) {
            boolean vengeance;

            @Override
            public Resolver onListenEvent(Event e) {
                Effect effect = this; // anonymous fuckery
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
                        if (e instanceof EventDamage || e instanceof EventRestore || e instanceof EventAddEffect) {
                            Player p = b.getPlayer(effect.owner.team);
                            if (!vengeance && p.vengeance()) {
                                this.resolve(b, rl, el, new UpdateEffectStateResolver(effect, () -> vengeance = true));
                                EffectStats esc = new EffectStats();
                                esc.change.setStat(EffectStats.ATTACKS_PER_TURN, 1);
                                this.resolve(b, rl, el, new SetEffectStatsResolver(effect, esc));
                            }
                            if (vengeance && !p.vengeance()) {
                                this.resolve(b, rl, el, new UpdateEffectStateResolver(effect, () -> vengeance = false));
                                EffectStats esc = new EffectStats();
                                esc.change.setStat(EffectStats.ATTACKS_PER_TURN, 0);
                                this.resolve(b, rl, el, new SetEffectStatsResolver(effect, esc));
                            }
                        }
                    }
                };
            }

            @Override
            public Resolver onUnleashPre(Minion m) {
                Effect effect = this; // anonymous fuckery
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
                        this.resolve(b, rl, el, new EffectDamageResolver(effect,
                                effect.owner.board.getPlayer(effect.owner.team).getLeader().orElse(null), 2, true, null));
                    }
                };

            }

            @Override
            public String extraStateString() {
                return this.vengeance + " ";
            }

            @Override
            public void loadExtraState(Board b, StringTokenizer st) {
                this.vengeance = Boolean.parseBoolean(st.nextToken());
            }
        };
        this.addEffect(true, vengeanceBonus);

    }
}
