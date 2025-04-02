package server.card.cardset.standard.bloodwarlock;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageFire;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.Player;
import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.DrawResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class Belphegor extends MinionText {
    public static final String NAME = "Belphegor";
    public static final String DESCRIPTION = String.format("<b>Battlecry</b>: Draw 2 cards. If <b>Vengeance</b> isn't active for you, deal X damage to your leader. " +
            "X equals your leader's health minus %d.", Player.VENGEANCE_THRESHOLD);
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/belphegor.png"),
            CRAFT, TRAITS, RARITY, 4, 4, 2, 4, true, Belphegor.class,
            new Vector2f(142, 136), 1.7, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.VENGEANCE),
            List.of());

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
                                this.resolve(b, rq, el, new DamageResolver(effect, l, l.health - Player.VENGEANCE_THRESHOLD, true, new EventAnimationDamageFire().toString()));
                            });
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                // draw 2, vengeance part is hard to evaluate
                return AI.VALUE_PER_CARD_IN_HAND * 2;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return !this.owner.player.vengeance();
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
