package server.card.cardset.indie.neutral;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
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

import java.util.Collections;
import java.util.List;

public class Madotsuki extends MinionText {
    public static final String NAME = "Madotsuki";
    private static final String RELEVANT_CARDS = "<b>Bike Effect</b>, <b>Cat Effect</b>, <b>Fat Effect</b>, <b>Knife Effect</b>, <b>Medamaude Effect</b>, " +
            "<b>Midget Effect</b>, <b>Towel Effect</b>, and <b>Triangle Kerchief Effect</b>.";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Randomly put 2 of the following into your hand: " + RELEVANT_CARDS;
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Gain <b>Rush</b>, and randomly put 2 of the following into your hand: " + RELEVANT_CARDS;
    public static final String DESCRIPTION = BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/indie/madotsuki.png",
            CRAFT, TRAITS, RARITY, 4, 3, 2, 4, false, Madotsuki.class,
            new Vector2f(150, 118), 1.6, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, BikeEffect.TOOLTIP, CatEffect.TOOLTIP, FatEffect.TOOLTIP, KnifeEffect.TOOLTIP,
                    MedamaudeEffect.TOOLTIP, MidgetEffect.TOOLTIP, TowelEffect.TOOLTIP, TriangleKerchiefEffect.TOOLTIP, Tooltip.UNLEASH,
                    Tooltip.RUSH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<CardText> poss = List.of(new BikeEffect(), new CatEffect(), new FatEffect(), new KnifeEffect(),
                                new MedamaudeEffect(), new MidgetEffect(), new TowelEffect(), new TriangleKerchiefEffect());
                        List<CardText> selected = SelectRandom.from(poss, 2);
                        List<Integer> pos = Collections.nCopies(2, -1);
                        this.resolve(b, rq, el, new CreateCardResolver(selected, owner.team, CardStatus.HAND, pos, CardVisibility.ALLIES));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND * 2;
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        Effect rush = new Effect("<b>Rush</b> (from <b>Unleash</b>),", EffectStats.builder()
                                .set(Stat.RUSH, 1)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(owner, rush));
                        List<CardText> poss = List.of(new BikeEffect(), new CatEffect(), new FatEffect(), new KnifeEffect(),
                                new MedamaudeEffect(), new MidgetEffect(), new TowelEffect(), new TriangleKerchiefEffect());
                        List<CardText> selected = SelectRandom.from(poss, 2);
                        List<Integer> pos = Collections.nCopies(2, -1);
                        this.resolve(b, rq, el, new CreateCardResolver(selected, owner.team, CardStatus.HAND, pos, CardVisibility.ALLIES));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
