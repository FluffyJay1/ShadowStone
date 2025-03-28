package server.card.cardset.anime.portalhunter;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageAOESlice;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageDoubleSlice;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.event.EventPlayCard;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RaidenShogun extends MinionText {
    public static final String NAME = "Raiden Shogun";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Deal A damage evenly split among all enemy minions, prioritizing in clockwise order.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Summon an <b>Eye of Stormy Judgement</b> and set its <b>Countdown</b> to M.";
    private static final String ONLISTENEVENT_DESCRIPTION = "Whenever you play a card while this is in your hand, gain +1/+0/+0.";
    public static final String DESCRIPTION = "<b>Rush</b>.\n" + BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION + "\n" + ONLISTENEVENT_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/anime/raidenshogun.png",
            CRAFT, TRAITS, RARITY, 7, 3, 3, 5, false, Kurumi.class,
            new Vector2f(139, 146), 1.8, new EventAnimationDamageDoubleSlice(),
            () -> List.of(Tooltip.RUSH, Tooltip.BATTLECRY, Tooltip.UNLEASH, EyeOfStormyJudgement.TOOLTIP, Tooltip.COUNTDOWN),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.RUSH, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int x = owner.finalStats.get(Stat.ATTACK);
                        List<Minion> targets = b.getMinions(owner.team * -1, false, true).collect(Collectors.toList());
                        List<Integer> damage = IntStream.range(0, targets.size())
                                .map(i -> (x + i) / targets.size()) // math wizardry
                                .boxed()
                                .collect(Collectors.toList());
                        this.resolve(b, rq, el, new DamageResolver(effect, targets, damage, true,
                                new EventAnimationDamageAOESlice(owner.team * -1, false).toString()));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                int x = owner.finalStats.get(Stat.ATTACK);
                return AI.valueOfMinionDamage(x / 3) * 3;
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        CreateCardResolver ccr = this.resolve(b, rq, el, new CreateCardResolver(new EyeOfStormyJudgement(), owner.team, CardStatus.BOARD, owner.getIndex() + 1));
                        int x = owner.finalStats.get(Stat.MAGIC);
                        Effect countdown = new Effect("<b>Countdown(" + x + ")</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .set(Stat.COUNTDOWN, x)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(ccr.event.successfullyCreatedCards, countdown));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.valueForSummoning(List.of(new EyeOfStormyJudgement().constructInstance(owner.board)), refs) * owner.finalStats.get(Stat.MAGIC) / 2;
            }

            @Override
            public ResolverWithDescription onListenEvent(Event event) {
                if (!(event instanceof EventPlayCard) || ((EventPlayCard) event).p.team != this.owner.team
                        || !this.owner.status.equals(CardStatus.HAND) || ((EventPlayCard) event).c == this.owner) {
                    return null;
                }
                return new ResolverWithDescription(ONLISTENEVENT_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        if (owner.status.equals(CardStatus.HAND)) {
                            Effect e = new Effect("", EffectStats.builder()
                                    .change(Stat.ATTACK, 1)
                                    .build());
                            this.resolve(b, rq, el, new AddEffectResolver(owner, e));
                        }
                    }
                });
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
