package server.card.cardset.standard.swordpaladin;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import client.ui.Animation;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class SpikeridgedSteed extends SpellText {
    public static final String NAME = "Spikeridged Steed";
    public static final String EFFECT_DESCRIPTION = "+2/+0/+6, <b>Ward</b>, and <b>Last Words</b>: Summon a <b>" + SpikeridgedSteed.NAME + "</b>.";
    public static final String DESCRIPTION = "Give an allied minion" + EFFECT_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/standard/spikeridgedsteed.png"),
            CRAFT, TRAITS, RARITY, 5, SpikeridgedSteed.class,
            () -> List.of(Tooltip.WARD, Tooltip.LASTWORDS, SpikeridgedSteed.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 1, 1, DESCRIPTION) {
                    @Override
                    protected boolean criteria(Card c) {
                        return c.status.equals(CardStatus.BOARD) && c instanceof Minion && c.team == this.getCreator().owner.team;
                    }
                });
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            this.resolve(b, rq, el, new AddEffectResolver(c, new EffectSpikeridgedSteed()));
                        });
                    }
                });
            }
            @Override
            public double getBattlecryValue(int refs) {
                return 6; // idk
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }

    public static class EffectSpikeridgedSteed extends Effect {
        private static String ADDED_EFFECT_DESCRIPTION = EFFECT_DESCRIPTION + " (From <b>" + NAME + "</b>.)";
        // required for reflection
        public EffectSpikeridgedSteed() {
            super(ADDED_EFFECT_DESCRIPTION, EffectStats.builder()
                    .change(Stat.ATTACK, 2)
                    .change(Stat.HEALTH, 6)
                    .set(Stat.WARD, 1)
                    .build());
        }

        @Override
        public ResolverWithDescription lastWords() {
            Effect effect = this;
            return new ResolverWithDescription(ADDED_EFFECT_DESCRIPTION, new Resolver(false) {
                @Override
                public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                    int pos = ((BoardObject) effect.owner).getRelevantBoardPos();
                    this.resolve(b, rq, el, new CreateCardResolver(new Stegodon(), owner.team, CardStatus.BOARD, pos));
                }
            });
        }
    }
}
