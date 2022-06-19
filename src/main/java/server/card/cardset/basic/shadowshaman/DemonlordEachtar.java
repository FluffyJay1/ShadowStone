package server.card.cardset.basic.shadowshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.Player;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.EffectUntilTurnEnd;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.NecromancyResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DemonlordEachtar extends MinionText {
    public static final String NAME = "Demonlord Eachtar";
    public static final String DESCRIPTION = "<b>Battlecry</b>: <b>Necromancy(3)</b> - Summon a <b>Zombie</b>. Repeat for remaining shadows or until your area is full. " +
            "Then give all other allied minions +2/+0/+0 and <b>Rush</b> until the end of the turn.";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/demonlordeachtar.png",
            CRAFT, TRAITS, RARITY, 7, 5, 2, 6, true, DemonlordEachtar.class,
            new Vector2f(120, 120), 1.8, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.NECROMANCY, Zombie.TOOLTIP, Tooltip.RUSH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances; // for getBattlecryValue, preview the value of the created cards
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        Player p = b.getPlayer(owner.team);
                        // oh boy
                        int lastPos = owner.getIndex() + 1;
                        while (p.getPlayArea().size() < p.maxPlayAreaSize
                                && this.resolve(b, rq, el,
                                new NecromancyResolver(effect, 3, new CreateCardResolver(new Zombie(), owner.team, CardStatus.BOARD, lastPos)))
                                .wasSuccessful()) {
                            lastPos++;
                        }
                        String buffDescription = "+2/+0/+0 and <b>Rush</b> until the end of the turn (from <b>Demonlord Eachtar's Battlecry</b>).";
                        Effect buff = new EffectUntilTurnEnd(buffDescription, EffectStats.builder()
                                .change(Stat.ATTACK, 2)
                                .set(Stat.RUSH, 1)
                                .build()
                        );
                        List<Minion> relevant = b.getMinions(owner.team, false, true)
                                .filter(m -> m != owner)
                                .collect(Collectors.toList());
                        this.resolve(b, rq, el, new AddEffectResolver(relevant, buff));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = Collections.nCopies(Player.MAX_MAX_BOARD_SIZE, new Zombie().constructInstance(this.owner.board));
                }
                int numSummoned = this.owner.player.shadows / 3;
                return AI.valueForSummoning(this.cachedInstances.subList(0, Math.min(numSummoned, this.cachedInstances.size())), refs)
                        + (AI.valueForBuff(2, 0, 0) + AI.valueOfRush(4)) * 6 / 4;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
