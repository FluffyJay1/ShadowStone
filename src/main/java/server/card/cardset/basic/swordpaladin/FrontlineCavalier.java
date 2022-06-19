package server.card.cardset.basic.swordpaladin;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.RestoreResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FrontlineCavalier extends MinionText {
    public static final String NAME = "Frontline Cavalier";
    public static final String DESCRIPTION = "Whenever an allied Officer comes into play, restore 1 health to your leader.\n" +
            "<b>Unleash</b>: Summon 2 <b>Heavy Knights</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.COMMANDER);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/frontlinecavalier.png",
            CRAFT, TRAITS, RARITY, 4, 3, 2, 4, false, FrontlineCavalier.class,
            new Vector2f(163, 160), 1.5, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.UNLEASH, HeavyKnight.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances; // for getBattlecryValue, preview the value of the created cards

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                String resolverDescription = "<b>Unleash</b>: Summon 2 <b>Heavy Knights</b>.";
                return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<CardText> summons = List.of(new HeavyKnight(), new HeavyKnight());
                        List<Integer> pos = List.of(owner.getIndex() + 1, owner.getIndex());
                        this.resolve(b, rq, el, new CreateCardResolver(summons, owner.team, CardStatus.BOARD, pos));
                    }
                });
            }

            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event e) {
                if (e != null) {
                    int num = (int) e.cardsEnteringPlay().stream()
                            .filter(bo -> bo.team == this.owner.team && bo.finalTraits.contains(CardTrait.OFFICER) && bo != this.owner)
                            .count();
                    if (num > 0) {
                        String resolverDescription = "Whenever an allied Officer comes into play, restore 1 health to your leader.";
                        Optional<Leader> maybeLeader = this.owner.player.getLeader();
                        if (maybeLeader.isPresent()) {
                            Effect effect = this;
                            return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                                @Override
                                public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                    for (int i = 0; i < num; i++) {
                                        this.resolve(b, rq, el, new RestoreResolver(effect, maybeLeader.get(), 1));
                                    }
                                }
                            });
                        }
                    }
                }
                return null;
            }

            @Override
            public double getPresenceValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = Collections.nCopies(2, new HeavyKnight().constructInstance(this.owner.board));
                }
                return AI.VALUE_PER_HEAL * 2 + AI.valueForSummoning(this.cachedInstances, refs) / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
