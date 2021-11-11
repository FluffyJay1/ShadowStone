package server.card.unleashpower;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class UnleashTapSoul extends UnleashPower {
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower("Tap Soul",
            " Deal 2 damage to your leader. <b> Unleash </b> an allied minion. If <b> Vengeance </b> is active for you, this can be used once more per turn.",
            "res/unleashpower/tapsoul.png", CRAFT, 1, UnleashTapSoul.class, Tooltip.UNLEASH, Tooltip.VENGEANCE);

    public UnleashTapSoul(Board b) {
        super(b, TOOLTIP);
        Effect vengeanceBonus = new Effect("When Vengeance is active, this can be used once more per turn.") {
            boolean vengeance;

            @Override
            public Resolver onListenEvent(Event e) {
                Effect effect = this; // anonymous fuckery
                return new Resolver(false) {
                    @Override
                    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                        if (e instanceof EventDamage || e instanceof EventRestore || e instanceof EventAddEffect) {
                            if (!vengeance && p.vengeance()) {
								b.processEvent(rl, el, new EventFlag(owner));
                                vengeance = true;
                                Effect esc = new Effect();
                                esc.change.setStat(EffectStats.ATTACKS_PER_TURN, 1);
                                this.resolve(b, rl, el, new SetEffectStatsResolver(effect, esc));
                            }
                            if (vengeance && !p.vengeance()) {
								b.processEvent(rl, el, new EventFlag(owner));
                                vengeance = false;
                                Effect esc = new Effect();
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
                    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                        this.resolve(b, rl, el, new EffectDamageResolver(effect,
                                effect.owner.board.getPlayer(effect.owner.team).leader, 2, true));
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
