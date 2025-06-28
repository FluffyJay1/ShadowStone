package server.card.cardset.anime.dragondruid;

import java.util.List;

import client.tooltip.Tooltip;
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
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

public class TenKilometerRun extends SpellText {
    public static final String NAME = "Ten Kilometer Run";
    private static final String DESCRIPTION = "Give the left-most minion in your hand one of the following at random: "
            + "<b>Bane</b>, <b>Poisonous</b>, <b>Storm</b>, <b>Shield(1)</b>, <b>Elusive</b>, or <b>Stalwart</b>. "
            + "Put a <b>Every Single Day</b> in your hand.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/anime/tenkilometerrun.png"),
            CRAFT, TRAITS, RARITY, 2, TenKilometerRun.class,
            () -> List.of(Tooltip.BANE, Tooltip.POISONOUS, Tooltip.STORM, Tooltip.SHIELD, Tooltip.ELUSIVE, Tooltip.STALWART, EverySingleDay.TOOLTIP),
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
                                    this.rng = true;
                                    List<Effect> choices = List.of(
                                            new Effect("<b>Bane</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                                    .set(Stat.BANE, 1)
                                                    .build()),
                                            new Effect("<b>Poisonous</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                                    .set(Stat.POISONOUS, 1)
                                                    .build()),
                                            new Effect("<b>Storm</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                                    .set(Stat.STORM, 1)
                                                    .build()),
                                            new Effect("", EffectStats.builder()
                                                    .change(Stat.SHIELD, 1)
                                                    .build()),
                                            new Effect("<b>Elusive</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                                    .set(Stat.ELUSIVE, 1)
                                                    .build()),
                                            new Effect("<b>Stalwart</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                                    .set(Stat.STALWART, 1)
                                                    .build())
                                    );
                                    Effect buff = SelectRandom.from(choices);
                                    this.resolve(b, rq, el, new AddEffectResolver(c, buff));
                                });
                        this.resolve(b, rq, el, CreateCardResolver.builder()
                                .withCard(new EverySingleDay())
                                .withTeam(owner.team)
                                .withStatus(CardStatus.HAND)
                                .withTeam(-1)
                                .build());
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                // it's about 2
                return 2 + AI.valueForAddingToHand(List.of(new EverySingleDay().constructInstance(this.owner.board)), refs);
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}

