package client.tooltip;

import org.newdawn.slick.geom.Vector2f;

import client.ui.Animation;
import server.card.*;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class TooltipSpell extends TooltipCard {
    public TooltipSpell(String name, String description, Supplier<Animation> animation, ClassCraft craft, List<CardTrait> traits, CardRarity rarity, int cost,
                        Class<? extends SpellText> spellTextClass, Supplier<List<Tooltip>> references, List<Function<Card, String>> trackers) {
        super(name, "spell\n \n" + description, animation, craft, traits, rarity, cost, spellTextClass, new Vector2f(), -1, references, trackers);
    }
}
