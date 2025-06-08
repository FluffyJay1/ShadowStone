package server.card.cardset.indie.shadowdeathknight;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageBonePapyrus;

import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;

import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.cardset.indie.neutral.Mercy;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Stream;

public class TheGreatPapyrus extends MinionText {
    public static final String NAME = "The Great Papyrus";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Put a <b>Mercy</b> into your opponent's hand.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: <b>Freeze</b> the enemy leader and Unleash Power, and deal 2 damage to the enemy leader.";
    private static final String LASTWORDS_DESCRIPTION = "<b>Last Words</b>: Summon a <b>Sans</b> and give it +0/+M/+0.";
    private static final String OTHER_DESCRIPTION = "<b>Ward</b>. <b>Stalwart</b>.";
    public static final String DESCRIPTION = OTHER_DESCRIPTION + "\n" + BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION + "\n" + LASTWORDS_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.SHADOWDEATHKNIGHT;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION,
            () -> new Animation("card/indie/thegreatpapyrus.png", new Vector2f(1, 1), 0, 0, Image.FILTER_NEAREST),
            CRAFT, TRAITS, RARITY, 8, 1, 5, 8, false, TheGreatPapyrus.class,
            new Vector2f(100, 120), 1.2, new EventAnimationDamageBonePapyrus(),
            () -> List.of(Tooltip.WARD, Tooltip.STALWART, Tooltip.BATTLECRY, Mercy.TOOLTIP, Tooltip.UNLEASH, Tooltip.FROZEN, Tooltip.LASTWORDS, Sans.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.WARD, 1)
                .set(Stat.STALWART, 1)
                .build()) {
            private List<Card> cachedInstancesBattlecry;
            private List<Card> cachedInstancesLastWords;

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new CreateCardResolver(new Mercy(), owner.team * -1, CardStatus.HAND, -1));
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstancesBattlecry == null) {
                    this.cachedInstancesBattlecry = List.of(new Mercy().constructInstance(owner.board));
                }
                return -AI.valueForAddingToHand(this.cachedInstancesBattlecry, refs);
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Card> targets = Stream.concat(b.getPlayer(owner.team * -1).getLeader().stream(), b.getPlayer(owner.team * -1).getUnleashPower().stream()).toList();
                        Effect freezer = new Effect("", EffectStats.builder()
                                .set(Stat.FROZEN, 1)
                                .build());
                        if (!targets.isEmpty()) {
                            this.resolve(b, rq, el, new AddEffectResolver(targets, freezer));
                        }
                        b.getPlayer(owner.team * -1).getLeader().ifPresent(l -> {
                            this.resolve(b, rq, el, new DamageResolver(effect, l, 2, true, new EventAnimationDamageBonePapyrus()));
                        });
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return (AI.VALUE_OF_FREEZE + AI.VALUE_PER_DAMAGE * 2) / 2;
            }

            @Override
            public ResolverWithDescription lastWords() {
                Effect effect = this;
                return new ResolverWithDescription(LASTWORDS_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int pos = ((BoardObject) effect.owner).getRelevantBoardPos();
                        CreateCardResolver ccr = this.resolve(b, rq, el, new CreateCardResolver(new Sans(), owner.team, CardStatus.BOARD, pos));
                        int x = owner.finalStats.get(Stat.MAGIC);
                        Effect buff = new Effect("+0/+" + x + "/+0 (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .change(Stat.MAGIC, x)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(ccr.event.successfullyCreatedCards, buff));
                    }
                });
            }

            @Override
            public double getLastWordsValue(int refs) {
                if (this.cachedInstancesLastWords == null) {
                    this.cachedInstancesLastWords = List.of(new Sans().constructInstance(owner.board));
                }
                return AI.valueForSummoning(cachedInstancesLastWords, refs) + AI.valueForBuff(0, owner.finalStats.get(Stat.MAGIC), 0);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
