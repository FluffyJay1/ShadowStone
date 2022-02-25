package client.tooltip;

import org.newdawn.slick.geom.Vector2f;
import server.card.*;

import java.util.List;
import java.util.function.Supplier;

public class TooltipAmulet extends TooltipCard {
    public TooltipAmulet(String name, String description, String imagepath, ClassCraft craft, CardRarity rarity, int cost,
                         Class<? extends AmuletText> amuletTextClass, Vector2f artFocusPos,
                         double artFocusScale, Supplier<List<Tooltip>> references) {
        super(name, "amulet\n \n" + description, imagepath, craft, rarity, cost, amuletTextClass, artFocusPos, artFocusScale, references);
    }
}
