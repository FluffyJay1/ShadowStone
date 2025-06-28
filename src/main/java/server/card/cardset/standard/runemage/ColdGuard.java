package server.card.cardset.standard.runemage;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import client.ui.Animation;
import server.ServerBoard;
import server.card.*;
import server.card.cardset.basic.runemage.Snowman;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.effect.common.EffectSpellboostDiscount;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.Collections;
import java.util.List;

public class ColdGuard extends SpellText {
    public static final String NAME = "Cold Guard";
    private static final String BATTLECRY_DESCRIPTION = "Set all enemy minions' health to 1. Summon 3 <b>Snowmen</b>. Give all allied <b>Snowmen</b> +0/+0/+1 and <b>Ward</b>. Give your leader <b>Shield(3)</b>.";
    public static final String DESCRIPTION = EffectSpellboostDiscount.DESCRIPTION + "\n" + BATTLECRY_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/standard/coldguard.png"),
            CRAFT, TRAITS, RARITY, 8, ColdGuard.class,
            () -> List.of(Tooltip.SPELLBOOST, Snowman.TOOLTIP, Tooltip.WARD, Tooltip.SHIELD),
            List.of());


    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> enemies = b.getMinions(owner.team * -1, false, true).toList();
                        if (!enemies.isEmpty()) {
                            Effect debuff = new Effect("Health set to 1 (from <b>" + NAME + "</b>).", EffectStats.builder()
                                    .set(Stat.HEALTH, 1)
                                    .build());
                            this.resolve(b, rq, el, new AddEffectResolver(enemies, debuff));
                        }
                        List<CardText> summons = Collections.nCopies(3, new Snowman());
                        List<Integer> pos = Collections.nCopies(3, -1);
                        this.resolve(b, rq, el, CreateCardResolver.builder()
                                .withCards(summons)
                                .withTeam(owner.team)
                                .withStatus(CardStatus.BOARD)
                                .withPos(pos)
                                .build());
                        List<Minion> snowmen = b.getMinions(owner.team, false, true)
                                .filter(m -> m.getCardText() instanceof Snowman)
                                .toList();
                        Effect buff = new Effect("+0/+0/+1 and <b>Ward</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .change(Stat.HEALTH, 1)
                                .set(Stat.WARD, 1)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(snowmen, buff));
                        owner.player.getLeader().ifPresent(l -> {
                            Effect shield = new Effect("", EffectStats.builder()
                                    .change(Stat.SHIELD, 3)
                                    .build());
                            this.resolve(b, rq, el, new AddEffectResolver(l, shield));
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 8;
            }
        }, new EffectSpellboostDiscount());
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
