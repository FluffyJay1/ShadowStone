package server.card.cardset.special.kurumi;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageAOECloud;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.ManaChangeResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class CityOfDevouringTime extends AmuletText {
    public static final String NAME = "City of Devouring Time";
    private static final String ONTURNSTART_DESCRIPTION = "At the start of your turn, deal 1 damage to all enemy minions and gain X mana orbs this turn only. " +
            "X equals the number of enemy minions damaged.";
    public static final String DESCRIPTION = "<b>Countdown(4)</b>. <b>Lifesteal</b>.\n" + ONTURNSTART_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, "res/card/special/cityofdevouringtime.png",
            CRAFT, TRAITS, RARITY, 5, CityOfDevouringTime.class,
            new Vector2f(), -1,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.LIFESTEAL),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.COUNTDOWN, 4)
                .set(Stat.LIFESTEAL, 1)
                .build()) {
            @Override
            public ResolverWithDescription onTurnStartAllied() {
                Effect effect = this;
                return new ResolverWithDescription(ONTURNSTART_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> targets = b.getMinions(owner.team * -1, false, true).collect(Collectors.toList());
                        DamageResolver dr = this.resolve(b, rq, el, new DamageResolver(effect, targets, 1, true, new EventAnimationDamageAOECloud().toString()));
                        int x = (int) dr.event.actualDamage.stream().filter(i -> i > 0).count();
                        this.resolve(b, rq, el, new ManaChangeResolver(owner.player, x, true, false, true));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return 8; // idk it's good tho
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
