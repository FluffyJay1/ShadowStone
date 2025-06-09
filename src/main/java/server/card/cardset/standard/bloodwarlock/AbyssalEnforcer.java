package server.card.cardset.standard.bloodwarlock;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageAOEFire;
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
import java.util.stream.Collectors;

public class AbyssalEnforcer extends MinionText {
    public static final String NAME = "Abyssal Enforcer";
    public static final String DESCRIPTION = "<b>Battlecry</b>: Deal 3 damage to all other characters.";
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/abyssalenforcer.png"),
            CRAFT, TRAITS, RARITY, 7, 6, 2, 6, true, AbyssalEnforcer.class,
            new Vector2f(141, 180), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY),
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
                        List<Minion> relevant = b.getMinions(0, true, true)
                                .filter(m -> m != owner)
                                .collect(Collectors.toList());
                        this.resolve(b, rq, el, new DamageResolver(effect, relevant, 3, true, new EventAnimationDamageAOEFire(0, true)));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_DAMAGE * 9;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
