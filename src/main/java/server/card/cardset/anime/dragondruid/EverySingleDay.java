package server.card.cardset.anime.dragondruid;

import java.util.List;
import java.util.stream.Collectors;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import client.ui.Animation;
import server.ServerBoard;
import server.ai.AI;
import server.card.BoardObject;
import server.card.CardRarity;
import server.card.CardStatus;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.SpellText;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.MuteResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class EverySingleDay extends SpellText {
    public static final String NAME = "Every Single Day";
    private static final String DESCRIPTION = "<b>Mute</b> all enemy cards in play. Put a <b>One Hundred Pushups</b> in your hand.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/anime/everysingleday.png"),
            CRAFT, TRAITS, RARITY, 2, EverySingleDay.class,
            () -> List.of(Tooltip.MUTE, OneHundredPushups.TOOLTIP),
            List.of());
    
    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<BoardObject> relevant = owner.board.getBoardObjects(owner.team * -1, false, true, true, true).collect(Collectors.toList());
                        this.resolve(b, rq, el, new MuteResolver(relevant, true));
                        this.resolve(b, rq, el, CreateCardResolver.builder()
                                .withCard(new OneHundredPushups())
                                .withTeam(owner.team)
                                .withStatus(CardStatus.HAND)
                                .withPos(-1)
                                .build());
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                // it's about 2
                return AI.VALUE_OF_MUTE * 2 + AI.valueForAddingToHand(List.of(new OneHundredPushups().constructInstance(this.owner.board)), refs);
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}

