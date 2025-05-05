package server.card.cardset.indie.neutral;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageMagicHit;

import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;

import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectAura;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;

public class EmotionHappy extends AmuletText {
    public static final String NAME = "Emotion: Happy";
    private static final String AURA_DESCRIPTION = "<b>Aura</b>: Allied minions have <b>Rush</b> and \"<b>Clash</b>: Deal 1 damage to the enemy minion.\"";
    public static final String DESCRIPTION = "<b>Countdown(3)</b>.\n" + AURA_DESCRIPTION + "\nAt the start of your turn, <b>Disarm</b> a random ally until the end of the turn.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION,
            () -> new Animation("card/indie/emotionhappy.png", new Vector2f(3, 1), 0, 0, Image.FILTER_NEAREST,
                    anim -> {
                        anim.play = true;
                        anim.loop = true;
                        anim.setFrameInterval(0.2);
                    }),
            CRAFT, TRAITS, RARITY, 3, EmotionHappy.class,
            new Vector2f(), -1,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.AURA, Tooltip.RUSH, Tooltip.CLASH, Tooltip.DISARMED),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(
                new EffectAura(AURA_DESCRIPTION, 1, true, false, new EffectEmotionHappy()) {
                    @Override
                    public boolean applyConditions(Card cardToApply) {
                        return cardToApply instanceof Minion;
                    }

                    @Override
                    public double getPresenceValue(int refs) {
                        return AI.valueOfRush(2) * 3; // idk
                    }
                },
                new Effect("<b>Countdown(3)</b>", EffectStats.builder()
                        .set(Stat.COUNTDOWN, 3)
                        .build()
                ) {
                    public ResolverWithDescription onTurnStartAllied() {
                        return new ResolverWithDescription("At the start of your turn, <b>Disarm</b> a random ally until the end of the turn.", new Resolver(true) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                Effect debuff = new Effect("Attack set to 0 (from <b>" + NAME + "</b>)", EffectStats.builder()
                                        .set(Stat.DISARMED, 1)
                                        .build(),
                                        e -> e.untilTurnEndTeam = 0);
                                Minion choice = SelectRandom.from(b.getMinions(owner.team, true, true).toList());
                                if (choice != null) {
                                    this.resolve(b, rq, el, new AddEffectResolver(choice, debuff));
                                }
                            }
                        });
                    };
                }
        );
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }

    public static class EffectEmotionHappy extends Effect {
        public EffectEmotionHappy() {
            super("<b>Rush</b> and <b>Clash</b>: Deal 1 damage to the enemy minion (from <b>" + NAME + "</b>).", EffectStats.builder()
                    .set(Stat.RUSH, 1)
                    .build());
        }

        @Override
        public ResolverWithDescription clash(Minion target) {
            return new ResolverWithDescription("<b>Clash</b>: Deal 1 damage to the enemy minion (from <b>" + NAME + "'s Aura</b>).", new DamageResolver(this, target, 1, true, new EventAnimationDamageMagicHit()));
        }

        @Override
        public double getPresenceValue(int refs) {
            return AI.VALUE_PER_DAMAGE * 1;
        }
    }
}
