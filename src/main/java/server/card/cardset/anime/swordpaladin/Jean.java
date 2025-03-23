package server.card.cardset.anime.swordpaladin;

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
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class Jean extends MinionText {
    public static final String NAME = "Jean Gunnhildr";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Summon a <b>Dandelion Field</b>.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Return an enemy minion to the opponent's hand and add M to its cost.";
    public static final String DESCRIPTION = BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.COMMANDER);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/anime/jean.png",
            CRAFT, TRAITS, RARITY, 5, 3, 1, 6, false, Jean.class,
            new Vector2f(133, 149), 1.4, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.UNLEASH, DandelionField.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, new CreateCardResolver(new DandelionField(), owner.team, CardStatus.BOARD, owner.getIndex() + 1));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.valueForSummoning(List.of(new DandelionField().constructInstance(owner.board)), refs);
            }

            @Override
            public List<TargetingScheme<?>> getUnleashTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 0, 1, "Return an enemy minion to the opponent's hand.") {
                    @Override
                    protected boolean criteria(Card c) {
                        return c.status.equals(CardStatus.BOARD) && c instanceof Minion && c.team != this.getCreator().owner.team;
                    }
                });
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getUnleashTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            this.resolve(b, rq, el, new PutCardResolver(c, CardStatus.HAND, c.team, -1, true));
                            if (c.alive) {
                                int x = owner.finalStats.get(Stat.MAGIC);
                                Effect debuff = new Effect("+" + x + " cost (from <b>" + NAME + "</b>).", EffectStats.builder()
                                        .change(Stat.COST, x)
                                        .build());
                                this.resolve(b, rq, el, new AddEffectResolver(c, debuff));
                            }
                        });
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return (AI.VALUE_OF_BOUNCE_ENEMY + owner.finalStats.get(Stat.MAGIC)) / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
