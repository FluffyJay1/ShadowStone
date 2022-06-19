package server.card.cardset.basic.bloodwarlock;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageFire;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class SwarmingWraith extends MinionText {
    public static final String NAME = "Swarming Wraith";
    public static final String DESCRIPTION = "<b>Battlecry</b>: If <b>Vengeance</b> is not active for you, deal 2 damage to your leader. " +
            "If <b>Vengeance</b> is active for you, deal 2 damage to the enemy leader.";
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/swarmingwraith.png",
            CRAFT, TRAITS, RARITY, 2, 3, 1, 2, true, SwarmingWraith.class,
            new Vector2f(183, 153), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.VENGEANCE),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        if (owner.player.vengeance()) {
                            b.getPlayer(owner.team * -1).getLeader().ifPresent(l -> {
                                this.resolve(b, rq, el, new DamageResolver(effect, l, 2, true, new EventAnimationDamageFire().toString()));
                            });
                        } else {
                            b.getPlayer(owner.team).getLeader().ifPresent(l -> {
                                this.resolve(b, rq, el, new DamageResolver(effect, l, 2, true, new EventAnimationDamageFire().toString()));
                            });
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                // idk
                return this.owner.player.vengeance() ? AI.VALUE_PER_DAMAGE * 2 : AI.VALUE_PER_DAMAGE;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.player.vengeance();
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
