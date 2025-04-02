package server.card.cardset.standard.shadowshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.DestroyResolver;
import server.resolver.Resolver;
import server.resolver.RestoreResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;
import java.util.stream.Collectors;

public class UnderworldWatchmanKhawy extends MinionText {
    public static final String NAME = "Underworld Watchman Khawy";
    public static final String LASTWORDS_DESCRIPTION = "<b>Last Words</b>: Destroy a random enemy minion with the highest attack in play then restore X health to your leader. " +
            "X equals that minion's attack.";
    public static final String DESCRIPTION = "<b>Ward</b>.\n" + LASTWORDS_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/underworldwatchmankhawy.png"),
            CRAFT, TRAITS, RARITY, 7, 4, 2, 5, true, UnderworldWatchmanKhawy.class,
            new Vector2f(150, 200), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.LASTWORDS),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.WARD, 1)
                .build()) {
            @Override
            public ResolverWithDescription lastWords() {
                Effect effect = this;
                return new ResolverWithDescription(LASTWORDS_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        Minion target = SelectRandom.oneOfWith(b.getMinions(owner.team * -1, false, true).collect(Collectors.toList()),
                                m -> m.finalStats.get(Stat.ATTACK), Integer::max);
                        if (target != null) {
                            this.resolve(b, rq, el, new DestroyResolver(target));
                            owner.player.getLeader().ifPresent(l -> {
                                this.resolve(b, rq, el, new RestoreResolver(effect, l, target.finalStats.get(Stat.ATTACK)));
                            });
                        }
                    }
                });
            }

            @Override
            public double getLastWordsValue(int refs) {
                int heal = this.owner.board.getMinions(owner.team * -1, false, true)
                        .map(m -> m.finalStats.get(Stat.ATTACK))
                        .max(Integer::compareTo)
                        .orElse(0);
                return AI.VALUE_OF_DESTROY + AI.VALUE_PER_HEAL * heal;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
