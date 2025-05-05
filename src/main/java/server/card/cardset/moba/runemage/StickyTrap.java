package server.card.cardset.moba.runemage;

import java.util.List;

import org.newdawn.slick.geom.Vector2f;

import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageAOEFire;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageDefault;
import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.DestroyResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class StickyTrap extends MinionText {
    public static final String NAME = "Sticky Trap";
    public static final String DESCRIPTION = "At the start of your turn, deal this minion's health as damage to all enemy minions and destroy this minion.";
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/moba/stickytrap.png"),
            CRAFT, TRAITS, RARITY, 1, 0, 0, 1, false, StickyTrap.class,
            new Vector2f(), -1, new EventAnimationDamageDefault(),
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onTurnStartAllied() {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> relevant = b.getMinions(owner.team * -1, false, false).toList();
                        int h = owner instanceof Minion ? ((Minion) owner).health : 0; // doesnt hurt to be extra sure
                        this.resolve(b, rq, el, new DamageResolver(effect, relevant, h, true, new EventAnimationDamageAOEFire(owner.team * -1, false)));
                        this.resolve(b, rq, el, new DestroyResolver(owner));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.valueOfMinionDamage(owner.finalStats.get(Stat.HEALTH));
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}

