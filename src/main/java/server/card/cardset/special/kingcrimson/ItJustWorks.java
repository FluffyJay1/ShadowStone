package server.card.cardset.special.kingcrimson;

import client.tooltip.TooltipSpell;
import client.ui.Animation;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;
import java.util.stream.Collectors;

public class ItJustWorks extends SpellText {
    public static final String NAME = "It Just Works";
    public static final String DESCRIPTION = "Give both players 1 mana orb. Both players draw a card. Subtract 1 from the <b>Countdown</b> of all cards. " +
            "Force enemy minions to randomly attack your minions. Then give your minions <b>Storm</b>.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/special/itjustworks.png"),
            CRAFT, TRAITS, RARITY, 2, ItJustWorks.class,
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, new ManaChangeResolver(owner.player, 1, true, true, false));
                        this.resolve(b, rq, el, new ManaChangeResolver(b.getPlayer(owner.team * -1), 1, true, true, false));
                        this.resolve(b, rq, el, new DrawResolver(owner.player, 1));
                        this.resolve(b, rq, el, new DrawResolver(b.getPlayer(owner.team * -1), 1));
                        List<BoardObject> countdowners = b.getBoardObjects(0, false, true, true, true)
                                .filter(bo -> bo.finalStats.contains(Stat.COUNTDOWN))
                                .collect(Collectors.toList());
                        Effect countdownSubtract = new Effect("", EffectStats.builder()
                                .change(Stat.COUNTDOWN, -1)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(countdowners, countdownSubtract));
                        // ho boy
                        List<Minion> attacking = b.getMinions(owner.team * -1, false, true).collect(Collectors.toList());
                        for (Minion m : attacking) {
                            if (m.isInPlay() && m.alive) {
                                List<Minion> possibleTargets = b.getMinions(owner.team, false, true).collect(Collectors.toList());
                                if (!possibleTargets.isEmpty()) {
                                    Minion selected = SelectRandom.from(possibleTargets);
                                    this.resolve(b, rq, el, new MinionAttackResolver(m, selected, false));
                                }
                            }
                        }

                        List<Minion> buffTargets = b.getMinions(owner.team, false, true).collect(Collectors.toList());
                        Effect storm = new Effect("<b>Storm</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .set(Stat.STORM, 1)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(buffTargets, storm));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 4; // idk it just works
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
