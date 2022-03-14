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

public class UnleashTapSoul extends UnleashPowerText {
    public static final String NAME = "Tap Soul";
    public static final String DESCRIPTION = "Deal 2 damage to your leader if <b>Vengeance</b> isn't active for you. " +
            "<b>Unleash</b> an allied minion. If <b>Vengeance</b> is active for you, this costs 2 less.";
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower(NAME, DESCRIPTION, "res/unleashpower/basic/tapsoul.png",
            CRAFT, RARITY, 2, UnleashTapSoul.class,
            new Vector2f(445, 515), 1,
            () -> List.of(Tooltip.VENGEANCE, Tooltip.UNLEASH));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            boolean vengeance;

            @Override
            public Resolver onListenEvent(Event e) {
                Effect effect = this; // anonymous fuckery
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        if (e instanceof EventDamage || e instanceof EventRestore || e instanceof EventAddEffect) {
                            Player p = b.getPlayer(effect.owner.team);
                            if (!vengeance && p.vengeance()) {
                                this.resolve(b, rq, el, new UpdateEffectStateResolver(effect, () -> vengeance = true));
                                EffectStats esc = new EffectStats();
                                esc.change.setStat(EffectStats.COST, -2);
                                this.resolve(b, rq, el, new SetEffectStatsResolver(effect, esc));
                            }
                            if (vengeance && !p.vengeance()) {
                                this.resolve(b, rq, el, new UpdateEffectStateResolver(effect, () -> vengeance = false));
                                EffectStats esc = new EffectStats();
                                esc.change.setStat(EffectStats.COST, 0);
                                this.resolve(b, rq, el, new SetEffectStatsResolver(effect, esc));
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
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        Player p = b.getPlayer(effect.owner.team);
                        if (!p.vengeance()) {
                            this.resolve(b, rq, el, new DamageResolver(effect,
                                    effect.owner.board.getPlayer(effect.owner.team).getLeader().orElse(null), 2, true, null));
                        }
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
        });
    }

    @Override
    public TooltipUnleashPower getTooltip() {
        return TOOLTIP;
    }
}
