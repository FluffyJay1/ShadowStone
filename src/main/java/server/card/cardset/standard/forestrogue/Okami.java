package server.card.cardset.standard.forestrogue;

import java.util.List;

import org.newdawn.slick.geom.Vector2f;

import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import server.ServerBoard;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class Okami extends MinionText {
    public static final String NAME = "Okami";
    public static final String DESCRIPTION = "Whenever an allied minion comes into play, gain +1/+0/+0.";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/okami.png"),
            CRAFT, TRAITS, RARITY, 3, 3, 1, 4, true, Okami.class,
            new Vector2f(187, 144), 1.5, new EventAnimationDamageSlash(),
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event e) {
                if (e != null) {
                    int num = (int) e.cardsEnteringPlay().stream()
                            .filter(bo -> bo.team == this.owner.team && bo instanceof Minion && bo != this.owner)
                            .count();
                    if (num > 0) {
                        return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                Effect buff = new Effect("+" + num + "/+0/+0 (from allied cards coming into play)", EffectStats.builder()
                                        .change(Stat.ATTACK, num)
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
                // idk
                return 1;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
