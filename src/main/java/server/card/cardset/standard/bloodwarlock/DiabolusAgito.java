package server.card.cardset.standard.bloodwarlock;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class DiabolusAgito extends MinionText {
    public static final String NAME = "Diabolus Agito";
    public static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Gain +0/+0/+X. X equals the number of times your leader has been damaged during your turn.";
    public static final String DESCRIPTION = "<b>Stalwart</b>. <b>Rush</b>.\n" + BATTLECRY_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/diabolusagito.png"),
            CRAFT, TRAITS, RARITY, 4, 5, 2, 2, true, DiabolusAgito.class,
            new Vector2f(157, 135), 1.5, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.STALWART, Tooltip.RUSH, Tooltip.BATTLECRY),
            List.of(card -> String.format("(Times leader has been damaged during your turn: %d)", card.player.selfDamageCount)));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.STALWART, 1)
                .set(Stat.RUSH, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int x = owner.player.selfDamageCount;
                        Effect buff = new Effect("+0/+0/+" + x + " (from <b>Battlecry</b>).", EffectStats.builder()
                                .change(Stat.HEALTH, x)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.valueForBuff(0, 0, owner.player.selfDamageCount);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
