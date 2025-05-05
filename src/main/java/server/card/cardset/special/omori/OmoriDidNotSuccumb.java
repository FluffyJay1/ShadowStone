package server.card.cardset.special.omori;

import java.util.List;

import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageOmoriStab;
import server.Player;
import server.ServerBoard;
import server.ai.AI;
import server.card.AmuletText;
import server.card.Card;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.Leader;
import server.card.effect.Effect;
import server.card.effect.EffectAura;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class OmoriDidNotSuccumb extends AmuletText {
    public static final String NAME = "OMORI did not succumb.";
    private static final String AURA_DESCRIPTION = "<b>Aura</b>: Your leader has <b>Unyielding</b>.";
    private static final String ONTURNEND_DESCRIPTION = "At the end of your turn, deal 2 damage to both leaders.";
    public static final String DESCRIPTION = "<b>Countdown(3)</b>.\n" + AURA_DESCRIPTION + "\n" + ONTURNEND_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION,
            () -> new Animation("card/special/omorididnotsuccumb.png", new Vector2f(3, 1), 0, 0, Image.FILTER_NEAREST,
                    anim -> {
                        anim.play = true;
                        anim.loop = true;
                        anim.setFrameInterval(0.2);
                    }),
            CRAFT, TRAITS, RARITY, 2, OmoriDidNotSuccumb.class,
            new Vector2f(), -1,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.UNYIELDING),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(
                new EffectAura(AURA_DESCRIPTION, 1, false, false, true, false,
                        new Effect("<b>Unyielding</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .set(Stat.UNYIELDING, 1)
                                .build())) {
                    @Override
                    public boolean applyConditions(Card cardToApply) {
                        return cardToApply instanceof Leader;
                    }

                    @Override
                    public double getPresenceValue(int refs) {
                        return 3; // idk
                    }
                },
                new Effect("<b>Countdown(3)</b>." + ONTURNEND_DESCRIPTION, EffectStats.builder()
                        .set(Stat.COUNTDOWN, 3)
                        .build()) {
                    @Override
                    public ResolverWithDescription onTurnEndAllied() {
                        Effect effect = this;
                        return new ResolverWithDescription(ONTURNEND_DESCRIPTION, new Resolver(false) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                List<Leader> leaders = owner.board.getPlayerCard(0, Player::getLeader).toList();
                                this.resolve(b, rq, el, new DamageResolver(effect, leaders, 2, true, new EventAnimationDamageOmoriStab()));
                            }
                        });
                    };

                    @Override
                    public double getPresenceValue(int refs) {
                        return AI.VALUE_PER_DAMAGE * 2; // idk
                    }
                }
        );

    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}

