package client.tooltip;

import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamage;
import network.EmoteSet;
import org.newdawn.slick.geom.Vector2f;
import server.card.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class TooltipLeader extends TooltipMinion {
    public EmoteSet emoteSet;

    public TooltipLeader(String name, String description, String imagepath, ClassCraft craft, List<CardTrait> traits,
                         CardRarity rarity, Class<? extends MinionText> minionTextClass, Vector2f artFocusPos,
                         double artFocusScale, EventAnimationDamage attackAnimation, EmoteSet emoteSet) {
        super(name, description,
                imagepath, craft, traits, rarity, 0, 0, 0, 25, false, minionTextClass,
                artFocusPos, artFocusScale, attackAnimation, List::of, List.of());
        this.emoteSet = emoteSet;
    }
}
