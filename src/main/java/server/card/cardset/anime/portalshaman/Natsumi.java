package server.card.cardset.anime.portalshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageOrbFall;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.*;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.TransformResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class Natsumi extends MinionText {
    public static final String NAME = "Natsumi Kyouno";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: <b>Transform</b> into <b>" + AdultNatsumi.NAME + "</b> with +M/+M/+M.";
    public static final String DESCRIPTION =  "<b>Rush</b>.\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.PORTALSHAMAN;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/anime/natsumi.png"),
            CRAFT, TRAITS, RARITY, 3, 1, 2, 4, false, Natsumi.class,
            new Vector2f(150, 155), 1.6, new EventAnimationDamageOrbFall(),
            () -> List.of(Tooltip.RUSH, Tooltip.UNLEASH, Tooltip.TRANSFORM, AdultNatsumi.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.RUSH, 1)
                .build()) {
            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        TransformResolver tr = this.resolve(b, rq, el, new TransformResolver(owner, new AdultNatsumi()));
                        int m = owner.finalStats.get(Stat.MAGIC);
                        Effect buff = new Effect("+" + m + "/+" + m + "/+" + m + ". (From <b>" + NAME + "</b>.)", EffectStats.builder()
                                .change(Stat.ATTACK, m)
                                .change(Stat.MAGIC, m)
                                .change(Stat.HEALTH, m)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(tr.event.into.get(0), buff));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return owner.finalStats.get(Stat.MAGIC) + 1;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
