package server.card.cardset.indie.runemage;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.common.EffectSpellboostDiscount;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.Resolver;
import server.resolver.TransformResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class LordOfTheSecondZone extends SpellText {
    public static final String NAME = "Lord of the Second Zone";
    private static final String BATTLECRY_DESCRIPTION = "<b>Transform</b> an allied minion into <b>Japhet</b>.";
    public static final String DESCRIPTION = BATTLECRY_DESCRIPTION + " " + EffectSpellboostDiscount.DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "card/indie/lordofthesecondzone.png",
            CRAFT, TRAITS, RARITY, 5, LordOfTheSecondZone.class,
            () -> List.of(Tooltip.TRANSFORM, Japhet.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(BATTLECRY_DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 1, 1, BATTLECRY_DESCRIPTION) {
                    @Override
                    protected boolean criteria(Card c) {
                        return c.status.equals(CardStatus.BOARD) && c instanceof Minion && c.team == this.getCreator().owner.team;
                    }
                });
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            this.resolve(b, rq, el, new TransformResolver(c, new Japhet()));
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 3; // yeah sounds about roight
            }
        }, new EffectSpellboostDiscount());
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
