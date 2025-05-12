package server.card.cardset.standard.shadowshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import client.ui.game.visualboardanimation.eventanimation.destroy.EventAnimationDestroyDarkElectro;

import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.EffectWithDependentStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.DestroyResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class LordAtomy extends MinionText {
    public static final String NAME = "Lord Atomy";
    public static final String DEPENDENT_STATS_DESCRIPTION = "Costs 0 if you have at least 5 cards in play.";
    public static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: If you have at least 5 other cards in play, destroy them.";
    public static final String DESCRIPTION = DEPENDENT_STATS_DESCRIPTION + "\n" + BATTLECRY_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/lordatomy.png"),
            CRAFT, TRAITS, RARITY, 9, 9, 3, 9, true, LordAtomy.class,
            new Vector2f(150, 180), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new EffectWithDependentStats(DESCRIPTION, true) {
            @Override
            public EffectStats calculateStats() {
                if (owner.player.getPlayArea().size() >= 5) {
                    return EffectStats.builder()
                        .set(Stat.COST, 0)
                        .build();
                }
                return this.baselineStats;
            }

            @Override
            public boolean isActive() {
                return this.owner.status.equals(CardStatus.HAND);
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<BoardObject> targets = b.getBoardObjects(owner.player.team, false, true, true, true).filter(bo -> bo != owner).toList();
                        if (targets.size() >= 5) {
                            this.resolve(b, rq, el, new DestroyResolver(targets, new EventAnimationDestroyDarkElectro()));
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 2; // i guess?
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return owner.player.getPlayArea().size() >= 5;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
