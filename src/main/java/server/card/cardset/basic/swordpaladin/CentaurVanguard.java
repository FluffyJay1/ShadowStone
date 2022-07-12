package server.card.cardset.basic.swordpaladin;

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

public class CentaurVanguard extends MinionText {
    public static final String NAME = "Centaur Vanguard";
    public static final String DESCRIPTION = "<b>Battlecry</b>: Gain <b>Storm</b> if an allied Commander is in play.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.OFFICER);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/basic/centaurvanguard.png",
            CRAFT, TRAITS, RARITY, 2, 2, 1, 2, true, CentaurVanguard.class,
            new Vector2f(144, 144), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.STORM),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        if (owner.player.getPlayArea().stream()
                                .anyMatch(m -> m.finalTraits.contains(CardTrait.COMMANDER) && m != owner)) {
                            Effect storm = new Effect("<b>Storm</b> (from <b>Battlecry</b>).", EffectStats.builder()
                                    .set(Stat.STORM, 1)
                                    .build());
                            this.resolve(b, rq, el, new AddEffectResolver(owner, storm));
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.valueOfStorm(this.owner) / 2;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.player.getPlayArea().stream().anyMatch(m -> m.finalTraits.contains(CardTrait.COMMANDER));
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
