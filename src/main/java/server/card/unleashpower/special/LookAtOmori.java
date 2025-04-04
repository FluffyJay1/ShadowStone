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
import server.resolver.MinionAttackResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;
import java.util.stream.Collectors;

public class LookAtOmori extends UnleashPowerText {
    public static final String NAME = "Look at OMORI";
    public static final String DESCRIPTION = "<b>Unleash</b> an allied minion. If it has already attacked this turn, it attacks a random enemy.";
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower(NAME, DESCRIPTION, () -> new Animation("unleashpower/special/lookatomori.png"),
            CRAFT, TRAITS, RARITY, 2, LookAtOmori.class,
            new Vector2f(), -1,
            () -> List.of(Tooltip.UNLEASH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onUnleashPost(Minion m) {
                String resolverDescription = "If the unleashed minion has attacked this turn, it attacks a random enemy.";
                return new ResolverWithDescription(resolverDescription, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        if (m.attacksThisTurn > 0) {
                            List<Minion> possibleTargets = b.getMinions(owner.team * -1, true, true).collect(Collectors.toList());
                            if (!possibleTargets.isEmpty()) { 
                                Minion selected = SelectRandom.from(possibleTargets);
                                this.resolve(b, rq, el, new MinionAttackResolver(m, selected, false));
                            }
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
