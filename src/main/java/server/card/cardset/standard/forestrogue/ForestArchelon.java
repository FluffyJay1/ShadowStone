package server.card.cardset.standard.forestrogue;

import java.util.List;
import java.util.stream.Collectors;

import org.newdawn.slick.geom.Vector2f;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class ForestArchelon extends MinionText {
    public static final String NAME = "Forest Archelon";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: If at least 2 other cards were played this turn, <b>Disarm</b> them until the end of the opponent's turn.";
    public static final String DESCRIPTION = BATTLECRY_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/forestarchelon.png"),
            CRAFT, TRAITS, RARITY, 6, 5, 2, 7, true, ForestArchelon.class,
            new Vector2f(193, 139), 1.5, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.DISARMED),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        if (owner.player.cardsPlayedThisTurn > 2) {
                            List<Minion> targets = b.getMinions(owner.team * -1, false, true).collect(Collectors.toList());
                            Effect disarm = new Effect("<b>Disarmed</b> until the end of your turn (from <b>" + NAME + "</b>).", EffectStats.builder()
                                    .set(Stat.DISARMED, 1)
                                    .build(),
                                    e -> e.untilTurnEndTeam = 1);
                            this.resolve(b, rq, el, new AddEffectResolver(targets, disarm));
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_OF_FREEZE;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.player.cardsPlayedThisTurn >= 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
