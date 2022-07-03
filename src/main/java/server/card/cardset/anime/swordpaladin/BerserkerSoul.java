package server.card.cardset.anime.swordpaladin;

import client.tooltip.TooltipSpell;
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
import server.resolver.DiscardResolver;
import server.resolver.DrawResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.ArrayList;
import java.util.List;

public class BerserkerSoul extends SpellText {
    public static final String NAME = "Berserker Soul";
    public static final String DESCRIPTION = "Discard your hand. Give an allied minion with 3 attack or less the following effect until the end of the turn: "
            + EffectBerserkerSoul.DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/anime/berserkersoul.png",
            CRAFT, TRAITS, RARITY, 2, BerserkerSoul.class,
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 1, 1, "Choose an allied minion with 3 attack or less.") {
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
                            this.resolve(b, rq, el, new DiscardResolver(new ArrayList<>(owner.player.getHand())));
                            this.resolve(b, rq, el, new AddEffectResolver(c, new EffectBerserkerSoul()));
                        });
                    }
                });
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }

    public static class EffectBerserkerSoul extends Effect {
        public static final String DESCRIPTION = "<b>Strike</b>: Draw a card. If it's a minion, discard it and gain the ability to attack +1 times this turn.";
        public EffectBerserkerSoul() {
            super(DESCRIPTION);
            this.untilTurnEndTeam = 0;
        }

        @Override
        public ResolverWithDescription strike(Minion target) {
            return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                @Override
                public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                    DrawResolver dr = this.resolve(b, rq, el, new DrawResolver(owner.player, 1));
                    if (!dr.drawn.isEmpty() && dr.drawn.get(0) instanceof Minion) {
                        this.resolve(b, rq, el, new DiscardResolver(dr.drawn.get(0)));
                        Effect additionalAttack = new Effect("+1 attacks this turn (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .change(Stat.ATTACKS_PER_TURN, 1)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(owner, additionalAttack));
                    }
                }
            });
        }
    }
}
