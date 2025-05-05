package server.card.cardset.moba.portalhunter;

import java.util.List;

import org.newdawn.slick.geom.Vector2f;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageShoot;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSmallExplosion;
import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.BlastResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class EngineersBuildingSentry extends MinionText {
    public static final String NAME = "Engineer's Building: Sentry";
    public static final String DESCRIPTION = "At the end of your turn, <b>Blast(M)</b>.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.ARTIFACT);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/moba/engineersbuildingsentry.png"),
            CRAFT, TRAITS, RARITY, 3, 4, 2, 4, true, EngineersBuildingSentry.class,
            new Vector2f(), -1, new EventAnimationDamageShoot(),
            () -> List.of(Tooltip.BLAST),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onTurnEndAllied() {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, new BlastResolver(effect, owner.finalStats.get(Stat.MAGIC), new EventAnimationDamageSmallExplosion()));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return owner.finalStats.get(Stat.MAGIC) * AI.VALUE_PER_DAMAGE;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
