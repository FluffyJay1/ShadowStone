package server.card.cardset.anime.bloodwarlock;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageShoot;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.effect.common.EffectStatChange;
import server.card.target.TargetList;
import server.event.Event;
import server.event.EventDamage;
import server.resolver.AddEffectResolver;
import server.resolver.BlastResolver;
import server.resolver.Resolver;
import server.resolver.TransformResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.IntStream;

public class Guts extends MinionText {
    public static final String NAME = "Guts";
    private static final String ONLISTENEVENT_DESCRIPTION = "Whenever this minion or your leader takes damage, gain +1/+1/+1. " +
            "Then if this minion has at least 5 magic, <b>Transform</b> into a <b>Guts, Berserk</b>.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: <b>Blast(X)</b>. X equals this minion's magic.";
    public static final String DESCRIPTION = "<b>Ward</b>. <b>Stalwart</b>.\n" + UNLEASH_DESCRIPTION + "\n" + ONLISTENEVENT_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/anime/guts.png",
            CRAFT, TRAITS, RARITY, 5, 2, 1, 5, false, Guts.class,
            new Vector2f(142, 147), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.WARD, Tooltip.STALWART, Tooltip.UNLEASH, Tooltip.BLAST, Tooltip.TRANSFORM, GutsBerserk.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.WARD, 1)
                .set(Stat.STALWART, 1)
                .build()) {
            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int x = owner.finalStats.get(Stat.MAGIC);
                        this.resolve(b, rq, el, new BlastResolver(effect, x, new EventAnimationDamageShoot().toString()));
                    }
                });
            }

            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event event) {
                if (event instanceof EventDamage) {
                    EventDamage ed = (EventDamage) event;
                    if(this.owner.player.getLeader().isPresent()) {
                        int count = (int) IntStream.range(0, ed.m.size())
                                .filter(i -> ed.actualDamage.get(i) > 0 && (ed.m.get(i) == this.owner.player.getLeader().get() || ed.m.get(i) == this.owner))
                                .count();
                        if (count > 0) {
                            return new ResolverWithDescription(ONLISTENEVENT_DESCRIPTION, new Resolver(false) {
                                @Override
                                public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                    Effect buff = new EffectStatChange("+1/+1/+1 (from taking damage).", 1, 1, 1);
                                    for (int i = 0; i < count; i++) {
                                        this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                                        if (owner.finalStats.get(Stat.MAGIC) >= 5) {
                                            this.resolve(b, rq, el, new TransformResolver(owner, new GutsBerserk()));
                                            break;
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
                return null;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
