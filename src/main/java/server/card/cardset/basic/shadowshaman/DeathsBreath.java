package server.card.cardset.basic.shadowshaman;

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
import server.resolver.NecromancyResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DeathsBreath extends SpellText {
    public static final String NAME = "Death's Breath";
    public static final String DESCRIPTION = "Summon 3 <b>Zombies</b>. <b>Necromancy(6)</b>: Give +0/+0/+1 and <b>Ward</b> to all allied <b>Zombies</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/basic/deathsbreath.png"),
            CRAFT, TRAITS, RARITY, 6, DeathsBreath.class,
            () -> List.of(Zombie.TOOLTIP, Tooltip.NECROMANCY, Tooltip.WARD),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances;

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<CardText> cardTexts = Collections.nCopies(3, new Zombie());
                        List<Integer> pos = Collections.nCopies(3, -1);
                        this.resolve(b, rq, el, new CreateCardResolver(cardTexts, owner.team, CardStatus.BOARD, pos));
                        this.resolve(b, rq, el, new NecromancyResolver(effect, 6, new Resolver(false) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                List<Minion> zombies = b.getMinions(owner.team, false, true)
                                        .filter(m -> m.getCardText() instanceof Zombie)
                                        .collect(Collectors.toList());
                                Effect buff = new Effect("+0/+0/+1 and <b>Ward</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                        .change(Stat.HEALTH, 1)
                                        .set(Stat.WARD, 1)
                                        .build());
                                this.resolve(b, rq, el, new AddEffectResolver(zombies, buff));
                            }
                        }));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = Collections.nCopies(3, new Zombie().constructInstance(this.owner.board));
                }
                return AI.valueForSummoning(this.cachedInstances, refs)
                        + this.owner.player.shadows >= 6 ? (AI.valueForBuff(0, 0, 1) + AI.VALUE_OF_WARD) * 3 : 0;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.player.shadows >= 6;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
