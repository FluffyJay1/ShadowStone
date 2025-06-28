package server.card.cardset.standard.havenpriest;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageAOEFire;

import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.AmuletText;
import server.card.BoardObject;
import server.card.Card;
import server.card.CardRarity;
import server.card.CardStatus;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.cardset.basic.havenpriest.HolywingDragon;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class DevourerOfHeavens extends AmuletText {
    public static final String NAME = "Devourer of Heavens";
    private static final String LASTWORDS_DESCRIPTION = "<b>Last Words</b>: Summon a <b>Holywing Dragon</b>. Then deal 2 damage to all enemy minions.";
    public static final String DESCRIPTION = "<b>Countdown(5)</b>.\n" + LASTWORDS_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, () -> new Animation("card/standard/devourerofheavens.png"),
            CRAFT, TRAITS, RARITY, 2, DevourerOfHeavens.class,
            new Vector2f(120, 160), 1.4,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.LASTWORDS, HolywingDragon.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.COUNTDOWN, 5)
                .build()) {
            private List<Card> cachedInstances;

            @Override
            public ResolverWithDescription lastWords() {
                Effect effect = this;
                return new ResolverWithDescription(LASTWORDS_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int pos = ((BoardObject) effect.owner).getRelevantBoardPos(); // startpos
                        this.resolve(b, rq, el, CreateCardResolver.builder()
                                .withCard(new HolywingDragon())
                                .withTeam(effect.owner.team)
                                .withStatus(CardStatus.BOARD)
                                .withPos(pos)
                                .build());
                        List<Minion> relevant = b.getMinions(owner.team * -1, false, false).toList();
                        this.resolve(b, rq, el, new DamageResolver(effect, relevant, 2, true, new EventAnimationDamageAOEFire(owner.team * -1, false)));
                    }
                });
            }

            @Override
            public double getLastWordsValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = List.of(new HolywingDragon().constructInstance(this.owner.board));
                }
                return AI.valueForSummoning(cachedInstances, refs) + AI.valueOfMinionDamage(2) * 3;
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
