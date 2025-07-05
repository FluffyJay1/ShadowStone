package server.card.cardset.anime.neutral;

import java.util.List;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import client.ui.Animation;
import server.ServerBoard;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.SpellText;
import server.card.effect.Effect;
import server.card.target.ModalOption;
import server.card.target.ModalTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.RandomBattlecryResolver;
import server.resolver.RandomUnleashResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class WhiteWhistle extends SpellText {
    public static final String NAME = "White Whistle";
    public static final String DESCRIPTION = "<b>Choose</b> to trigger the <b>Battlecry</b> effects or <b>Unleash</b> effects of all allied minions (targets chosen randomly).";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/anime/whitewhistle.png"),
            CRAFT, TRAITS, RARITY, 3, WhiteWhistle.class,
            () -> List.of(Tooltip.CHOOSE, Tooltip.BATTLECRY, Tooltip.UNLEASH),
            List.of());
    
    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new ModalTargetingScheme(this, 1, "<b>Choose</b> to trigger on all allied minions", List.of(
                        new ModalOption("<b>Battlecry</b>"),
                        new ModalOption("<b>Unleash</b>")
                )));
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<?> options = targetList.get(0).targeted;
                        if (!options.isEmpty()) {
                            int option = (int) options.get(0);
                            List<Minion> minions = b.getMinions(owner.team, false, true).toList();
                            if (option == 0) {
                                for (Minion m : minions) {
                                    this.resolve(b, rq, el, new RandomBattlecryResolver(m));
                                }
                            } else {
                                for (Minion m : minions) {
                                    this.resolve(b, rq, el, new RandomUnleashResolver(owner, m));
                                }
                            }
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
