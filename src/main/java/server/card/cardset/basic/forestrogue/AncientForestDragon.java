package server.card.cardset.basic.forestrogue;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageFire;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class AncientForestDragon extends MinionText {
    public static final String NAME = "Ancient Forest Dragon";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlery</b>: Give all allied cards on board <b>Elusive</b>.";
    public static final String DESCRIPTION = "<b>Ward</b>.\n" + BATTLECRY_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/ancientforestdragon.png",
            CRAFT, TRAITS, RARITY, 8, 6, 2, 8, true, AncientForestDragon.class,
            new Vector2f(126, 202), 1.5, new EventAnimationDamageFire(),
            () -> List.of(Tooltip.WARD, Tooltip.BATTLECRY, Tooltip.ELUSIVE),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.WARD, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        Effect buff = new Effect("<b>Elusive</b> (from <b>Ancient Forest Dragon</b>).", EffectStats.builder()
                                .set(Stat.ELUSIVE, 1)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(owner.player.getPlayArea(), buff));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_OF_ELUSIVE * 3;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
