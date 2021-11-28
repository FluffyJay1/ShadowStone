package server.card;

import client.tooltip.*;
import server.*;
import server.card.effect.*;

public class Amulet extends BoardObject {
    public Amulet(Board b, TooltipAmulet tooltip) {
        super(b, tooltip);
        this.addEffect(true, new Effect("", tooltip.cost));
    }

    @Override
    public TooltipAmulet getTooltip() {
        return (TooltipAmulet) super.getTooltip();
    }
}
