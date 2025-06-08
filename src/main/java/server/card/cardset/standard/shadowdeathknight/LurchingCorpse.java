package server.card.cardset.standard.shadowdeathknight;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import client.ui.game.visualboardanimation.eventanimation.destroy.EventAnimationDestroyDarkElectro;

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
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;

public class LurchingCorpse extends MinionText {
    public static final String NAME = "Lurching Corpse";
    public static final String LASTWORDS_DESCRIPTION = "<b>Last Words</b>: Destroy a random enemy minion.";
    public static final String DESCRIPTION = "<b>SMOrc</b>.\n" + LASTWORDS_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.SHADOWDEATHKNIGHT;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/lurchingcorpse.png"),
            CRAFT, TRAITS, RARITY, 2, 1, 1, 2, true, LurchingCorpse.class,
            new Vector2f(155, 140), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.SMORC, Tooltip.LASTWORDS),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.SMORC, 1)
                .build()) {
            @Override
            public ResolverWithDescription lastWords() {
                return new ResolverWithDescription(LASTWORDS_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        Minion target = SelectRandom.from(b.getMinions(owner.team * -1, false, true).toList());
                        if (target != null) {
                            this.resolve(b, rq, el, new DestroyResolver(target, new EventAnimationDestroyDarkElectro()));
                        }
                    }
                });
            }

            @Override
            public double getLastWordsValue(int refs) {
                return AI.VALUE_OF_DESTROY;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
