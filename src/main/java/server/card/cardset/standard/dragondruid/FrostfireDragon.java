package server.card.cardset.standard.dragondruid;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageFire;
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
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;

public class FrostfireDragon extends MinionText {
    public static final String NAME = "Frostfire Dragon";
    public static final String DESCRIPTION = "<b>Battlecry</b>: Deal 3 damage to a random enemy minion, then <b>Freeze</b> a random enemy minion.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/frostfiredragon.png"),
            CRAFT, TRAITS, RARITY, 7, 4, 3, 6, true, FrostfireDragon.class,
            new Vector2f(160, 150), 1.3, new EventAnimationDamageFire(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.FROZEN),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        Minion damageChoice = SelectRandom.from(b.getMinions(owner.team * -1, false, true).toList());
                        if (damageChoice != null) {
                            this.resolve(b, rq, el, new DamageResolver(effect, damageChoice, 3, true, new EventAnimationDamageFire()));
                        }
                        Minion freezeChoice = SelectRandom.from(b.getMinions(owner.team * -1, false, true).toList());
                        if (freezeChoice != null) {
                            Effect freezer = new Effect("", EffectStats.builder()
                                    .set(Stat.FROZEN, 1)
                                    .build());
                            this.resolve(b, rq, el, new AddEffectResolver(freezeChoice, freezer));
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.valueOfMinionDamage(3) + AI.VALUE_OF_FREEZE;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
