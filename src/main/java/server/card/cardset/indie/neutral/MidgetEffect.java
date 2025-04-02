package server.card.cardset.indie.neutral;

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

import java.util.Collections;
import java.util.List;

public class MidgetEffect extends SpellText {
    public static final String NAME = "Midget Effect";
    public static final String DESCRIPTION = "Choose an allied minion. Summon 2 plain copies of it and set their stats to 1/1/1.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/indie/midgeteffect.png"),
            CRAFT, TRAITS, RARITY, 2, MidgetEffect.class,
            List::of,
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
                            List<CardText> ct = Collections.nCopies(2, c.getCardText());
                            List<Integer> pos = List.of(c.getIndex() + 1, c.getIndex());
                            CreateCardResolver ccr = this.resolve(b, rq, el, new CreateCardResolver(ct, c.team, CardStatus.BOARD, pos));
                            if (!ccr.event.successfullyCreatedCards.isEmpty()) {
                                Effect stats = new Effect("Stats set to 1/1/1 (from <b>" + NAME + "</b>).", EffectStats.builder()
                                        .set(Stat.ATTACK, 1)
                                        .set(Stat.MAGIC, 1)
                                        .set(Stat.HEALTH, 1)
                                        .build());
                                this.resolve(b, rq, el, new AddEffectResolver(ccr.event.successfullyCreatedCards, stats));
                            }
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 3;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
