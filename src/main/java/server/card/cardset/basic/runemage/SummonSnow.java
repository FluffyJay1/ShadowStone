package server.card.cardset.basic.runemage;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import server.ServerBoard;
import server.card.*;
import server.card.cardset.basic.shadowshaman.Zombie;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.Collections;
import java.util.List;

public class SummonSnow extends SpellText {
    public static final String NAME = "Summon Snow";
    public static final String DESCRIPTION = "Summon X + 1 <b>Snowmen</b>. X equals the number of times this card has been <b>Spellboosted</b>.";
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/basic/summonsnow.png",
            CRAFT, RARITY, 3, SummonSnow.class,
            () -> List.of(Snowman.TOOLTIP, Tooltip.SPELLBOOST));
    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, new EffectStats(
                new EffectStats.Setter(EffectStats.SPELLBOOSTABLE, false, 1)
        )) {
            private List<Card> cachedInstances; // for getBattlecryValue, preview the value of the created cards
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int times = owner.spellboosts + 1;
                        List<CardText> snowmen = Collections.nCopies(times, new Snowman());
                        List<Integer> pos = Collections.nCopies(times, -1);
                        this.resolve(b, rq, el, new CreateCardResolver(snowmen, owner.team, CardStatus.BOARD, pos));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = List.of(new Snowman().constructInstance(this.owner.board));
                }
                int numSummoned = this.owner.spellboosts + 1;
                double sum = 0;
                double multiplier = 0.9;
                for (int i = 0; i < numSummoned; i++) {
                    sum += this.cachedInstances.get(0).getValue(refs - 1) * multiplier * 0.8;
                    multiplier *= multiplier; // each card has lower and lower chance of being able to fit
                }
                return sum;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
