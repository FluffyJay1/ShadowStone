package server.card.cardset.standard.forestrogue;

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
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.PutCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.Collections;
import java.util.List;

public class KingElephant extends MinionText {
    public static final String NAME = "King Elephant";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Return all allied minions to your hand, then gain +1/+1/+1 for each card in your hand.";
    public static final String DESCRIPTION = "<b>Storm</b>. Ignores <b>Ward</b>.\n" + BATTLECRY_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/kingelephant.png"),
            CRAFT, TRAITS, RARITY, 10, 1, 1, 1, true, KingElephant.class,
            new Vector2f(150, 160), 1.5, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.STORM, Tooltip.WARD, Tooltip.BATTLECRY),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.STORM, 1)
                .set(Stat.IGNORE_WARD, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> relevant = b.getMinions(owner.team, false, true)
                                .filter(m -> m != owner)
                                .toList();
                        if (!relevant.isEmpty()) {
                            List<Integer> positions = Collections.nCopies(relevant.size(), -1);
                            this.resolve(b, rq, el, new PutCardResolver(relevant, CardStatus.HAND, owner.team, positions, true));
                        }
                        int x = owner.player.getHand().size();
                        Effect buff = new Effect("+" + x + "/+" + x + "/+" + x + " (from <b>Battlecry</b>).", EffectStats.builder()
                                        .change(Stat.ATTACK, x)
                                        .change(Stat.MAGIC, x)
                                        .change(Stat.HEALTH, x)
                                        .build());
                        this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                int x = owner.player.getHand().size();
                return AI.valueForBuff(x, x, x);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
