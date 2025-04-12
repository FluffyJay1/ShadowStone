package server.card.cardset.moba.neutral;

import java.util.List;

import org.newdawn.slick.geom.Vector2f;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageShoot;
import server.ServerBoard;
import server.ai.AI;
import server.card.BoardObject;
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

public class Scout extends MinionText {
    public static final String NAME = "Scout";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Give all allied cards in play <b>Lifesteal</b>.";
    public static final String DESCRIPTION = "<b>Storm</b>.\n" + BATTLECRY_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/moba/scout.png"),
            CRAFT, TRAITS, RARITY, 5, 3, 1, 3, true, Scout.class,
            new Vector2f(), -1, new EventAnimationDamageShoot(),
            () -> List.of(Tooltip.STORM, Tooltip.BATTLECRY, Tooltip.LIFESTEAL),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.STORM, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<BoardObject> relevant = b.getBoardObjects(owner.team, false, true, true, true).toList();
                        Effect buff = new Effect("<b>Lifesteal</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .set(Stat.LIFESTEAL, 1)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(relevant, buff));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_OF_LIFESTEAL * 4; // idk
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}

