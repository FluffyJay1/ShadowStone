package server.card.unleashpower;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class UnleashSharpenSword extends UnleashPower {
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower("Sharpen Sword",
            "Give an allied minion +1/+0/+0, then <b> Unleash </b> it.", "res/unleashpower/sharpensword.png", CRAFT, 2,
            UnleashSharpenSword.class, Tooltip.UNLEASH);

    public UnleashSharpenSword(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect("Gives an allied minion +1/+0/+0 pre-unleash.") {
            @Override
            public Resolver onUnleashPre(Minion m) {
                return new Resolver(false) {
                    @Override
                    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                        EffectStatChange e = new EffectStatChange("+1/+0/+0 from <b> Sharpen Sword. </b>", 1, 0, 0);
                        this.resolve(b, rl, el, new AddEffectResolver(m, e));
                    }
                };
            }
        };
        this.addEffect(true, e);
    }
}
