package server.card.cardset.standard.havenpriest;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.Animation;

import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.AmuletText;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.cardset.basic.havenpriest.HolywingDragon;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.effect.common.EffectLastWordsSummon;
import server.event.Event;
import server.event.EventRestore;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.RestoreResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class WhitefangTemple extends AmuletText {
    public static final String NAME = "Whitefang Temple";
    private static final String ONTURNENDALLIED_DESCRIPTION = "At the end of your turn, restore 1 health to your leader.";
    private static final String ONLISTENEVENT_DESCRIPTION = "Whenever an effect restores health to an ally, subtract 1 from this amulet's <b>Countdown</b>.";
    private static final String LASTWORDS_DESCRIPTION = "<b>Last Words</b>: Summon a <b>Holywing Dragon</b>.";
    public static final String DESCRIPTION = "<b>Countdown(8)</b>.\n" + ONTURNENDALLIED_DESCRIPTION + "\n" + ONLISTENEVENT_DESCRIPTION + "\n" + LASTWORDS_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, () -> new Animation("card/standard/whitefangtemple.png"),
            CRAFT, TRAITS, RARITY, 3, WhitefangTemple.class,
            new Vector2f(139, 204), 1.4,
            () -> List.of(Tooltip.COUNTDOWN, HolywingDragon.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect("<b>Countdown(8)</b>\n" + ONTURNENDALLIED_DESCRIPTION + "\n" + ONLISTENEVENT_DESCRIPTION, EffectStats.builder()
                .set(Stat.COUNTDOWN, 8)
                .build()) {
            @Override
            public ResolverWithDescription onTurnEndAllied() {
                Effect effect = this;
                return new ResolverWithDescription(ONTURNENDALLIED_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        owner.player.getLeader().ifPresent(l -> {
                            this.resolve(b, rq, el, new RestoreResolver(effect, l, 1));
                        });
                    }
                });
            }

            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event event) {
                if (event instanceof EventRestore && ((EventRestore) event).m.stream().anyMatch(m -> m.team == owner.team)) {
                    Effect countdownSubtract = new Effect("", EffectStats.builder()
                            .change(Stat.COUNTDOWN, -1)
                            .build());
                    return new ResolverWithDescription(ONLISTENEVENT_DESCRIPTION, new AddEffectResolver(this.owner, countdownSubtract));
                }
                return null;
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_PER_HEAL * 4;
            }

        }, new EffectLastWordsSummon(LASTWORDS_DESCRIPTION, List.of(new HolywingDragon()), 1));
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
