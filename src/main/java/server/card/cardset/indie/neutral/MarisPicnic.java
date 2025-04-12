package server.card.cardset.indie.neutral;

import java.util.List;

import org.newdawn.slick.geom.Vector2f;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.Animation;
import server.ServerBoard;
import server.ai.AI;
import server.card.AmuletText;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.DrawResolver;
import server.resolver.ManaChangeResolver;
import server.resolver.Resolver;
import server.resolver.RestoreResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class MarisPicnic extends AmuletText {
    public static final String NAME = "Mari's Picnic";
    private static final String LASTWORDS_DESCRIPTION = "<b>Last Words</b>: Draw 2 cards, restore 5 health to all allies, and gain 2 mana orbs this turn only.";
    public static final String DESCRIPTION = "<b>Countdown(1)</b>.\n" + LASTWORDS_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, () -> new Animation("card/indie/marispicnic.png"),
            CRAFT, TRAITS, RARITY, 5, MarisPicnic.class,
            new Vector2f(150, 130), 1.5,
            () -> List.of(Tooltip.LASTWORDS),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.COUNTDOWN, 1)
                .build()){
            @Override
            public ResolverWithDescription lastWords() {
                Effect effect = this;
                return new ResolverWithDescription(LASTWORDS_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, new DrawResolver(owner.player, 2));
                        List<Minion> healTargets = b.getMinions(owner.team, true, true).toList();
                        this.resolve(b, rq, el, new RestoreResolver(effect, healTargets, 5));
                        this.resolve(b, rq, el, new ManaChangeResolver(owner.player, 2, true, false, true));
                    }
                });
            }

            @Override
            public double getLastWordsValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND * 2 + AI.VALUE_PER_HEAL * 5 + 2;
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
