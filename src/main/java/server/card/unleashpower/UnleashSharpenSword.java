package server.card.unleashpower;

import java.util.*;

import client.tooltip.*;
import org.newdawn.slick.geom.Vector2f;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class UnleashSharpenSword extends UnleashPower {
    public static final String NAME = "Sharpen Sword";
    public static final String DESCRIPTION = "Give an allied minion +1/+0/+0, then <b>Unleash</b> it.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower(NAME, DESCRIPTION, "res/unleashpower/sharpensword.png",
            CRAFT, RARITY, 2, UnleashSharpenSword.class,
            new Vector2f(500, 330), 3,
            () -> List.of(Tooltip.UNLEASH));

    public UnleashSharpenSword(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(DESCRIPTION) {
            @Override
            public Resolver onUnleashPre(Minion m) {
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
                        EffectStatChange e = new EffectStatChange("+1/+0/+0 (from <b>Sharpen Sword</b>).", 1, 0, 0);
                        this.resolve(b, rl, el, new AddEffectResolver(m, e));
                    }
                };
            }
        };
        this.addEffect(true, e);
    }
}
