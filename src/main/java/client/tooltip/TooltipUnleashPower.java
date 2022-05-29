package client.tooltip;

import org.newdawn.slick.geom.Vector2f;
import server.UnleashPowerText;
import server.card.*;

import java.util.List;
import java.util.function.Supplier;

public class TooltipUnleashPower extends TooltipCard {

    public TooltipUnleashPower(String name, String description, String imagepath, ClassCraft craft, List<CardTrait> traits, CardRarity rarity, int cost,
                               Class<? extends UnleashPowerText> unleashPowerTextClass, Vector2f artFocusPos, double artFocusScale, Supplier<List<Tooltip>> references) {
        super(name, "unleash power\n \n" + description, imagepath, craft, traits, rarity, cost, unleashPowerTextClass, artFocusPos, artFocusScale, references);
    }

}
