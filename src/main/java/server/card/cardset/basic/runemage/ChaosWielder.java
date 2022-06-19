package server.card.cardset.basic.runemage;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.EffectUntilTurnEnd;
import server.card.effect.Stat;
import server.card.effect.common.EffectSpellboostDiscount;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.DrawResolver;
import server.resolver.Resolver;
import server.resolver.SpellboostResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class ChaosWielder extends MinionText {
    public static final String NAME = "Chaos Wielder";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Draw 2 cards.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: <b>Spellboost</b> the cards in your hand, and gain +X/+0/+0 and <b>Rush</b> until the end of the turn. X equals this minion's magic.";
    public static final String DESCRIPTION = EffectSpellboostDiscount.DESCRIPTION + "\n" + BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/chaoswielder.png",
            CRAFT, TRAITS, RARITY, 5, 1, 2, 2, false, ChaosWielder.class,
            new Vector2f(166, 114), 1.6, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.SPELLBOOST, Tooltip.BATTLECRY, Tooltip.UNLEASH, Tooltip.RUSH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        String description = BATTLECRY_DESCRIPTION + " " + UNLEASH_DESCRIPTION;
        return List.of(new Effect(description) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new DrawResolver(this.owner.player, 2));
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND * 2;
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, new SpellboostResolver(owner.player.getHand()));
                        int x = owner.finalStats.get(Stat.MAGIC);
                        Effect buff = new EffectUntilTurnEnd("+" + x + "/+0/+0 and <b>Rush</b> until the end of the turn (from <b>Unleash</b>).", EffectStats.builder()
                                .change(Stat.ATTACK, x)
                                .set(Stat.RUSH, 1)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return (AI.VALUE_OF_SPELLBOOST + AI.valueOfRush(this.owner.finalStats.get(Stat.MAGIC) + this.owner.finalStats.get(Stat.ATTACK))) / 2;
            }
        }, new EffectSpellboostDiscount());
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
