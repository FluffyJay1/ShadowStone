package server.card.cardset.basic.bloodwarlock;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageDoubleSlice;
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

public class DarkGeneral extends MinionText {
    public static final String NAME = "Dark General";
    public static final String DESCRIPTION = "<b>Battlecry</b>: Gain <b>Storm</b> if <b>Vengeance</b> is active for you.";
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/basic/darkgeneral.png",
            CRAFT, TRAITS, RARITY, 4, 4, 2, 3, true, DarkGeneral.class,
            new Vector2f(131, 157), 1.3, new EventAnimationDamageDoubleSlice(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.STORM, Tooltip.VENGEANCE),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        if (owner.player.vengeance()) {
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
                return this.owner.player.vengeance() ? AI.valueOfStorm(this.owner) : 0;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.player.vengeance();
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
