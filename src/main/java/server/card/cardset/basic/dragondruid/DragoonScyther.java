package server.card.cardset.basic.dragondruid;

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
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class DragoonScyther extends MinionText {
    public static final String NAME = "Dragoon Scyther";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Gain <b>Storm</b> if <b>Overflow</b> is active for you.";
    public static final String DESCRIPTION = "<b>Bane</b>.\n" + BATTLECRY_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/dragoonscyther.png",
            CRAFT, TRAITS, RARITY, 3, 2, 1, 2, true, DragoonScyther.class,
            new Vector2f(146, 155), 1.4, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.BANE, Tooltip.BATTLECRY, Tooltip.STORM, Tooltip.OVERFLOW));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.BANE, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        if (owner.player.overflow()) {
                            Effect buff = new Effect("<b>Storm</b> (from <b>Battlecry</b>).", EffectStats.builder()
                                    .set(Stat.STORM, 1)
                                    .build());
                            this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_OF_STORM / 2;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.player.overflow();
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
