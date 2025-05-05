package server.card.cardset.standard.havenpriest;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageWind;

import org.newdawn.slick.geom.Vector2f;

import server.ServerBoard;
import server.ai.AI;
import server.card.Amulet;
import server.card.BoardObject;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.event.EventDestroy;
import server.resolver.AddEffectResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class GarudaRulerOfStorms extends MinionText {
    public static final String NAME = "Garuda, Ruler of Storms";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Subtract 3 from the <b>Countdown</b> of all allied amulets.";
    private static final String ONLISTENEVENT_DESCRIPTION = "Whenever an allied amulet is destroyed, deal 3 damage to the enemy leader.";
    public static final String DESCRIPTION = ONLISTENEVENT_DESCRIPTION + "\n" + BATTLECRY_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/garudarulerofstorms.png"),
            CRAFT, TRAITS, RARITY, 9, 6, 3, 6, true, GarudaRulerOfStorms.class,
            new Vector2f(150, 120), 1.5, new EventAnimationDamageWind(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.COUNTDOWN),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<BoardObject> amulets = b.getBoardObjects(owner.team, false, false, true, true).filter(bo -> bo.finalStats.contains(Stat.COUNTDOWN)).toList();
                        Effect subtract = new Effect("", EffectStats.builder()
                                .change(Stat.COUNTDOWN, -3)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(amulets, subtract));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 6; // ?
            }

            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event event) {
                if (event instanceof EventDestroy) {
                    EventDestroy ed = (EventDestroy) event;
                    int destroyed = (int) ed.cards.stream()
                            .filter(c -> c.team == this.owner.team && c != this.owner && c instanceof Amulet)
                            .count();
                    if (destroyed > 0) {
                        Effect effect = this;
                        return new ResolverWithDescription(description, new Resolver(false) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                for (int i = 0; i < destroyed; i++) {
                                    b.getPlayer(owner.team * -1).getLeader().ifPresent(l -> {
                                        this.resolve(b, rq, el, new DamageResolver(effect, l, 3, true, new EventAnimationDamageWind()));
                                    });
                                }
                            }
                        });
                    }
                }
                return null;
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_PER_DAMAGE * 1.5;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
