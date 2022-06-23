package server.card.cardset.standard.shadowshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.cardset.basic.shadowshaman.Skeleton;
import server.card.effect.Effect;
import server.card.effect.common.EffectLastWordsSummon;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PrinceCatacomb extends MinionText {
    public static final String NAME = "Prince Catacomb";
    public static final String DESCRIPTION = "<b>Battlecry</b>: Give all allied minions <b>Last Words</b>: Summon a <b>Skeleton</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/standard/princecatacomb.png",
            CRAFT, TRAITS, RARITY, 4, 1, 1, 1, true, PrinceCatacomb.class,
            new Vector2f(150, 160), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.LASTWORDS, Skeleton.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances; // for getBattlecryValue, preview the value of the created cards

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> relevant = b.getMinions(owner.team, false, true).collect(Collectors.toList());
                        Effect buff = new EffectLastWordsSummon("<b>Last Words</b>: Summon a <b>Skeleton</b> (from <b>Prince Catacomb</b>).", new Skeleton(), 1);
                        this.resolve(b, rq, el, new AddEffectResolver(relevant, buff));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                // idk
                if (this.cachedInstances == null) {
                    this.cachedInstances = Collections.nCopies(3, new Skeleton().constructInstance(this.owner.board));
                }
                return AI.valueForSummoning(this.cachedInstances, refs);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
