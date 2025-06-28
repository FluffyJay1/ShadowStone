package server.card.cardset.anime.dragondruid;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamagePlanetBefall;
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
import server.resolver.CreateCardResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class Zhongli extends MinionText {
    public static final String NAME = "Zhongli";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Deal 5 damage to all enemies and <b>Disarm</b> them until the end of the opponent's turn.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Summon a <b>Stone Stele</b>. Give all allies <b>Shield(M)</b> until the end of the opponent's turn.";
    public static final String DESCRIPTION = BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/anime/zhongli.png"),
            CRAFT, TRAITS, RARITY, 10, 5, 5, 5, false, Zhongli.class,
            new Vector2f(139, 147), 1.5, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.DISARMED, Tooltip.UNLEASH, StoneStele.TOOLTIP, Tooltip.SHIELD),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> relevant = b.getMinions(owner.team * -1, true, true).collect(Collectors.toList());
                        this.resolve(b, rq, el, new DamageResolver(effect, relevant, 5, true, new EventAnimationDamagePlanetBefall(owner.team * -1)));
                        List<Minion> disarmtargets = b.getMinions(owner.team * -1, true, true).collect(Collectors.toList());
                        Effect disarm = new Effect("<b>Disarmed</b> until the end of your turn (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .set(Stat.DISARMED, 1)
                                .build(),
                                e -> e.untilTurnEndTeam = 1);
                        this.resolve(b, rq, el, new AddEffectResolver(disarmtargets, disarm));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_DAMAGE * 5 + (AI.valueOfMinionDamage(5) + AI.VALUE_OF_FREEZE) * 3;
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, CreateCardResolver.builder()
                                .withCard(new StoneStele())
                                .withTeam(owner.team)
                                .withStatus(CardStatus.BOARD)
                                .withPos(owner.getIndex() + 1)
                                .build());
                        int x = owner.finalStats.get(Stat.MAGIC);
                        Effect shield = new Effect("<b>Shield(" + x + ")</b> until the end of the opponent's turn (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .change(Stat.SHIELD, x)
                                .build(),
                                e -> e.untilTurnEndTeam = -1);
                        List<Minion> relevant = b.getMinions(owner.team, true, true).collect(Collectors.toList());
                        this.resolve(b, rq, el, new AddEffectResolver(relevant, shield));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                int x = owner.finalStats.get(Stat.MAGIC);
                return AI.VALUE_OF_SHIELD + AI.valueForBuff(0, 0, x) * 3 + AI.valueForSummoning(List.of(new StoneStele().constructInstance(owner.board)), refs);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
