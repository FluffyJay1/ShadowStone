package server.card.cardset.anime.neutral;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class Jotaro extends MinionText {
    public static final String NAME = "Jotaro Kujo";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Put a <b>Yare Yare Daze</b> into your hand.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Gain the ability to attack +15 times this turn.";
    private static final String STRIKE_DESCRIPTION = "<b>Strike</b>: Gain <b>Shield(1)</b> until the end of the turn.";
    public static final String DESCRIPTION = "<b>Rush</b>. Can't attack the enemy leader.\n" + BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION
            + "\n" + STRIKE_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/anime/jotaro.png"),
            CRAFT, TRAITS, RARITY, 9, 4, 2, 4, false, Jotaro.class,
            new Vector2f(134, 173), 1.4, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, YareYareDaze.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.RUSH, 1)
                .set(Stat.CANT_ATTACK_LEADER, 1)
                .build()) {
            private List<Card> cachedInstances;

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, CreateCardResolver.builder()
                        .withCard(new YareYareDaze())
                        .withTeam(this.owner.team)
                        .withStatus(CardStatus.HAND)
                        .withPos(-1)
                        .build());
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = List.of(new YareYareDaze().constructInstance(this.owner.board));
                }
                return AI.valueForAddingToHand(this.cachedInstances, refs);
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                Effect buff = new Effect("+15 attacks this turn (from <b>Unleash</b>).", EffectStats.builder()
                        .change(Stat.ATTACKS_PER_TURN, 15)
                        .build(),
                        e -> e.untilTurnEndTeam = 0);
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new AddEffectResolver(this.owner, buff));
            }

            @Override
            public ResolverWithDescription strike(Minion target) {
                Effect buff = new Effect("<b>Shield(1)</b> until the end of the turn (from <b>Strike</b>).", EffectStats.builder()
                        .change(Stat.SHIELD, 1)
                        .build(),
                        e -> e.untilTurnEndTeam = 0);
                return new ResolverWithDescription(STRIKE_DESCRIPTION, new AddEffectResolver(this.owner, buff));
            }

            @Override
            public double getPresenceValue(int refs) {
                return 8;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
