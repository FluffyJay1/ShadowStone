package server.card.cardset.anime.havenpriest;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageMagicHit;
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
import server.event.Event;
import server.resolver.BlastResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class SesshouSakura extends AmuletText {
    public static final String NAME = "Sesshou Sakura";
    private static final String LASTWORDS_DESCRIPTION = "<b>Last Words</b>: <b>Blast(X + 1)</b>. X equals this amulet's <b>Countdown</b>.";
    private static final String ONTURNEND_DESCRIPTION = "At the end of your turn, <b>Blast(X)</b>. X equals the number of allied amulets in play.";
    public static final String DESCRIPTION = "<b>Countdown(3)</b>.\n" + LASTWORDS_DESCRIPTION + "\n" + ONTURNEND_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, () -> new Animation("card/anime/sesshousakura.png"),
            CRAFT, TRAITS, RARITY, 3, SesshouSakura.class,
            new Vector2f(), -1,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.LASTWORDS, Tooltip.BLAST),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.COUNTDOWN, 3)
                .build()) {
            @Override
            public ResolverWithDescription lastWords() {
                Effect effect = this;
                return new ResolverWithDescription(LASTWORDS_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int x = owner.finalStats.get(Stat.COUNTDOWN);
                        this.resolve(b, rq, el, new BlastResolver(effect, x + 1, new EventAnimationDamageMagicHit().toString()));
                    }
                });
            }

            @Override
            public double getLastWordsValue(int refs) {
                return AI.VALUE_PER_DAMAGE * (this.owner.finalStats.get(Stat.COUNTDOWN) + 1);
            }

            @Override
            public ResolverWithDescription onTurnEndAllied() {
                Effect effect = this;
                return new ResolverWithDescription(ONTURNEND_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int x = (int) b.getBoardObjects(owner.team, false, false, true, true).count();
                        this.resolve(b, rq, el, new BlastResolver(effect, x, new EventAnimationDamageMagicHit().toString()));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                int x = (int) this.owner.board.getBoardObjects(this.owner.team, false, false, true, true).count();
                return AI.VALUE_PER_DAMAGE * x; // idk
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
