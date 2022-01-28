package server.card.cardpack.basic;

import client.Game;
import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import org.newdawn.slick.geom.Vector2f;
import server.Board;
import server.ServerBoard;
import server.card.Amulet;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.effect.Effect;
import server.card.effect.EffectLastWordsSummon;
import server.card.effect.EffectStatChange;
import server.card.effect.EffectStats;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BeastcallAria extends Amulet {
    public static final String NAME = "Beastcall Aria";
    public static final String DESCRIPTION = "<b>Countdown(3)</b>.\n<b>Last Words</b>: Summon a <b>Holy Falcon</b> and a <b>Holyflame Tiger</b>.";
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, "res/card/basic/beastcallaria.png",
            CRAFT, 2, BeastcallAria.class, new Vector2f(150, 130), 1.4,
            () -> List.of(Tooltip.COUNTDOWN, HolyFalcon.TOOLTIP, HolyflameTiger.TOOLTIP));

    public BeastcallAria(Board b) {
        super(b, TOOLTIP);
        Effect e = new EffectLastWordsSummon(DESCRIPTION, List.of(HolyFalcon.class, HolyflameTiger.class), 1);
        e.effectStats.set.setStat(EffectStats.COUNTDOWN, 3);
        this.addEffect(true, e);
    }
}
