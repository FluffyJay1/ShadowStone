package server.card.cardset.anime.bloodwarlock;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageWind;
import org.newdawn.slick.geom.Vector2f;
import server.Player;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Xiao extends MinionText {
    public static final String NAME = "Xiao";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Summon a <b>Yaksha's Mask</b>.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Deal M damage a random enemy minion and the enemy leader.";
    public static final String DESCRIPTION = "<b>Rush</b>.\n" + BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/anime/xiao.png",
            CRAFT, TRAITS, RARITY, 4, 3, 1, 4, false, Guts.class,
            new Vector2f(146, 151), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.RUSH, Tooltip.BATTLECRY, YakshasMask.TOOLTIP, Tooltip.UNLEASH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.RUSH, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, new CreateCardResolver(new YakshasMask(), owner.team, CardStatus.BOARD, owner.getIndex() + 1));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.valueForSummoning(List.of(new YakshasMask().constructInstance(owner.board)), refs);
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> targets = new ArrayList<>(2);
                        Minion choice = SelectRandom.from(b.getMinions(owner.team * -1, false, true).collect(Collectors.toList()));
                        if (choice != null) {
                            targets.add(choice);
                        }
                        targets.addAll(b.getPlayerCard(owner.team * -1, Player::getLeader).collect(Collectors.toList()));
                        int x = owner.finalStats.get(Stat.MAGIC);
                        this.resolve(b, rq, el, new DamageResolver(effect, targets, x, true, new EventAnimationDamageWind().toString()));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                int x = owner.finalStats.get(Stat.MAGIC);
                return (x * AI.VALUE_PER_DAMAGE * x + AI.valueOfMinionDamage(x)) / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
