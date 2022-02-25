package server.card;

import client.tooltip.*;
import server.*;
import server.card.effect.*;

public class Amulet extends BoardObject {
    public Amulet(Board b, AmuletText amuletText) {
        super(b, amuletText);
    }

    @Override
    public TooltipAmulet getTooltip() {
        return (TooltipAmulet) super.getTooltip();
    }
}
