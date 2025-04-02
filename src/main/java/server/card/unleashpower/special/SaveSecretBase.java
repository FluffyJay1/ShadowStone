package server.card.unleashpower.special;

import client.tooltip.Tooltip;
import client.tooltip.TooltipUnleashPower;
import client.ui.Animation;

import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.UnleashPowerText;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.effect.Effect;
import server.event.Event;
import server.resolver.Resolver;
import server.resolver.RestoreResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class SaveSecretBase extends UnleashPowerText {
    public static final String NAME = "Save Secret Base";
    public static final String DESCRIPTION = "<b>Unleash</b> an allied minion. If it has already attacked this turn, restore 3 health to all allies.";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower(NAME, DESCRIPTION, () -> new Animation("unleashpower/special/savesecretbase.png"),
            CRAFT, TRAITS, RARITY, 2, SaveSecretBase.class,
            new Vector2f(), -1,
            () -> List.of(Tooltip.UNLEASH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onUnleashPost(Minion m) {
                Effect effect = this; // anonymous fuckery
                String resolverDescription = "If the unleashed minion has attacked this turn, restore 3 health to all allies.";
                return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        if (m.attacksThisTurn > 0) {
                            List<Minion> relevant = b.getMinions(owner.team, true, true).collect(Collectors.toList());
                            this.resolve(b, rq, el, new RestoreResolver(effect, relevant, 3));
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
}
