package server.card.cardset.anime.dragondruid;

import java.util.List;


import client.tooltip.TooltipSpell;
import client.ui.Animation;
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

public class OneHundredPushups extends SpellText {
    public static final String NAME = "One Hundred Pushups";
    private static final String DESCRIPTION = "Give the left-most minion in your hand +2/+1/+2. Put a <b>One Hundred Situps</b> in your hand.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/anime/onehundredpushups.png"),
            CRAFT, TRAITS, RARITY, 2, OneHundredPushups.class,
            () -> List.of(OneHundredSitups.TOOLTIP),
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
                                    Effect buff = new EffectStatChange("+2/+1/+2 (from <b>" + NAME + "</b>).", 2, 1, 2);
                                    this.resolve(b, rq, el, new AddEffectResolver(c, buff));
                                });
                        this.resolve(b, rq, el, CreateCardResolver.builder()
                                .withCard(new OneHundredSitups())
                                .withTeam(owner.team)
                                .withStatus(CardStatus.HAND)
                                .withPos(-1)
                                .build());
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.valueForBuff(2, 1, 2) + AI.valueForAddingToHand(List.of(new OneHundredSitups().constructInstance(this.owner.board)), refs);
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
