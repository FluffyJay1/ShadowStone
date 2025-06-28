package server.card.cardset.basic.runemage;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import client.ui.Animation;
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
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class MysterianCircle extends SpellText {
    public static final String NAME = "Mysterian Circle";
    public static final String DESCRIPTION = "Summon a <b>Clay Golem</b>. Give <b>Ward</b> to all allied <b>Clay Golems</b>.";
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/basic/mysteriancircle.png"),
            CRAFT, TRAITS, RARITY, 2, ConjureGolem.class,
            () -> List.of(ClayGolem.TOOLTIP, Tooltip.WARD),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances;

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, CreateCardResolver.builder()
                                .withCard(new ClayGolem())
                                .withTeam(owner.team)
                                .withStatus(CardStatus.BOARD)
                                .withPos(-1)
                                .build());
                        List<Minion> clayGolems = b.getMinions(owner.team, false, true)
                                .filter(m -> m.getCardText() instanceof ClayGolem)
                                .toList();
                        Effect buff = new Effect("<b>Ward</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .set(Stat.WARD, 1)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(clayGolems, buff));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = List.of(new ClayGolem().constructInstance(this.owner.board));
                }
                return AI.valueForSummoning(this.cachedInstances, refs) + AI.VALUE_OF_WARD;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
