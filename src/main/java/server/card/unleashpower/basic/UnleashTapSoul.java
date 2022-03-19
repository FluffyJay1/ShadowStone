package server.card.unleashpower.basic;

import java.util.*;

import client.tooltip.*;
import org.newdawn.slick.geom.Vector2f;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
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
        Effect discount = new Effect("Costs 2 less because <b>Vengeance</b> is active.", new EffectStats(
                new EffectStats.Setter(EffectStats.COST, true, -2)
        ));
        return List.of(new EffectAura(DESCRIPTION, 1, false, false, false, true, discount) {

            @Override
            public boolean applyConditions(Card cardToApply) {
                return cardToApply.board.getPlayer(cardToApply.team).vengeance();
            }

            @Override
            public ResolverWithDescription onUnleashPre(Minion m) {
                Effect effect = this; // anonymous fuckery
                return new ResolverWithDescription("Deal 2 damage to your leader if <b>Vengeance</b> isn't active for you.", new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        Player p = b.getPlayer(effect.owner.team);
                        if (!p.vengeance()) {
                            this.resolve(b, rq, el, new DamageResolver(effect,
                                    effect.owner.board.getPlayer(effect.owner.team).getLeader().orElse(null), 2, true, null));
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