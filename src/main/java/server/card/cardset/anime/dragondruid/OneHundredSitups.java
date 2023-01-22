package server.card.cardset.anime.dragondruid;

import java.util.List;


import client.tooltip.TooltipSpell;
import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardStatus;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.SpellText;
import server.card.effect.Effect;
import server.card.effect.common.EffectStatChange;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class OneHundredSitups extends SpellText {
    public static final String NAME = "One Hundred Situps";
    private static final String DESCRIPTION = "Give the left-most minion in your hand +2/+2/+1. Put a <b>One Hundred Squats</b> in your hand.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "card/anime/onehundredsitups.png",
            CRAFT, TRAITS, RARITY, 2, OneHundredSitups.class,
            () -> List.of(OneHundredSquats.TOOLTIP),
            List.of());
    
    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        owner.player.getHand().stream()
                                .filter(c -> c instanceof Minion)
                                .findFirst()
                                .ifPresent(c -> {
                                    Effect buff = new EffectStatChange("+2/+2/+1 (from <b>" + NAME + "</b>).", 2, 2, 1);
                                    this.resolve(b, rq, el, new AddEffectResolver(c, buff));
                                });
                        this.resolve(b, rq, el, new CreateCardResolver(new OneHundredSquats(), owner.team, CardStatus.HAND, -1));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.valueForBuff(2, 2, 1) + AI.valueForAddingToHand(List.of(new OneHundredSquats().constructInstance(this.owner.board)), refs);
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}

