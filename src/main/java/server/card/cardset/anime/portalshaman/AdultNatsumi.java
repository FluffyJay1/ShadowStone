package server.card.cardset.anime.portalshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageOrbFall;

import org.jetbrains.annotations.Nullable;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.event.EventPlayCard;
import server.resolver.EvolveResolver;
import server.resolver.Resolver;
import server.resolver.TransformResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class AdultNatsumi extends MinionText {
    public static final String NAME = "Adult Natsumi";
    private static final String ONTURNSTART_DESCRIPTION = "At the start of your turn, <b>Transform</b> into a <b>" + Natsumi.NAME + "</b>.";
    private static final String ONLISTENEVENT_DESCRIPTION = "The first time your opponent plays a minion each turn, <b>Transform</b> it into a random minion that costs M less.";
    public static final String DESCRIPTION =  "<b>Rush</b>.\n" + ONTURNSTART_DESCRIPTION + "\n" + ONLISTENEVENT_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.PORTALSHAMAN;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/anime/adultnatsumi.png"),
            CRAFT, TRAITS, RARITY, 3, 2, 1, 6, true, AdultNatsumi.class,
            new Vector2f(150, 155), 1.5, new EventAnimationDamageOrbFall(),
            () -> List.of(Tooltip.RUSH, Tooltip.TRANSFORM, Natsumi.TOOLTIP, Tooltip.UNLEASH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.RUSH, 1)
                .build()) {
            @Override
            public ResolverWithDescription onListenEventWhileInPlay(@Nullable Event event) {
                if (event instanceof EventPlayCard) {
                    EventPlayCard epc =  (EventPlayCard) event;
                    if (epc.p.team != owner.team && epc.c instanceof Minion) {
                        return new ResolverWithDescription(ONLISTENEVENT_DESCRIPTION, perTurnCounter("transform").limit(1, new Resolver(true) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                this.resolve(b, rq, el, new EvolveResolver((Minion) epc.c, -owner.finalStats.get(Stat.MAGIC)));
                            }
                        }));
                    }
                }
                return null;
            }

            @Override
            public ResolverWithDescription onTurnStartAllied() {
                return new ResolverWithDescription(ONTURNSTART_DESCRIPTION, new TransformResolver(owner, new Natsumi()));
            }

            @Override
            public double getPresenceValue(int refs) {
                return owner.finalStats.get(Stat.MAGIC);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
