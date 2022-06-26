package server.card.cardset.basic.neutral;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import server.ServerBoard;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.SpellText;
import server.card.cardset.standard.neutral.ServantOfDarkness;
import server.card.effect.Effect;
import server.card.effect.common.EffectUnleashPowerOneTimeCost;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.ManaChangeResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class NotCoin extends SpellText {
    public static final String NAME = "Not Coin";
    public static final String DESCRIPTION = "Gain 1 mana orb this turn only. Set the cost of your Unleash Power to 0 for its next use only.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/basic/notcoin.png",
            CRAFT, TRAITS, RARITY, 0, NotCoin.class,
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, new ManaChangeResolver(owner.player, 1, true, false, true));
                        owner.player.getUnleashPower().ifPresent(up -> {
                            Effect buff = new EffectUnleashPowerOneTimeCost("<b>Not Coin</b>", 0);
                            this.resolve(b, rq, el, new AddEffectResolver(up, buff));
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 3;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
