package server.card.cardset.special.treasure;

import client.tooltip.TooltipSpell;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageFireball;
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
import utils.SelectRandom;

import java.util.List;
import java.util.stream.Collectors;

public class EmbersOfRagnaros extends SpellText {
    public static final String NAME = "Embers of Ragnaros";
    public static final String DESCRIPTION = "Deal 8 damage to a random enemy 3 times.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/special/embersofragnaros.png"),
            CRAFT, TRAITS, RARITY, 3, EmbersOfRagnaros.class,
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        for (int i = 0; i < 3; i++) {
                            List<Minion> targets = b.getMinions(owner.team * -1, true, true).collect(Collectors.toList());
                            if (!targets.isEmpty()) {
                                Minion selected = SelectRandom.from(targets);
                                this.resolve(b, rq, el, new DamageResolver(effect, selected, 8, true, new EventAnimationDamageFireball()));
                            }
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_DAMAGE * 18;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
