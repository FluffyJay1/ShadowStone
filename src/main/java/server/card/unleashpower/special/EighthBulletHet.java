package server.card.unleashpower.special;

import client.tooltip.Tooltip;
import client.tooltip.TooltipUnleashPower;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.UnleashPowerText;
import server.card.*;
import server.card.effect.Effect;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.DestroyResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class EighthBulletHet extends UnleashPowerText {
    public static final String NAME = "Eighth Bullet Het";
    public static final String DESCRIPTION = "<b>Unleash</b> an allied minion. Summon a plain copy of it that gets destroyed at the end of your opponent's turn.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower(NAME, DESCRIPTION, "unleashpower/special/eighthbullethet.png",
            CRAFT, TRAITS, RARITY, 2, EpitaphsProtection.class,
            new Vector2f(), -1,
            () -> List.of(Tooltip.UNLEASH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onUnleashPost(Minion target) {
                String resolverDescription = "Summon a plain copy of the unleashed minion that gets destroyed at the end of your opponent's turn.";
                return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        CreateCardResolver ccr = this.resolve(b, rq, el, new CreateCardResolver(target.getCardText(), owner.team, CardStatus.BOARD,
                                target.getRelevantBoardPos() + 1));
                        for (Card c : ccr.event.successfullyCreatedCards) {
                            this.resolve(b, rq, el, new AddEffectResolver(c, new EffectEnemyEndTurnDestroy()));
                        }
                    }
                });
            }
        });
    }

    @Override
    public TooltipUnleashPower getTooltip() {
        return TOOLTIP;
    }

    public static class EffectEnemyEndTurnDestroy extends Effect {
        public static final String DESCRIPTION = "Destroy this minion at the end of your opponent's turn (from <b>" + NAME + "</b>).";

        public EffectEnemyEndTurnDestroy() {
            super(DESCRIPTION);
        }

        @Override
        public ResolverWithDescription onTurnEndEnemy() {
            return new ResolverWithDescription(DESCRIPTION, new DestroyResolver(owner));
        }
    }
}
