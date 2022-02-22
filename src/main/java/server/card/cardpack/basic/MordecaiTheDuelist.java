package server.card.cardpack.basic;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.Board;
import server.card.CardRarity;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.effect.Effect;
import server.card.effect.EffectLastWordsSummon;

import java.util.List;

public class MordecaiTheDuelist extends Minion {
    public static final String NAME = "Mordecai the Duelist";
    public static final String DESCRIPTION = "<b>Last Words</b>: Summon a <b>Mordecai the Duelist</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/mordecaitheduelist.png",
            CRAFT, RARITY, 8, 5, 2, 5, true, MordecaiTheDuelist.class,
            new Vector2f(150, 155), 1.5, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.LASTWORDS));

    public MordecaiTheDuelist(Board b) {
        super(b, TOOLTIP);
        Effect e = new EffectLastWordsSummon(DESCRIPTION, MordecaiTheDuelist.class, 1);
        this.addEffect(true, e);
    }
}
