package server.card.cardset.standard.forestrogue;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;

import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
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
import server.resolver.PutCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class OmnisPrimeOkami extends MinionText {
    public static final String NAME = "Omnis, Prime Okami";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Return 2 other allied cards into your hand. Summon X <b>Okami</b>. X equals the number of cards returned.";
    private static final String ONLISTENEVENT_DESCRIPTION = "Whenever an allied <b>Okami</b> comes into play, give it <b>Rush</b>.";
    public static final String DESCRIPTION = "<b>Storm</b>.\n" + BATTLECRY_DESCRIPTION + "\n" + ONLISTENEVENT_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/omnisprimeokami.png"),
            CRAFT, TRAITS, RARITY, 7, 4, 2, 5, true, OmnisPrimeOkami.class,
            new Vector2f(122, 144), 1.5, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.STORM, Tooltip.BATTLECRY, Okami.TOOLTIP, Tooltip.RUSH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.STORM, 1)
                .build()) {
            private List<Card> cachedInstances; // for getBattlecryValue, preview the value of the created cards
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 0, 2, "Return 2 allied cards to your hand.") {
                    @Override
                    protected boolean criteria(Card c) {
                        return c.status.equals(CardStatus.BOARD) && c.team == this.getCreator().owner.team;
                    }
                });
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Card> returnedCards = getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).toList();
                        if (!returnedCards.isEmpty()) {
                            List<Integer> returnPos = Collections.nCopies(returnedCards.size(), -1);
                            this.resolve(b, rq, el, new PutCardResolver(returnedCards, CardStatus.HAND, owner.team, returnPos, true));
                            List<CardText> summons = Collections.nCopies(returnedCards.size(), new Okami());
                            List<Integer> pos = IntStream.range(0, summons.size())
                                    .map(i -> owner.getIndex() + (i + 1) * ((i + 1) % 2)) // 1 0 3 0 5 0...
                                    .boxed()
                                    .toList();
                            this.resolve(b, rq, el, new CreateCardResolver(summons, owner.team, CardStatus.BOARD, pos));
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = Collections.nCopies(2, new Okami().constructInstance(this.owner.board));
                }
                return AI.valueForSummoning(this.cachedInstances.subList(0, Math.min(owner.player.getPlayArea().size(), this.cachedInstances.size())), refs);
            }

            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event e) {
                if (e != null) {
                    List<BoardObject> relevant = e.cardsEnteringPlay().stream()
                            .filter(bo -> bo.getCardText() instanceof Okami && bo != this.owner && bo.team == this.owner.team)
                            .toList();
                    if (!relevant.isEmpty()) {
                        return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                Effect buff = new Effect("<b>Rush</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                        .set(Stat.RUSH, 1)
                                        .build());
                                this.resolve(b, rq, el, new AddEffectResolver(relevant, buff));
                            }
                        });
                    }
                }
                return null;
            }

            @Override
            public double getPresenceValue(int refs) {
                // idk
                return AI.valueOfRush(4);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
