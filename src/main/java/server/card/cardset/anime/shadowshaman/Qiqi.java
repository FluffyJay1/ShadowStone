package server.card.cardset.anime.shadowshaman;

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
import server.resolver.AddEffectResolver;
import server.resolver.NecromancyResolver;
import server.resolver.ReanimateResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Qiqi extends MinionText {
    public static final String NAME = "Qiqi";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: <b>Necromancy(6)</b> - Give all allied cards in play and in your hand <b>Lifesteal</b>.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: <b>Reanimate(X + 5)</b> and give it <b>Lifesteal</b>. X equals this minion's magic.";
    public static final String DESCRIPTION = "<b>Freezing Touch</b>.\n" + BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/anime/qiqi.png",
            CRAFT, TRAITS, RARITY, 5, 3, 2, 3, false, Qiqi.class,
            new Vector2f(150, 169), 1.4, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.FREEZING_TOUCH, Tooltip.BATTLECRY, Tooltip.NECROMANCY, Tooltip.LIFESTEAL, Tooltip.UNLEASH, Tooltip.REANIMATE),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.FREEZING_TOUCH, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new NecromancyResolver(this, 6, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Card> relevant = Stream.concat(owner.player.getPlayArea().stream(),owner.player.getHand().stream()).collect(Collectors.toList());
                        Effect buff = new Effect("<b>Lifesteal</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .set(Stat.LIFESTEAL, 1)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(relevant, buff));
                    }
                }));
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_OF_LIFESTEAL * 2; // idk
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.player.shadows >= 6;
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int x = owner.finalStats.get(Stat.MAGIC);
                        ReanimateResolver rr = this.resolve(b, rq, el, new ReanimateResolver(owner.player, 5 + x, owner.getIndex() + 1));
                        if (rr.reanimated != null) {
                            Effect buff = new Effect("<b>Lifesteal</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                    .set(Stat.LIFESTEAL, 1)
                                    .build());
                            this.resolve(b, rq, el, new AddEffectResolver(rr.reanimated, buff));
                        }
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return (5 + this.owner.finalStats.get(Stat.MAGIC)) / 4. + AI.VALUE_OF_LIFESTEAL;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
