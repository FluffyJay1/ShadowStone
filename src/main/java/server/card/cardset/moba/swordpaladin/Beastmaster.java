package server.card.cardset.moba.swordpaladin;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.card.*;
import server.card.cardset.standard.neutral.StonetuskBoar;
import server.card.effect.Effect;
import server.card.effect.EffectAura;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.resolver.CreateCardResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class Beastmaster extends MinionText {
    public static final String NAME = "Beastmaster";
    public static final String DESCRIPTION = "<b>Aura</b>: adjacent minions have +1 attacks per turn.\n<b>Unleash</b>: summon a <b>Stonetusk Boar</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/moba/beastmaster.png",
            CRAFT, TRAITS, RARITY, 4, 2, 0, 4, false, Beastmaster.class,
            new Vector2f(140, 100), 2, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.AURA, Tooltip.UNLEASH, StonetuskBoar.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        Effect auraBuff = new Effect("+1 attacks per turn (from <b>Beastmaster's Aura</b>).");
        auraBuff.effectStats.change.set(Stat.ATTACKS_PER_TURN, 1);
        return List.of(new EffectAura(DESCRIPTION, 1, true, false, auraBuff) {
            @Override
            public boolean applyConditions(Card cardToApply) {
                return cardToApply instanceof Minion && Math.abs(cardToApply.getIndex() - this.owner.getIndex()) == 1;
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
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
