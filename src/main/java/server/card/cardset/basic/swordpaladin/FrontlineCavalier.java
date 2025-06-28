package server.card.cardset.basic.swordpaladin;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.Player;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.Stat;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FrontlineCavalier extends MinionText {
    public static final String NAME = "Frontline Cavalier";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Summon M <b>Heavy Knights</b>.";
    public static final String DESCRIPTION = UNLEASH_DESCRIPTION + "\nWhenever an allied Officer comes into play, restore 1 health to your leader.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.COMMANDER);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/basic/frontlinecavalier.png"),
            CRAFT, TRAITS, RARITY, 4, 3, 2, 5, false, FrontlineCavalier.class,
            new Vector2f(163, 160), 1.5, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.UNLEASH, HeavyKnight.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances; // for getBattlecryValue, preview the value of the created cards

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int x = owner.finalStats.get(Stat.MAGIC);
                        List<CardText> summons = Collections.nCopies(x, new HeavyKnight());
                        List<Integer> pos = IntStream.range(0, x)
                                .map(i -> owner.getIndex() + (i + 1) * ((i + 1) % 2)) // 1 0 3 0 5 0...
                                .boxed()
                                .collect(Collectors.toList());
                        this.resolve(b, rq, el, CreateCardResolver.builder()
                                .withCards(summons)
                                .withTeam(owner.team)
                                .withStatus(CardStatus.BOARD)
                                .withPos(pos)
                                .build());
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
                    this.cachedInstances = Collections.nCopies(Player.MAX_MAX_BOARD_SIZE, new HeavyKnight().constructInstance(this.owner.board));
                }
                int x = owner.finalStats.get(Stat.MAGIC);
                return AI.VALUE_PER_HEAL * 2 + AI.valueForSummoning(this.cachedInstances.subList(0, Math.min(x, this.cachedInstances.size())), refs) / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
