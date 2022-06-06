package server.card.cardset.basic.shadowshaman;

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
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.ReanimateResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class LadyGreyDeathweaver extends MinionText {
    public static final String NAME = "Lady Grey, Deathweaver";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: <b>Reanimate(X)</b> and gain +X/+0/+0 and <b>Rush</b> until the end of the turn. " +
            "X equals this minion's magic.";
    public static final String DESCRIPTION = "<b>Lifesteal</b>.\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/ladygreydeathweaver.png",
            CRAFT, TRAITS, RARITY, 2, 1, 2, 3, false, LadyGreyDeathweaver.class,
            new Vector2f(150, 140), 1.4, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.LIFESTEAL, Tooltip.UNLEASH, Tooltip.REANIMATE, Tooltip.RUSH));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.LIFESTEAL, 1)
                .build()) {
            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int x = owner.finalStats.get(Stat.MAGIC);
                        this.resolve(b, rq, el, new ReanimateResolver(owner.player, x, owner.getIndex() + 1));
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
                return (this.owner.finalStats.get(Stat.MAGIC)
                        + AI.valueOfRush(this.owner.finalStats.get(Stat.MAGIC) + this.owner.finalStats.get(Stat.ATTACK))) / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
