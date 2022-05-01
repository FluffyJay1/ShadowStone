package server.card.cardset.basic.bloodwarlock;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageFire;
import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.DamageResolver;
import server.resolver.DrawResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class Belphegor extends MinionText {
    public static final String NAME = "Belphegor";
    public static final String DESCRIPTION = "<b>Battlecry</b>: Draw 2 cards. If <b>Vengeance</b> isn't active for you, deal X - 15 damage to your leader. X equals your leader's health. " +
            "In either case, give your leader <b>Shield(Y)</b> afterwards. Y equals the amount of health your leader is missing.";
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/belphegor.png",
            CRAFT, RARITY, 8, 4, 2, 4, true, Belphegor.class,
            new Vector2f(142, 136), 1.7, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.BATTLECRY, Tooltip.VENGEANCE, Tooltip.SHIELD));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, new DrawResolver(owner.player, 2));
                        if (!owner.player.vengeance()) {
                            owner.player.getLeader().ifPresent(l -> {
                                this.resolve(b, rq, el, new DamageResolver(effect, l, l.health - 15, true, EventAnimationDamageFire.class));
                            });
                        }
                        owner.player.getLeader().ifPresent(l -> {
                            Effect shield = new Effect("", EffectStats.builder()
                                    .change(EffectStats.SHIELD, 25 - l.health)
                                    .build());
                            this.resolve(b, rq, el, new AddEffectResolver(l, shield));
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                // full heal + shield + draw 2
                return AI.VALUE_PER_HEAL * 25 / 2. + AI.VALUE_OF_SHIELD + AI.VALUE_PER_CARD_IN_HAND * 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
