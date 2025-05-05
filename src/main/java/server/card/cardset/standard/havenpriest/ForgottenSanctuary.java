package server.card.cardset.standard.havenpriest;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.Animation;

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
import server.card.cardset.basic.havenpriest.Barong;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class ForgottenSanctuary extends AmuletText {
    public static final String NAME = "Forgotten Sanctuary";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Summon a <b>Barong</b>.";
    private static final String LASTWORDS_DESCRIPTION = "<b>Last Words</b>: Summon a <b>Barong</b>.";
    public static final String DESCRIPTION = "<b>Countdown(3)</b>.\n" + BATTLECRY_DESCRIPTION + "\n" + LASTWORDS_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, () -> new Animation("card/standard/forgottensanctuary.png"),
            CRAFT, TRAITS, RARITY, 5, ForgottenSanctuary.class,
            new Vector2f(160, 170), 1.3,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.BATTLECRY, Barong.TOOLTIP, Tooltip.LASTWORDS),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.COUNTDOWN, 3)
                .build()) {
            private List<Card> cachedInstances;
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, new CreateCardResolver(new Barong(), owner.team, CardStatus.BOARD, owner.getIndex() + 1));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = List.of(new Barong().constructInstance(this.owner.board));
                }
                return AI.valueForSummoning(cachedInstances, refs);
            }

            @Override
            public ResolverWithDescription lastWords() {
                return new ResolverWithDescription(LASTWORDS_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int pos = ((BoardObject) owner).getRelevantBoardPos(); // startpos
                        this.resolve(b, rq, el, new CreateCardResolver(new Barong(), owner.team, CardStatus.BOARD, pos));
                    }
                });
            }

            @Override
            public double getLastWordsValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = List.of(new Barong().constructInstance(this.owner.board));
                }
                return AI.valueForSummoning(cachedInstances, refs);
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
