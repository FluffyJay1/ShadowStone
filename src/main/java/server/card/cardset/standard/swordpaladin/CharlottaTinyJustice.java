package server.card.cardset.standard.swordpaladin;

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
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class CharlottaTinyJustice extends MinionText {
    public static final String NAME = "Charlotta, Tiny Justice";
    public static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Give all allied minions <b>Repel</b> until the end of the opponent's turn, and gain +M/+0/+0 and <b>Rush</b> until the end of the turn.";
    public static final String DESCRIPTION = "<b>Ward</b>.\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.COMMANDER);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/charlottatinyjustice.png"),
            CRAFT, TRAITS, RARITY, 3, 2, 1, 4, false, CharlottaTinyJustice.class,
            new Vector2f(156, 171), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.WARD, Tooltip.UNLEASH, Tooltip.REPEL, Tooltip.RUSH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.WARD, 1)
                .build()) {
            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> allies = b.getMinions(owner.team, false, true).toList();
                        Effect repel = new Effect("<b>Repel</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .set(Stat.REPEL, 1)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(allies, repel));
                        int m = owner.finalStats.get(Stat.MAGIC);
                        Effect buff = new Effect("+" + m + "/+0/+0 and <b>Rush</b> until the end of the turn (from <b>Unleash</b>).", EffectStats.builder()
                                .change(Stat.ATTACK, m)
                                .set(Stat.RUSH, 1)
                                .build(),
                                e -> e.untilTurnEndTeam = 0);
                        this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return (AI.VALUE_OF_REPEL * 3 + AI.valueOfRush(this.owner.finalStats.get(Stat.MAGIC) + this.owner.finalStats.get(Stat.ATTACK))) / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
