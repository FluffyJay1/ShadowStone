package server.card.cardset.moba.dragondruid;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageDefault;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageWidowMineExplosion;

import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.*;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WidowMine extends MinionText {
    public static final String NAME = "Widow Mine";
    public static final String ONTURNSTART_DESCRIPTION = "At the start of your turn, deal <b>M</b> + 3 damage to a random enemy minion and <b>M</b> damage to its neighbors.";
    public static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Gain <b>Stealth</b>.";
    public static final String DESCRIPTION = "<b>Stealth</b>.\n" + ONTURNSTART_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/moba/widowmine.png"),
            CRAFT, TRAITS, RARITY, 3, 0, 1, 2, false, WidowMine.class,
            new Vector2f(), -1, new EventAnimationDamageDefault(),
            () -> List.of(Tooltip.STEALTH, Tooltip.UNLEASH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.STEALTH, 1)
                .build()) {
            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION,
                        new AddEffectResolver(this.owner, new Effect("<b>Stealth</b> (from <b>Unleash</b>).",
                                EffectStats.builder().set(Stat.STEALTH, 1).build())));
            }

            @Override
            public ResolverWithDescription onTurnStartAllied() {
                Effect effect = this;
                return new ResolverWithDescription(ONTURNSTART_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> potentialTargets = b.getMinions(owner.team * -1, false, true).collect(Collectors.toList());
                        if (!potentialTargets.isEmpty()) {
                            List<Minion> targets = new ArrayList<>(3);
                            List<Integer> damage = new ArrayList<>(3);
                            Minion selected = SelectRandom.from(potentialTargets);
                            int m = owner.finalStats.get(Stat.MAGIC);
                            targets.add(selected);
                            damage.add(m + 3);
                            int selectedInd = selected.getIndex();
                            for (int i = -1; i <= 1; i += 2) {
                                int offsetPos = selectedInd + i;
                                BoardObject adjacent = b.getPlayer(owner.team * -1).getPlayArea().get(offsetPos);
                                if (adjacent instanceof Minion) {
                                    targets.add((Minion) adjacent);
                                    damage.add(m);
                                }
                            }
                            this.resolve(b, rq, el, new DamageResolver(effect, targets, damage, true, new EventAnimationDamageWidowMineExplosion()));
                        }
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return (owner.finalStats.get(Stat.MAGIC) * 3 + 3) / 2.;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
