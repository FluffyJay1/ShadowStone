package server.card.cardset.indie.dragondruid;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.BanishResolver;
import server.resolver.Resolver;
import server.resolver.TransformResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class Poniko extends MinionText {
    public static final String NAME = "Poniko";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: <b>Transform</b> into an <b>Uboa</b> and <b>Banish</b> all other cards in play.";
    public static final String DESCRIPTION = "<b>Disarmed</b>. <b>Ward</b>.\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/indie/poniko.png",
            CRAFT, TRAITS, RARITY, 10, 6, 3, 10, false, Poniko.class,
            new Vector2f(85, 150), 1.8, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.DISARMED, Tooltip.WARD, Tooltip.UNLEASH, Tooltip.TRANSFORM, Uboa.TOOLTIP, Tooltip.BANISH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.DISARMED, 1)
                .set(Stat.WARD, 1)
                .build()) {
            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        TransformResolver tr = this.resolve(b, rq, el, new TransformResolver(owner, new Uboa()));
                        List<BoardObject> others = b.getBoardObjects(0, false, true, true, false)
                                .filter(bo -> !tr.event.into.contains(bo))
                                .collect(Collectors.toList());
                        this.resolve(b, rq, el, new BanishResolver(others));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return (AI.valueOfStorm(Uboa.TOOLTIP.attack) + AI.VALUE_OF_BANISH * 3) / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
