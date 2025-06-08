package server.card.cardset.basic.shadowdeathknight;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageDoubleSlice;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.NecromancyResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;
import java.util.stream.Collectors;

public class DarkBladefiend extends MinionText {
    public static final String NAME = "Dark Bladefiend";
    private static final String ONTURNENDALLIED_DESCRIPTION = "At the end of your turn, perform <b>Necromancy(2)</b>: Deal M damage to a random enemy minion.";
    public static final String DESCRIPTION = "<b>Ward</b>.\n" + ONTURNENDALLIED_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.SHADOWDEATHKNIGHT;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/basic/darkbladefiend.png"),
            CRAFT, TRAITS, RARITY, 5, 3, 2, 5, true, DarkBladefiend.class,
            new Vector2f(161, 173), 1.25, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.WARD, Tooltip.NECROMANCY),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.WARD, 1)
                .build()) {

            @Override
            public ResolverWithDescription onTurnEndAllied() {
                Effect effect = this;
                return new ResolverWithDescription(ONTURNENDALLIED_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> targets = b.getMinions(owner.team * -1, false, true).collect(Collectors.toList());
                        if (!targets.isEmpty()) {
                            this.resolve(b, rq, el, new NecromancyResolver(effect, 2, new Resolver(true) {
                                @Override
                                public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                    Minion target = SelectRandom.from(targets);
                                    int damage = owner.finalStats.get(Stat.MAGIC);
                                    this.resolve(b, rq, el, new DamageResolver(effect, target, damage, true,
                                            new EventAnimationDamageDoubleSlice()));
                                }
                            }));
                        }
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.valueOfMinionDamage(this.owner.finalStats.get(Stat.MAGIC)) / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
