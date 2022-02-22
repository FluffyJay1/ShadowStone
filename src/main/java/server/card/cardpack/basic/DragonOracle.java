package server.card.cardpack.basic;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class DragonOracle extends Spell {
    public static final String NAME = "Dragon Oracle";
    public static final String DESCRIPTION = "Gain one empty mana orb. If <b>Overflow</b> is active for you, draw a card.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/basic/dragonoracle.png",
            CRAFT, RARITY, 2, DragonOracle.class,
            List::of);

    public DragonOracle(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(DESCRIPTION) {
            @Override
            public Resolver battlecry() {
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
                        Player player = owner.board.getPlayer(owner.team);
                        if (player.overflow()) {
                            this.resolve(b, rl, el, new DrawResolver(player, 1));
                        }
                        b.processEvent(rl, el, new EventManaChange(player, 1, true, false));

                    }
                };
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 3;
            }
        };
        this.addEffect(true, e);
    }
}
