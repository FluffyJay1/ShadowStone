package server.card.cardset.basic.portalshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.Animation;

import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.AmuletText;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.DrawResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class TranquilCog extends AmuletText {
    public static final String NAME = "Tranquil Cog";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Draw a card. If <b>Resonance</b> is active for you, draw 2 cards instead.";
    private static final String LASTWORDS_DESCRIPTION = "<b>Last Words</b>: Draw 2 cards. If <b>Resonance</b> is active for you, draw 1 card instead.";
    public static final String DESCRIPTION = "<b>Countdown(3)</b>.\n" + BATTLECRY_DESCRIPTION + "\n" + LASTWORDS_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.PORTALSHAMAN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, () -> new Animation("card/basic/tranquilcog.png"),
            CRAFT, TRAITS, RARITY, 3, TranquilCog.class,
            new Vector2f(153, 153), 1.3,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.BATTLECRY, Tooltip.RESONANCE, Tooltip.LASTWORDS),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.COUNTDOWN, 3)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, new DrawResolver(owner.player, owner.player.resonance() ? 2 : 1));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND * 1.5;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.player.resonance();
            }

            @Override
            public ResolverWithDescription lastWords() {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, new DrawResolver(owner.player, owner.player.resonance() ? 1 : 2));
                    }
                });
            }

            @Override
            public double getLastWordsValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND * 1.5;
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
