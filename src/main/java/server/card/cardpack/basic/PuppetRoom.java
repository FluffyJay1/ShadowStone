package server.card.cardpack.basic;

import client.tooltip.*;
import server.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
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
            public double getBattlecryValue() {
                return AI.VALUE_PER_CARD_IN_HAND / 2.;
            }

            @Override
            public Resolver onTurnEnd() {
                 return new CreateCardResolver(new Puppet(this.owner.board), this.owner.team, CardStatus.HAND, -1);
            }

            @Override
            public double getPresenceValue() {
                return AI.VALUE_PER_CARD_IN_HAND * this.owner.finalStatEffects.getStat(EffectStats.COUNTDOWN) / 2.;
            }
        };
        e.effectStats.set.setStat(EffectStats.COUNTDOWN, 3);
        this.addEffect(true, e);
    }
}
