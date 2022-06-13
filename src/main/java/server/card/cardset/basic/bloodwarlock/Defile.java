package server.card.cardset.basic.bloodwarlock;

import client.tooltip.TooltipSpell;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageAOECloud;
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

public class Defile extends SpellText {
    public static final String NAME = "Defile";
    public static final String DESCRIPTION = "Deal 1 damage to all minions. If any die, cast this again.";
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/basic/defile.png",
            CRAFT, TRAITS, RARITY, 2, Defile.class,
            List::of,
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
                        for (int i = 0; i < 14; i++) {
                            List<Minion> relevant = b.getMinions(0, false, true).collect(Collectors.toList());
                            DamageResolver damageResolver = new DamageResolver(effect, relevant, 1, true, EventAnimationDamageAOECloud.class);
                            ResolverQueue subQueue = new ResolverQueue();
                            this.resolve(b, subQueue, el, damageResolver);
                            this.resolveQueue(b, subQueue, el, subQueue);
                            if (damageResolver.destroyed.isEmpty()) {
                                break;
                            }
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.valueOfMinionDamage(4); //??? how tf do i evaluate this
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
