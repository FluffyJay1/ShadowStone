package server.card.cardset.standard.swordpaladin;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.SpendResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class Cuhullin extends MinionText {
    public static final String NAME = "Cuhullin";
    public static final String DESCRIPTION = "<b>Rush</b>.\n<b>Battlecry</b>: <b>Spend(4)</b> to gain <b>Bane</b> and <b>Shield(4)</b> until the end of the turn.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.OFFICER);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/standard/cuhullin.png",
            CRAFT, TRAITS, RARITY, 2, 2, 1, 2, true, Cuhullin.class,
            new Vector2f(161, 137), 1.4, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.RUSH, Tooltip.BATTLECRY, Tooltip.SPEND, Tooltip.BANE, Tooltip.SHIELD),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.RUSH, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                String resolverDescription = "<b>Battlecry</b>: <b>Spend(4)</b> to gain <b>Bane</b> and <b>Shield(4)</b> until the end of the turn.";
                return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        Effect buff = new Effect("<b>Bane</b> and <b>Shield(4)</b> until the end of the turn (from <b>Battlecry</b>).",
                                EffectStats.builder()
                                        .set(Stat.BANE, 1)
                                        .change(Stat.SHIELD, 4)
                                        .build(),
                                e -> e.untilTurnEndTeam = 0);
                        this.resolve(b, rq, el, new SpendResolver(effect, 4, new AddEffectResolver(owner, buff)));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_OF_DESTROY / 4;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.canSpendAfterPlayed(4);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
