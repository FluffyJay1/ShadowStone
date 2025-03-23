package server.card.cardset.standard.swordpaladin;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.PutCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Magnolia extends MinionText {
    public static final String NAME = "Magnolia, Battlefield Muse";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Put a random M-cost minion and (M-1)-cost minion from your deck into play.";
    public static final String DESCRIPTION = UNLEASH_DESCRIPTION + "\nAt the end of your turn, give +1/+1/+1 to all allied Officer minions.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.COMMANDER);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/standard/magnolia.png",
            CRAFT, TRAITS, RARITY, 4, 1, 2, 5, false, Magnolia.class,
            new Vector2f(140, 144), 1.4, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.UNLEASH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Card> target = new ArrayList<>(2);
                        List<Integer> pos = new ArrayList<>(2);
                        int x = owner.finalStats.get(Stat.MAGIC);
                        for (int i = 0; i < 2; i++) {
                            int cost = x - 1 + i;
                            List<Card> eligible = owner.player.getDeck().stream()
                                    .filter(c -> c instanceof Minion && c.finalStats.get(Stat.COST) == cost)
                                    .collect(Collectors.toList());
                            if (!eligible.isEmpty()) {
                                target.add(SelectRandom.from(eligible));
                                pos.add(owner.getIndex() + 1 - i);
                            }
                        }
                        if (!target.isEmpty()) {
                            this.resolve(b, rq, el, new PutCardResolver(target, CardStatus.BOARD, owner.team, pos, true));
                        }
                    }
                });
            }

            @Override
            public ResolverWithDescription onTurnEndAllied() {
                String resolverDescription = "At the end of your turn, give +1/+1/+1 to all allied Officer minions.";
                return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> targets = owner.board.getMinions(owner.team, false, true)
                                .filter(m -> m.finalTraits.contains(CardTrait.OFFICER))
                                .collect(Collectors.toList());
                        Effect buff = new Effect("+1/+1/+1 (from <b>Magnolia, Battlefield Muse</b>).", EffectStats.builder()
                                .change(Stat.ATTACK, 1)
                                .change(Stat.MAGIC, 1)
                                .change(Stat.HEALTH, 1)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(targets, buff));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                // idk some pretty powerful shit going on here
                return AI.valueForBuff(1, 1, 1) * 6 / 2 + 3 / 2.;
            }
        });

    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
