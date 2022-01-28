package client.tooltip;

import org.newdawn.slick.geom.Vector2f;
import server.card.*;

import java.util.List;
import java.util.function.Supplier;

public class TooltipUnleashPower extends TooltipCard {

    public TooltipUnleashPower(String name, String description, String imagepath, ClassCraft craft, int cost,
                               Class<? extends Card> cardClass, Vector2f artFocusPos, double artFocusScale, Supplier<List<Tooltip>> references) {
        super(name, "unleash power\n \n" + description, imagepath, craft, cost, cardClass, artFocusPos, artFocusScale, references);
    }

}
