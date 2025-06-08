package server.card.cardset.moba.portalshaman;

import java.util.List;

import org.newdawn.slick.geom.Vector2f;

import client.tooltip.TooltipMinion;
import client.ui.Animation;
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
import server.resolver.DrawResolver;
import server.resolver.Resolver;
import server.resolver.RestoreResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class EngineersBuildingDispenser extends MinionText {
    public static final String NAME = "Engineer's Building: Dispenser";
    public static final String DESCRIPTION = "At the end of your turn, restore M health to all allies and draw a card.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALSHAMAN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.ARTIFACT);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/moba/engineersbuildingdispenser.png"),
            CRAFT, TRAITS, RARITY, 2, 0, 2, 4, true, EngineersBuildingDispenser.class,
            new Vector2f(), -1, new EventAnimationDamageDefault(),
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onTurnEndAllied() {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int x = owner.finalStats.get(Stat.MAGIC);
                        List<Minion> alliedTargets = b.getMinions(owner.team, true, true).toList();
                        this.resolve(b, rq, el, new RestoreResolver(effect, alliedTargets, x));
                        this.resolve(b, rq, el, new DrawResolver(owner.player, 1));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return owner.finalStats.get(Stat.MAGIC) * AI.VALUE_PER_HEAL * 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
