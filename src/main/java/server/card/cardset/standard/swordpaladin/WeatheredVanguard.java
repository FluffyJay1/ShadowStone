package server.card.cardset.standard.swordpaladin;

import java.util.*;

import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.ai.AI;
import server.card.*;
import server.card.cardset.basic.swordpaladin.HeavyKnight;
import server.card.effect.*;
import server.card.target.TargetList;
import server.event.*;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class WeatheredVanguard extends MinionText {
    public static final String NAME = "Weathered Vanguard";
    public static final String DESCRIPTION = "<b>Battlecry</b>: <b>Spend(3)</b> to summon 3 <b>Heavy Knights</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.COMMANDER);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/weatheredvanguard.png"),
            CRAFT, TRAITS, RARITY, 3, 2, 1, 4, true, WeatheredVanguard.class,
            new Vector2f(155, 120), 1.6, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.SPEND, HeavyKnight.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances; // for getBattlecryValue, preview the value of the created cards

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new SpendResolver(this, 3, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<CardText> knights = Collections.nCopies(3, new HeavyKnight());
                        List<Integer> pos = Collections.nCopies(3, owner.getIndex() + 1);
                        this.resolve(b, rq, el, new CreateCardResolver(knights, owner.team, CardStatus.BOARD, pos));
                    }
                }));
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = Collections.nCopies(3, new HeavyKnight().constructInstance(this.owner.board));
                }
                return AI.valueForSummoning(this.cachedInstances, refs) / 3;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.canSpendAfterPlayed(3);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
