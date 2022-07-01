package server.card.cardset.special.treasure;

import client.tooltip.TooltipSpell;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;

public class OrbOfDestruction extends SpellText {
    public static final String NAME = "Orb of Destruction";
    public static final String DESCRIPTION = "Destroy 2 of your opponent's mana orbs and they discard 2 cards.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/special/orbofdestruction.png",
            CRAFT, TRAITS, RARITY, 3, OrbOfDestruction.class,
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, new ManaChangeResolver(b.getPlayer(owner.team * -1), -2, true, true, false));
                        List<Card> targets = SelectRandom.from(b.getPlayer(owner.team * -1).getHand(), 2);
                        this.resolve(b, rq, el, new DiscardResolver(targets));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 15; // trust me it good
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
