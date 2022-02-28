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

public class UnleashImbueMagic extends UnleashPowerText {
    public static final String NAME = "Imbue Magic";
    public static final String DESCRIPTION = "Give an allied minion +0/+1/+0, then <b>Unleash</b> it.";
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower(NAME, DESCRIPTION, "res/unleashpower/imbuemagic.png",
            CRAFT, RARITY, 2, UnleashImbueMagic.class,
            new Vector2f(393, 733), 1.5,
            () -> List.of(Tooltip.UNLEASH));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public Resolver onUnleashPre(Minion m) {
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        EffectStatChange e = new EffectStatChange("+0/+1/+0 (from <b>Imbue Magic</b>).", 0, 1, 0);
                        this.resolve(b, rq, el, new AddEffectResolver(m, e));
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
