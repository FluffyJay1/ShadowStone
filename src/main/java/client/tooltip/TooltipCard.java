package client.tooltip;

import org.newdawn.slick.geom.Vector2f;
import server.card.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

// used to store metadata on a card
public abstract class TooltipCard extends Tooltip {
    public final Class<? extends CardText> cardTextClass;
    public String imagepath;
    public final int cost;
    public final ClassCraft craft;
    public final CardRarity rarity;
    public final List<CardTrait> traits;
    public final Vector2f artFocusPos;
    public final double artFocusScale; // if <= 0, focused image will be same as
    // original

    public TooltipCard(String name, String description, String imagepath, ClassCraft craft, List<CardTrait> traits, CardRarity rarity, int cost,
                       Class<? extends CardText> cardTextClass, Vector2f artFocusPos, double artFocusScale, Supplier<List<Tooltip>> references) {
        super(name, cost + "-cost " + craft.toString() + " " + listTraits(traits) + description, references);
        this.imagepath = imagepath;
        this.craft = craft;
        this.traits = traits;
        this.rarity = rarity;
        this.cost = cost;
        this.cardTextClass = cardTextClass;
        this.artFocusPos = artFocusPos;
        this.artFocusScale = artFocusScale;
    }

    public static String listTraits(Collection<CardTrait> traits) {
        if (traits.isEmpty()) {
            return "";
        }
        Iterator<CardTrait> iter = traits.iterator();
        StringBuilder sb = new StringBuilder(iter.next().toString());
        while (iter.hasNext()) {
            sb.append(", ").append(iter.next().toString());
        }
        sb.append(" ");
        return sb.toString();
    }
}
