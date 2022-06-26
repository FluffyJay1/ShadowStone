package server.card.unleashpower.special;

import client.tooltip.Tooltip;
import client.tooltip.TooltipUnleashPower;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.UnleashPowerText;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.effect.Effect;
import server.event.Event;
import server.resolver.DestroyResolver;
import server.resolver.DrawResolver;
import server.resolver.ManaChangeResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class Sacrifice extends UnleashPowerText {
    public static final String NAME = "Sacrifice";
    public static final String DESCRIPTION = "<b>Unleash</b> an allied minion. Then destroy it to gain its health as mana orbs for this turn only and draw a card.";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower(NAME, DESCRIPTION, "res/unleashpower/special/sacrifice.png",
            CRAFT, TRAITS, RARITY, 0, Sacrifice.class,
            new Vector2f(), -1,
            () -> List.of(Tooltip.UNLEASH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onUnleashPost(Minion target) {
            String resolverDescription = "Destroy the unleashed minion to gain its health as mana orbs for this turn only and draw a card.";
                return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, new DestroyResolver(target));
                        this.resolve(b, rq, el, new ManaChangeResolver(owner.player, target.health, true, false, true));
                        this.resolve(b, rq, el, new DrawResolver(owner.player, 1));
                    }
                });
            }
        });
    }

    @Override
    public TooltipUnleashPower getTooltip() {
        return TOOLTIP;
    }
}
