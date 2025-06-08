package server.card.cardset.anime.havenpriest;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.cardset.basic.shadowdeathknight.Zombie;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class Aqua extends MinionText {
    public static final String NAME = "Aqua";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: <b>Reanimate(5)</b> and restore 5 health to all allies.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Gain +0/+0/+M and <b>Ward</b>.";
    private static final String CLASH_DESCRIPTION = "<b>Clash</b>: <b>Mute</b> the enemy minion.";
    private static final String ONTURNEND_DESCRIPTION = "At the end of your turn, summon a <b>Zombie</b> for your opponent.";
    public static final String DESCRIPTION = "<b>Rush</b>.\n" + BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION + "\n" + CLASH_DESCRIPTION
            + "\n" + ONTURNEND_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/anime/aqua.png"),
            CRAFT, TRAITS, RARITY, 6, 0, 3, 10, false, Aqua.class,
            new Vector2f(162, 140), 1.6, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.RUSH, Tooltip.BATTLECRY, Tooltip.REANIMATE, Tooltip.UNLEASH, Tooltip.WARD, Tooltip.CLASH, Tooltip.MUTE,
                    Zombie.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.RUSH, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, new ReanimateResolver(owner.player, 5, owner.getIndex() + 1));
                        List<Minion> healTargets = b.getMinions(owner.team, true, true).collect(Collectors.toList());
                        this.resolve(b, rq, el, new RestoreResolver(effect, healTargets, 5));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 5 + AI.VALUE_PER_HEAL * 5;
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int x = owner.finalStats.get(Stat.MAGIC);
                        Effect buff = new Effect("+0/+0/+" + x + " and <b>Ward</b> (from <b>Unleash</b>).", EffectStats.builder()
                                .change(Stat.HEALTH, x)
                                .set(Stat.WARD, 1)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                    }
                });
            }

            @Override
            public ResolverWithDescription clash(Minion target) {
                return new ResolverWithDescription(CLASH_DESCRIPTION, new MuteResolver(target, true));
            }

            private List<Card> cachedInstances;

            @Override
            public ResolverWithDescription onTurnEndAllied() {
                return new ResolverWithDescription(ONTURNEND_DESCRIPTION,
                        new CreateCardResolver(new Zombie(), this.owner.team * -1, CardStatus.BOARD, -1));
            }

            @Override
            public double getPresenceValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = List.of(new Zombie().constructInstance(this.owner.board));
                }
                return AI.VALUE_OF_MUTE - AI.valueForSummoning(this.cachedInstances, refs)
                        + AI.valueForBuff(0, 0, this.owner.finalStats.get(Stat.MAGIC)) / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
