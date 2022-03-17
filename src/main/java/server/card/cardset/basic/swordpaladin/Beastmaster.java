package server.card.cardset.basic.swordpaladin;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.card.*;
import server.card.cardset.basic.neutral.StonetuskBoar;
import server.card.effect.Effect;
import server.card.effect.EffectAura;
import server.card.effect.EffectStats;
import server.resolver.CreateCardResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class Beastmaster extends MinionText {
    public static final String NAME = "Beastmaster";
    public static final String DESCRIPTION = "<b>Aura</b>: adjacent minions have +1 attacks per turn.\n<b>Unleash</b>: summon a <b>Stonetusk Boar</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/beastmaster.png",
            CRAFT, RARITY, 4, 2, 0, 4, false, Beastmaster.class,
            new Vector2f(140, 100), 2, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.AURA, Tooltip.UNLEASH, StonetuskBoar.TOOLTIP));

    @Override
    protected List<Effect> getSpecialEffects() {
        Effect auraBuff = new Effect("+1 attacks per turn (from <b>Beastmaster's Aura</b>).");
        auraBuff.effectStats.change.setStat(EffectStats.ATTACKS_PER_TURN, 1);
        return List.of(new EffectAura(DESCRIPTION, 1, true, false, auraBuff) {
            @Override
            public boolean applyConditions(Card cardToApply) {
                return cardToApply instanceof Minion && Math.abs(cardToApply.getIndex() - this.owner.getIndex()) == 1;
            }

            @Override
            public ResolverWithDescription unleash() {
                String resolverDescription = "<b>Unleash</b>: summon a <b>Stonetusk Boar</b>.";
                return new ResolverWithDescription(resolverDescription, new CreateCardResolver(new StonetuskBoar(), owner.team, CardStatus.BOARD, owner.getIndex() + 1));
            }

            @Override
            public double getPresenceValue(int refs) {
                return 3;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
