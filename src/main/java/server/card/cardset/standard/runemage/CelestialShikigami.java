package server.card.cardset.standard.runemage;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;

import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.Resolver;
import server.resolver.SpellboostResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class CelestialShikigami extends MinionText {
    public static final String NAME = "Celestial Shikigami";
    public static final String LASTWORDS_DESCRIPTION = "<b>Last Words</b>: <b>Spellboost</b> the cards in your hand 3 times.";
    public static final String DESCRIPTION = "<b>Ward</b>.\n" + LASTWORDS_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/celestialshikigami.png"),
            CRAFT, TRAITS, RARITY, 4, 4, 3, 5, true, CelestialShikigami.class,
            new Vector2f(150, 95), 2, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.LASTWORDS, Tooltip.SPELLBOOST),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.WARD, 1)
                .build()) {
            @Override
            public ResolverWithDescription lastWords() {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        for (int i = 0; i < 3; i++) {
                            this.resolve(b, rq, el, new SpellboostResolver(owner.player.getHand()));
                        }
                    }
                });
            }

            @Override
            public double getLastWordsValue(int refs) {
                return AI.VALUE_OF_SPELLBOOST * 3;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
