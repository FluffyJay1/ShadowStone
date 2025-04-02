package server.card.cardset.moba.neutral;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageShoot;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;
import java.util.stream.Collectors;

public class LilShredder extends SpellText {
    public static final String NAME = "Lil' Shredder";
    public static final String DESCRIPTION = "Deal 1 damage to a random enemy minion then reduce its <b>Armor</b> by 1. Do this a total of 6 times.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/moba/lilshredder.png"),
            CRAFT, TRAITS, RARITY, 4, LilShredder.class,
            () -> List.of(Tooltip.ARMOR),
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
                        for (int i = 0; i < 6; i++) {
                            List<Minion> targets = b.getMinions(owner.team * -1, false, true).collect(Collectors.toList());
                            if (!targets.isEmpty()) {
                                Minion selected = SelectRandom.from(targets);
                                this.resolve(b, rq, el, new DamageResolver(effect, selected, 1, true, new EventAnimationDamageShoot().toString()));
                                if (selected.alive) {
                                    Effect debuff = new Effect("-1 <b>Armor</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                            .change(Stat.ARMOR, -1)
                                            .build());
                                    this.resolve(b, rq, el, new AddEffectResolver(selected, debuff));
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.valueOfMinionDamage(12);
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
