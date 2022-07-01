package server.card.cardset.special.treasure;

import client.tooltip.TooltipSpell;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectAura;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.DrawResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class BootsOfHaste extends SpellText {
    public static final String NAME = "Boots of Haste";
    public static final String DESCRIPTION = "Give your leader the following effect until the end of the turn: " + EffectBootsOfHaste.DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/special/bootsofhaste.png",
            CRAFT, TRAITS, RARITY, 1, BootsOfHaste.class,
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
                        owner.player.getLeader().ifPresent(l -> {
                            this.resolve(b, rq, el, new AddEffectResolver(l, new EffectBootsOfHaste()));
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 6; // it's bretty good
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }

    public static class EffectBootsOfHaste extends EffectAura {
        public static final String DESCRIPTION = "<b>Aura</b>: Your minions cost 0.";
        public EffectBootsOfHaste() {
            super(DESCRIPTION, 1, false, true, new Effect("Costs 0 (from <b>Boots of Haste</b>).", EffectStats.builder()
                    .set(Stat.COST, 0)
                    .build()));
            this.untilTurnEndTeam = 0;
        }

        @Override
        public boolean applyConditions(Card cardToApply) {
            return cardToApply instanceof Minion;
        }
    }
}
