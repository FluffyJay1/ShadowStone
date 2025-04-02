package server.card.cardset.anime.runemage;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSplash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class PhantomOfFate extends MinionText {
    public static final String NAME = "Phantom of Fate";
    private static final String LASTWORDS_DESCRIPTION = "<b>Last Words</b>: Deal M damage to all enemy minions.";
    public static final String DESCRIPTION = "<b>Ward</b>. <b>Countdown(1)</b>.\n" + LASTWORDS_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/anime/phantomoffate.png"),
            CRAFT, TRAITS, RARITY, 3, 0, 0, 1, true, PhantomOfFate.class,
            new Vector2f(), -1, new EventAnimationDamageSplash(),
            () -> List.of(Tooltip.WARD, Tooltip.COUNTDOWN, Tooltip.LASTWORDS),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.WARD, 1)
                .set(Stat.COUNTDOWN, 1)
                .build()) {
            @Override
            public ResolverWithDescription lastWords() {
                Effect effect = this;
                return new ResolverWithDescription(LASTWORDS_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int x = owner.finalStats.get(Stat.MAGIC);
                        List<Minion> relevant = b.getMinions(owner.team * -1, false, true).collect(Collectors.toList());
                        this.resolve(b, rq, el, new DamageResolver(effect, relevant, x, true, new EventAnimationDamageSplash().toString()));
                    }
                });
            }

            @Override
            public double getLastWordsValue(int refs) {
                return AI.valueOfMinionDamage(owner.finalStats.get(Stat.MAGIC)) * 3;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
