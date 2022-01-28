package server.card.cardpack.basic;

import client.tooltip.*;
import org.newdawn.slick.geom.Vector2f;
import server.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.resolver.*;

import java.util.List;

public class PuppetRoom extends Amulet {
    public static final String NAME = "Puppet Room";
    public static final String DESCRIPTION = "<b>Countdown(3)</b>.\n<b>Battlecry</b>: put a <b>Puppet</b> in your hand.\nAt the end of your turn, put a <b>Puppet</b> in your hand.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, "res/card/basic/puppetroom.png",
            CRAFT, 3, PuppetRoom.class, new Vector2f(), -1,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.BATTLECRY, Puppet.TOOLTIP));

    public PuppetRoom(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(DESCRIPTION) {
            @Override
            public Resolver battlecry() {
                return new CreateCardResolver(new Puppet(this.owner.board), this.owner.team, CardStatus.HAND, -1);
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND;
            }

            @Override
            public Resolver onTurnEnd() {
                 return new CreateCardResolver(new Puppet(this.owner.board), this.owner.team, CardStatus.HAND, -1);
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND * 3;
            }
        };
        e.effectStats.set.setStat(EffectStats.COUNTDOWN, 3);
        this.addEffect(true, e);
    }
}
