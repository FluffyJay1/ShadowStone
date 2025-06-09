package server.card.cardset.basic.swordpaladin;

import client.tooltip.TooltipSpell;
import client.ui.Animation;
import server.ServerBoard;
import server.card.*;
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

public class Equality extends SpellText {
    public static final String NAME = "Equality";
    public static final String DESCRIPTION = "Change the health of all minions to 1.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/basic/equality.png"),
            CRAFT, TRAITS, RARITY, 2, Equality.class,
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> relevant = b.getMinions(0, false, true).toList();
                        if (!relevant.isEmpty()) {
                            Effect debuff = new Effect("Health set to 1 (from <b>" + NAME + "</b>).", EffectStats.builder()
                                    .set(Stat.HEALTH, 1)
                                    .build());
                            this.resolve(b, rq, el, new AddEffectResolver(relevant, debuff));
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 3; // idk
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
