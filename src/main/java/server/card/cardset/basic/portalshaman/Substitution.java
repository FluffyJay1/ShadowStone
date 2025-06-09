package server.card.cardset.basic.portalshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import client.ui.Animation;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.BanishResolver;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class Substitution extends SpellText {
    public static final String NAME = "Substitution";
    public static final String DESCRIPTION = "<b>Banish</b> an enemy minion with 4 health or less. Put a <b>Puppet</b> in your hand.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALSHAMAN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/basic/substitution.png"),
            CRAFT, TRAITS, RARITY, 3, Substitution.class,
            () -> List.of(Tooltip.BANISH, Puppet.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 1, 1, DESCRIPTION) {
                    @Override
                    protected boolean criteria(Card c) {
                        return c.status.equals(CardStatus.BOARD) && c instanceof Minion
                                && c.team != this.getCreator().owner.team && ((Minion) c).health <= 4;
                    }
                });
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            this.resolve(b, rq, el, new BanishResolver((c)));
                        });
                        this.resolve(b, rq, el, new CreateCardResolver(new Puppet(), owner.team, CardStatus.HAND, -1));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 5;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
