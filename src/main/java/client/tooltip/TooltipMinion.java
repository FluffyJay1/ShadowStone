package client.tooltip;

import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamage;
import org.newdawn.slick.geom.*;

import server.card.*;

public class TooltipMinion extends TooltipCard {
    public final int attack;
    public final int magic;
    public final int health;
    public final boolean basicUnleash;
    public final Class<? extends EventAnimationDamage> attackAnimation;

    public TooltipMinion(String name, String description, String imagepath, ClassCraft craft, int cost, int attack,
                         int magic, int health, boolean basicUnleash, Class<? extends Card> cardClass, Vector2f artFocusPos,
                         double artFocusScale, Class<? extends EventAnimationDamage> attackAnimation, Tooltip... references) {
        super(name,
                "minion\nA:" + attack + ", M:" + magic + ", H:" + health + "\n \n" + description + (basicUnleash
                        ? "\n <b>Unleash</b>: Deal X damage to an enemy minion. X equals this minion's magic.\n"
                        : ""),
                imagepath, craft, cost, cardClass, artFocusPos, artFocusScale);
        this.basicUnleash = basicUnleash;
        if (basicUnleash) {
            this.references = new Tooltip[references.length + 1];
            this.references[references.length] = Tooltip.UNLEASH;
            System.arraycopy(references, 0, this.references, 0, references.length);
        } else {
            this.references = references;
        }
        this.attack = attack;
        this.magic = magic;
        this.health = health;
        this.attackAnimation = attackAnimation;
    }
}
