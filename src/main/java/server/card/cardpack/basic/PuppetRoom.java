package server.card.cardpack.basic;

import java.util.List;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.Event;
import server.event.EventFlag;
import server.resolver.*;

public class PuppetRoom extends Amulet {
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;

    public static final TooltipAmulet TOOLTIP = new TooltipAmulet("Puppet Room",
            "<b> Countdown(3). Battlecry: </b> put a <b> Puppet </b> in your hand. At the end of your turn, put a <b> Puppet </b> in your hand.",
            "res/card/basic/puppetroom.png", CRAFT, 3, PuppetRoom.class, Tooltip.COUNTDOWN, Tooltip.BATTLECRY,
            Puppet.TOOLTIP);

    public PuppetRoom(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(TOOLTIP.description) {
            @Override
            public Resolver battlecry() {
                return new CreateCardResolver(new Puppet(this.owner.board), this.owner.team, CardStatus.HAND, -1);
            }

            @Override
            public Resolver onTurnEnd() {
                 return new CreateCardResolver(new Puppet(this.owner.board), this.owner.team, CardStatus.HAND, -1);
            }
        };
        e.set.setStat(EffectStats.COUNTDOWN, 3);
        this.addEffect(true, e);
    }
}
