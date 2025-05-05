package server.card.cardset.standard.portalhunter;

import client.tooltip.TooltipSpell;
import client.ui.Animation;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.MinionAttackResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class CaptivatingConductor extends SpellText {
    public static final String NAME = "Captivating Conductor";
    public static final String DESCRIPTION = "Select 2 minions in play. Make the first minion attack the second.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/standard/captivatingconductor.png"),
            CRAFT, TRAITS, RARITY, 5, CaptivatingConductor.class,
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 2, 2, DESCRIPTION) {
                    @Override
                    protected boolean criteria(Card c) {
                        return c.status.equals(CardStatus.BOARD) && c instanceof Minion;
                    }
                });
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Card> selected = getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).toList();
                        if (selected.size() == 2) {
                            this.resolve(b, rq, el, new MinionAttackResolver((Minion) selected.get(0), (Minion) selected.get(1), false));
                        }
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
