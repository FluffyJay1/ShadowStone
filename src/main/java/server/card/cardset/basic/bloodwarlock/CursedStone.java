package server.card.cardset.basic.bloodwarlock;

import java.util.*;

import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageEnergyBeam;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.card.effect.common.EffectLastWordsAlliedBlast;
import server.card.target.TargetList;
import server.event.*;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class CursedStone extends MinionText {
    public static final String NAME = "Cursed Stone";
    public static final String DESCRIPTION = "<b>Unleash</b>: <b>Blast(M)</b> and gain <b>Last Words</b>: Deal M damage to a random allied minion.";
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/basic/cursedstone.png"),
            CRAFT, TRAITS, RARITY, 3, 1, 5, 7, false, CursedStone.class,
            new Vector2f(), -1, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.UNLEASH, Tooltip.BLAST, Tooltip.LASTWORDS),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                Effect effect = this; // anonymous fuckery
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int damage = owner.finalStats.get(Stat.MAGIC);
                        this.resolve(b, rq, el, new BlastResolver(effect, damage, new EventAnimationDamageEnergyBeam()));
                        Effect lw = new EffectLastWordsAlliedBlast("<b>Unleash</b>", damage, new EventAnimationDamageEnergyBeam());
                        this.resolve(b, rq, el, new AddEffectResolver(effect.owner, lw));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                int damage = this.owner.finalStats.get(Stat.MAGIC);
                return AI.VALUE_PER_DAMAGE * damage / 2.;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
