package server.card.cardset.anime.bloodwarlock;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageWind;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectAura;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class YakshasMask extends AmuletText {
    public static final String NAME = "Yaksha's Mask";
    private static final String AURA_DESCRIPTION = "<b>Aura</b>: The allied minion directly clockwise to this has +3/+3/+0 and <b>Bane</b>.";
    private static final String ONTURNEND_DESCRIPTION = "At the end of your turn, deal 1 damage to your leader.";
    private static final String NONAURA_DESCRIPTION = "<b>Countdown(4)</b>.\n" + ONTURNEND_DESCRIPTION;
    public static final String DESCRIPTION =  NONAURA_DESCRIPTION + "\n" + AURA_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, () -> new Animation("card/anime/yakshasmask.png"),
            CRAFT, TRAITS, RARITY, 3, YakshasMask.class,
            new Vector2f(), -1,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.AURA),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(
                new EffectAura(AURA_DESCRIPTION, 1, true, false, new Effect("+3/+3/+0 and <b>Bane</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                        .change(Stat.ATTACK, 3)
                        .change(Stat.MAGIC, 3)
                        .set(Stat.BANE, 1)
                        .build())) {
                    @Override
                    public boolean applyConditions(Card cardToApply) {
                        return cardToApply instanceof Minion && cardToApply.getIndex() == this.owner.getIndex() - 1;
                    }

                    @Override
                    public double getPresenceValue(int refs) {
                        return AI.valueForBuff(3, 3, 0) + AI.VALUE_OF_BANE;
                    }
                },
                new Effect(NONAURA_DESCRIPTION, EffectStats.builder()
                        .set(Stat.COUNTDOWN, 4)
                        .build()) {
                    @Override
                    public ResolverWithDescription onTurnEndAllied() {
                        Effect effect = this;
                        return new ResolverWithDescription(ONTURNEND_DESCRIPTION, new Resolver(false) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                owner.player.getLeader().ifPresent(l -> {
                                    this.resolve(b, rq, el, new DamageResolver(effect, l, 1, true, new EventAnimationDamageWind().toString()));
                                });
                            }
                        });
                    }

                    @Override
                    public double getPresenceValue(int refs) {
                        return AI.VALUE_PER_DAMAGE * -3;
                    }
                }
        );
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
