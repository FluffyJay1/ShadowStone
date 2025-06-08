package server.card.cardset.indie.shadowdeathknight;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageBoneSans;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageBoneSansArray;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageGasterBlaster;

import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;

import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.event.EventPlayCard;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;
import server.resolver.AddEffectResolver;
import server.resolver.BlastResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;

public class Sans extends MinionText {
    public static final String NAME = "Sans";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: <b>Blast(1)</b> X times. X equals the number of unique minions in your graveyard.";
    private static final String RETALIATE_DESCRIPTION = "<b>Retaliate</b>: If M is at least 1, gain <b>Shield(1)</b> and -0/-1/-0.";
    private static final String ONTURNSTART_DESCRIPTION = "At the start of your turn, perform the following M times: deal 1 damage to a random enemy minion that attacked last turn.";
    private static final String ONLISTENEVENT_DESCRIPTION = "Whenever your opponent plays a card, deal 1 damage to left-most minion in your opponent's hand.";
    private static final String OTHER_DESCRIPTION = "<b>Elusive</b>. <b>Stalwart</b>. <b>Repel</b>. <b>Armor(-99)</b>.";
    public static final String DESCRIPTION = OTHER_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION + "\n" + RETALIATE_DESCRIPTION + "\n" + ONTURNSTART_DESCRIPTION + "\n" + ONLISTENEVENT_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.SHADOWDEATHKNIGHT;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION,
            () -> new Animation("card/indie/sans.png", new Vector2f(13, 1), 0, 0, Image.FILTER_NEAREST,
                    anim -> {
                        anim.play = true;
                        anim.loop = true;
                        anim.setFrameInterval(0.1);
                    }),
            CRAFT, TRAITS, RARITY, 8, 1, 1, 1, false, Sans.class,
            new Vector2f(75, 90), 1.2, new EventAnimationDamageBoneSansArray(),
            () -> List.of(Tooltip.ELUSIVE, Tooltip.STALWART, Tooltip.REPEL, Tooltip.ARMOR, Tooltip.UNLEASH, Tooltip.BLAST, Tooltip.RETALIATE),
            List.of(card -> String.format("(Unique minions in graveyard: %d)", card.player.getGraveyard().stream().filter(c -> c instanceof Minion).map(Card::getCardText).distinct().count())));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.ELUSIVE, 1)
                .set(Stat.STALWART, 1)
                .set(Stat.REPEL, 1)
                .set(Stat.ARMOR, -99)
                .build()) {
            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int x = (int) owner.player.getGraveyard().stream().filter(c -> c instanceof Minion).map(Card::getCardText).distinct().count();
                        b.pushEventGroup(new EventGroup(EventGroupType.CONCURRENTDAMAGE));
                        for (int i = 0; i < x; i++) {
                            this.resolve(b, rq, el, new BlastResolver(effect, 1, new EventAnimationDamageGasterBlaster()));
                        }
                        b.popEventGroup();
                    }
                });
            }

            @Override
            public ResolverWithDescription retaliate(Minion target) {
                return new ResolverWithDescription(RETALIATE_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int m = owner.finalStats.get(Stat.MAGIC);
                        if (m > 0) {
                            Effect effect = new Effect("<b>Shield(1)</b> and -0/-1/-0 (from <b>Retaliate</b>).", EffectStats.builder()
                                    .change(Stat.SHIELD, 1)
                                    .change(Stat.MAGIC, -1)
                                    .build());
                            this.resolve(b, rq, el, new AddEffectResolver(owner, effect));
                        }
                    }
                });
            }

            @Override
            public ResolverWithDescription onTurnStartAllied() {
                Effect effect = this;
                return new ResolverWithDescription(ONTURNSTART_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int times = owner.finalStats.get(Stat.MAGIC);
                        b.pushEventGroup(new EventGroup(EventGroupType.CONCURRENTDAMAGE));
                        for (int i = 0; i < times; i++) {
                            List<Minion> minions = b.getMinions(owner.team * -1, false, true).filter(m -> m.attacksThisTurn > 0).toList();
                            Minion choice = SelectRandom.from(minions);
                            if (choice != null) {
                                this.resolve(b, rq, el, new DamageResolver(effect, choice, 1, true, new EventAnimationDamageGasterBlaster()));
                            }
                        }
                        b.popEventGroup();
                    }
                });
            }

            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event event) {
                if (event instanceof EventPlayCard && ((EventPlayCard) event).p.team != owner.team) {
                    Effect effect = this;
                    return new ResolverWithDescription(ONLISTENEVENT_DESCRIPTION, new Resolver(false) {
                        @Override
                        public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                            b.getPlayer(owner.team * -1).getHand().stream()
                                    .filter(c -> c instanceof Minion)
                                    .findFirst()
                                    .ifPresent(c -> {
                                        this.resolve(b, rq, el, new DamageResolver(effect, (Minion) c, 1, true, new EventAnimationDamageBoneSans()));
                                    });
                        }
                    });
                }
                return null;
            }

            @Override
            public double getPresenceValue(int refs) {
                return owner.finalStats.get(Stat.MAGIC) * 1.5 + 5; // it's bretty good
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
