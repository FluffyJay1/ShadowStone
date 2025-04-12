package server.card.cardset.basic.forestrogue;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageArrow;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
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

import java.util.List;

public class BeetleWarrior extends MinionText {
    public static final String NAME = "Beetle Warrior";
    public static final String DESCRIPTION = "<b>Battlecry</b>: If at least 2 other cards were played this turn, gain +1/+0/+1 and <b>Storm</b>.";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/basic/beetlewarrior.png"),
            CRAFT, TRAITS, RARITY, 3, 3, 1, 3, true, BeetleWarrior.class,
            new Vector2f(150, 145), 1.5, new EventAnimationDamageArrow(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.STORM),
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
                            Effect buff = new Effect("+1/+0/+1 and <b>Storm</b> (from <b>Battlecry</b>).", EffectStats.builder()
                                    .change(Stat.ATTACK, 1)
                                    .change(Stat.HEALTH, 1)
                                    .set(Stat.STORM, 1)
                                    .build());
                            this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return ((AI.valueForBuff(1, 0, 1) + 1) + AI.valueOfStorm(this.owner.finalStats.get(Stat.ATTACK) + 1)) / 2.;
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
