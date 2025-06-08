package server.card.cardset.anime.shadowdeathknight;

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
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Qiqi extends MinionText {
    public static final String NAME = "Qiqi";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Give all other allied cards in play and in your hand <b>Lifesteal</b>. <b>Necromancy(7)</b> - <b>Reanimate(7)</b> first.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Summon a <b>Herald of Frost</b> and set its <b>Countdown</b> to M.";
    public static final String DESCRIPTION = "<b>Freezing Touch</b>. <b>Lifesteal</b>.\n" + BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.SHADOWDEATHKNIGHT;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/anime/qiqi.png"),
            CRAFT, TRAITS, RARITY, 7, 1, 2, 7, false, Qiqi.class,
            new Vector2f(150, 169), 1.4, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.FREEZING_TOUCH, Tooltip.LIFESTEAL, Tooltip.BATTLECRY, Tooltip.NECROMANCY, Tooltip.REANIMATE,
                    Tooltip.UNLEASH, HeraldOfFrost.TOOLTIP, Tooltip.COUNTDOWN),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.FREEZING_TOUCH, 1)
                .set(Stat.LIFESTEAL, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, new NecromancyResolver(effect, 7, new ReanimateResolver(owner.player, 7, owner.getIndex() + 1)));
                        List<Card> relevant = Stream.concat(owner.player.getPlayArea().stream(),owner.player.getHand().stream())
                                .filter(c -> c != owner)
                                .collect(Collectors.toList());
                        Effect buff = new Effect("<b>Lifesteal</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .set(Stat.LIFESTEAL, 1)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(relevant, buff));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_OF_LIFESTEAL * 5 + 3.5; // idk
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.player.shadows >= 7;
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        CreateCardResolver ccr = this.resolve(b, rq, el, new CreateCardResolver(new HeraldOfFrost(), owner.team, CardStatus.BOARD, owner.getIndex() + 1));
                        int x = owner.finalStats.get(Stat.MAGIC);
                        Effect countdown = new Effect("<b>Countdown(" + x + ")</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .set(Stat.COUNTDOWN, x)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(ccr.event.successfullyCreatedCards, countdown));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.valueForSummoning(List.of(new HeraldOfFrost().constructInstance(owner.board)), refs) * owner.finalStats.get(Stat.MAGIC) / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
