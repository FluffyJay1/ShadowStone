package server.card.cardset.basic.swordpaladin;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.common.EffectLastWordsSummon;
import server.card.target.TargetList;
import server.resolver.AddEffectResolver;
import server.resolver.SpendResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.Collections;
import java.util.List;

public class Jeno extends MinionText {
    public static final String NAME = "Jeno, Levin Vanguard";
    public static final String DESCRIPTION = "<b>Rush</b>.\n<b>Battlecry</b>: <b>Spend(2)</b> to gain <b>Last Words<b>: Summon 2 <b>Knights</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.OFFICER);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/jeno.png",
            CRAFT, TRAITS, RARITY, 4, 4, 1, 3, true, Jeno.class,
            new Vector2f(132, 130), 1.4, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.RUSH, Tooltip.BATTLECRY, Tooltip.SPEND, Tooltip.LASTWORDS, Knight.TOOLTIP));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(EffectStats.RUSH, 1)
                .build()) {
            private List<Card> cachedInstances; // for getBattlecryValue, preview the value of the created cards

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                String resolverDescription = "<b>Battlecry</b>: <b>Spend(2)</b> to gain <b>Last Words<b>: Summon 2 <b>Knights</b>.";
                Effect summon = new EffectLastWordsSummon("<b>Last Words</b>: Summon 2 <b>Knights</b> (from <b>Battlecry</b>).", List.of(new Knight(), new Knight()), 1);
                return new ResolverWithDescription(resolverDescription, new SpendResolver(this, 2, new AddEffectResolver(this.owner, summon)));
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = Collections.nCopies(2, new Knight().constructInstance(this.owner.board));
                }
                return AI.valueForSummoning(this.cachedInstances, refs) / 2;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.canSpendAfterPlayed(2);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
