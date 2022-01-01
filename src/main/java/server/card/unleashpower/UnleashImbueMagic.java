package server.card.unleashpower;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class UnleashImbueMagic extends UnleashPower {
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower("Imbue Magic",
            "Give an allied minion +0/+1/+0, then <b> Unleash </b> it.", "res/unleashpower/imbuemagic.png", CRAFT, 2,
            UnleashImbueMagic.class, Tooltip.UNLEASH);

    public UnleashImbueMagic(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect("Gives an allied minion +0/+1/+0 pre-unleash.") {
            @Override
            public Resolver onUnleashPre(Minion m) {
                return new Resolver(false) {
                    @Override
                    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                        EffectStatChange e = new EffectStatChange("+0/+1/+0 from <b> Imbue Magic. </b>", 0, 1, 0);
                        this.resolve(b, rl, el, new AddEffectResolver(m, e));
                    }
                };
            }
        };
        this.addEffect(true, e);
    }
}
