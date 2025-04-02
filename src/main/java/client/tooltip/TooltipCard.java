package client.tooltip;

import org.newdawn.slick.geom.Vector2f;

import client.ui.Animation;
import server.card.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

// used to store metadata on a card
public abstract class TooltipCard extends Tooltip {
    public final Class<? extends CardText> cardTextClass;
    public Supplier<Animation> animation;
    public final int cost;
    public final ClassCraft craft;
    public final CardRarity rarity;
    public final List<CardTrait> traits;
    public final Vector2f artFocusPos;
    public final double artFocusScale; // if <= 0, focused image will be same as
    // original
    public final List<Function<Card, String>> trackers;

    public TooltipCard(String name, String description, Supplier<Animation> animation, ClassCraft craft, List<CardTrait> traits, CardRarity rarity, int cost,
                       Class<? extends CardText> cardTextClass, Vector2f artFocusPos, double artFocusScale, Supplier<List<Tooltip>> references,
                       List<Function<Card, String>> trackers) {
        super(name, cost + "-cost " + craft.toString() + " " + listTraits(traits) + description, references);
        this.animation = animation;
        this.craft = craft;
        this.traits = traits;
        this.rarity = rarity;
        this.cost = cost;
        this.cardTextClass = cardTextClass;
        this.artFocusPos = artFocusPos;
        this.artFocusScale = artFocusScale;
        this.trackers = trackers;
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
