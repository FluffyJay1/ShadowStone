package server.card.cardset.indie.shadowshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageOff;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
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

import java.util.List;

public class Enoch extends MinionText {
    public static final String NAME = "Enoch";
    private static final String ONTURNSTART_DESCRIPTION = "At the start of your turn, perform <b>Necromancy(4)</b> to avoid dealing X damage to this minion. " +
            "X equals half of this minion's health, rounded down.";
    public static final String DESCRIPTION = "<b>Ward</b>. <b>Stalwart</b>.\n" + ONTURNSTART_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/indie/enoch.png"),
            CRAFT, TRAITS, RARITY, 8, 2, 2, 20, true, Enoch.class,
            new Vector2f(151, 141), 1.3, new EventAnimationDamageOff(),
            () -> List.of(Tooltip.WARD, Tooltip.STALWART, Tooltip.NECROMANCY),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.WARD, 1)
                .set(Stat.STALWART, 1)
                .build()) {
            @Override
            public ResolverWithDescription onTurnStartAllied() {
                Effect effect = this;
                return new ResolverWithDescription(ONTURNSTART_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        if (owner instanceof Minion) {
                            NecromancyResolver nr = this.resolve(b, rq, el, new NecromancyResolver(effect, 4, null));
                            if (!nr.wasSuccessful()) {
                                Minion m = (Minion) owner;
                                int x = m.health / 2;
                                this.resolve(b, rq, el, new DamageResolver(effect, m, x, true, new EventAnimationDamageOff().toString()));
                            }
                        }
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                if (owner instanceof Minion) {
                    Minion m = (Minion) this.owner;
                    return -m.health / 2;
                }
                return 0;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
