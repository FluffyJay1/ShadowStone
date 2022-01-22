package server.card.cardpack.basic;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.Board;
import server.card.Card;
import server.card.CardStatus;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.effect.Effect;
import server.card.effect.EffectAura;
import server.card.effect.EffectStats;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;

public class Beastmaster extends Minion {
    public static final String NAME = "Beastmaster";
    public static final String DESCRIPTION = "<b>Aura</b>: adjacent minions have +1 attacks per turn.\n<b>Unleash</b>: summon a <b>Stonetusk Boar</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/beastmaster.png",
            CRAFT, 4, 2, 0, 4, false, Beastmaster.class, new Vector2f(140, 100), 2, EventAnimationDamageSlash.class,
            Tooltip.AURA, Tooltip.UNLEASH, StonetuskBoar.TOOLTIP);

    public Beastmaster(Board b) {
        super(b, TOOLTIP);
        Effect auraBuff = new Effect("+1 attacks per turn (from <b>Beastmaster's Aura</b>).");
        auraBuff.effectStats.change.setStat(EffectStats.ATTACKS_PER_TURN, 1);
        Effect e = new EffectAura(DESCRIPTION, 1, true, false, auraBuff) {
            @Override
            public boolean applyConditions(Card cardToApply) {
                return cardToApply instanceof Minion && Math.abs(cardToApply.getIndex() - this.owner.getIndex()) == 1;
            }

            @Override
            public Resolver unleash() {
                return new CreateCardResolver(new StonetuskBoar(b), owner.team, CardStatus.BOARD, owner.getIndex() + 1);
            }

            @Override
            public double getPresenceValue(int refs) {
                return 3;
            }
        };
        this.addEffect(true, e);
    }
}
