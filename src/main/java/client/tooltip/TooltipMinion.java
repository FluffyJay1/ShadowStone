package client.tooltip;

import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamage;
import org.newdawn.slick.geom.*;

import server.card.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TooltipMinion extends TooltipCard {
    public final int attack;
    public final int magic;
    public final int health;
    public final boolean basicUnleash;
    public final Class<? extends EventAnimationDamage> attackAnimation;

    public TooltipMinion(String name, String description, String imagepath, ClassCraft craft, CardRarity rarity, int cost, int attack,
                         int magic, int health, boolean basicUnleash, Class<? extends MinionText> minionTextClass, Vector2f artFocusPos,
                         double artFocusScale, Class<? extends EventAnimationDamage> attackAnimation, Supplier<List<Tooltip>> references) {
        super(name,
                "minion\nA:" + attack + ", M:" + magic + ", H:" + health + "\n \n" + description + (basicUnleash
                        ? "\n <b>Unleash</b>: Deal X damage to an enemy minion. X equals this minion's magic.\n"
                        : ""),
                imagepath, craft, rarity, cost, minionTextClass, artFocusPos, artFocusScale, references);
        this.basicUnleash = basicUnleash;
        if (basicUnleash) {
            this.references = () -> {
                List<Tooltip> supplied = references.get();
                List<Tooltip> ret = new ArrayList<>(supplied.size() + 1);
                ret.addAll(supplied);
                if (!ret.contains(Tooltip.UNLEASH)) {
                    ret.add(Tooltip.UNLEASH);
                }
                return ret;
            };
        } else {
            this.references = references;
        }
        this.attack = attack;
        this.magic = magic;
        this.health = health;
        this.attackAnimation = attackAnimation;
    }
}
