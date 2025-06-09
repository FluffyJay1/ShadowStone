package server.card.cardset.standard.dragondruid;

import client.tooltip.TooltipSpell;
import client.ui.Animation;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class SpreadingPlague extends SpellText {
    public static final String NAME = "Spreading Plague";
    public static final String DESCRIPTION =  "Summon a <b>Scarab Beetle</b>. If your opponent has more minions, repeat this effect.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/standard/spreadingplague.png"),
            CRAFT, TRAITS, RARITY, 6, SpreadingPlague.class,
            () -> List.of(ScarabBeetle.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Minion> cachedInstances;
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        for (int i = 0; i < 6; i++) {
                            this.resolve(b, rq, el, new CreateCardResolver(new ScarabBeetle(), owner.team, CardStatus.BOARD, -1));
                            int enemySize = (int) b.getMinions(owner.team * -1, false, true).count();
                            int alliedSize = (int) b.getMinions(owner.team, false, true).count();
                            if (enemySize <= alliedSize || owner.player.getPlayArea().size() == owner.player.maxPlayAreaSize) {
                                break;
                            }
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = List.of(new ScarabBeetle().constructInstance(owner.board));
                }
                return AI.valueForSummoning(cachedInstances, refs) * 3;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
