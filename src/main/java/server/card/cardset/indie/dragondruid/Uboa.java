package server.card.cardset.indie.dragondruid;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageMagicHit;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.BanishResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class Uboa extends MinionText {
    public static final String NAME = "Uboa";
    private static final String ONLISTENEVENT_DESCRIPTION = "Whenever an allied card comes into play, <b>Banish</b> it and gain +1/+0/+0.";
    public static final String DESCRIPTION = "<b>Storm</b>.\n" + ONLISTENEVENT_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/indie/uboa.png",
            CRAFT, TRAITS, RARITY, 9, 6, 3, 6, true, Uboa.class,
            new Vector2f(150, 180), 1, new EventAnimationDamageMagicHit(),
            () -> List.of(Tooltip.STORM, Tooltip.BANISH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.STORM, 1)
                .build()) {

            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event event) {
                if (event != null) {
                    List<BoardObject> relevant = event.cardsEnteringPlay().stream()
                            .filter(bo -> bo != this.owner && bo.team == this.owner.team)
                            .collect(Collectors.toList());
                    if (!relevant.isEmpty()) {
                        return new ResolverWithDescription(ONLISTENEVENT_DESCRIPTION, new Resolver(false) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                this.resolve(b, rq, el, new BanishResolver(relevant));
                                int x = relevant.size();
                                Effect buff = new Effect("+" + x + "/+0/+0 (from allied cards coming into play)", EffectStats.builder()
                                        .change(Stat.ATTACK, x)
                                        .build());
                                this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                            }
                        });
                    }
                }
                return null;
            }

            @Override
            public double getPresenceValue(int refs) {
                return -AI.VALUE_OF_BANISH;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
