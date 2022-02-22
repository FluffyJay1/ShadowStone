package client.tooltip;

import org.newdawn.slick.geom.Vector2f;
import server.card.*;

import java.util.List;
import java.util.function.Supplier;

// used to store metadata on a card
public abstract class TooltipCard extends Tooltip {
    public final Class<? extends Card> cardClass;
    public String imagepath;
    public final int cost;
    public final ClassCraft craft;
    public final CardRarity rarity;
    public final Vector2f artFocusPos;
    public final double artFocusScale; // if <= 0, focused image will be same as
    // original

    public TooltipCard(String name, String description, String imagepath, ClassCraft craft, CardRarity rarity, int cost,
                       Class<? extends Card> cardClass, Vector2f artFocusPos, double artFocusScale, Supplier<List<Tooltip>> references) {
        super(name, cost + "-cost " + craft.toString() + " " + description, references);
        this.imagepath = imagepath;
        this.craft = craft;
        this.rarity = rarity;
        this.cost = cost;
        this.cardClass = cardClass;
        this.artFocusPos = artFocusPos;
        this.artFocusScale = artFocusScale;
    }
}
