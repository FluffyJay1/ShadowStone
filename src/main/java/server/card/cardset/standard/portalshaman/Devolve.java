package server.card.cardset.standard.portalshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import client.ui.Animation;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.EvolveResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class Devolve extends SpellText {
    public static final String NAME = "Devolve";
    public static final String DESCRIPTION = "<b>Transform</b> all enemy minions into random ones that cost 1 less.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALSHAMAN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/standard/devolve.png"),
            CRAFT, TRAITS, RARITY, 2, Devolve.class,
            () -> List.of(Tooltip.TRANSFORM),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> targets = b.getMinions(owner.team * -1, false, true).toList();
                        this.resolve(b, rq, el, new EvolveResolver(targets, -1));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 3;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
