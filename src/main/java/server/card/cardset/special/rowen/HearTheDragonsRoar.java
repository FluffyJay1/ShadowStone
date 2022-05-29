package server.card.cardset.special.rowen;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import server.ServerBoard;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.SpellText;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.BanishResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class HearTheDragonsRoar extends SpellText {
    public static final String NAME = "Hear the Dragon's Roar";
    public static final String DESCRIPTION = "<b>Banish</b> all enemy minions and amulets.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/special/hear.png",
            CRAFT, TRAITS, RARITY, 10, HearTheDragonsRoar.class,
            () -> List.of(Tooltip.BANISH));
    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el,
                                new BanishResolver(b.getBoardObjects(owner.team * -1, false, true, true, true)
                                        .collect(Collectors.toList())));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 30 / 2.;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
