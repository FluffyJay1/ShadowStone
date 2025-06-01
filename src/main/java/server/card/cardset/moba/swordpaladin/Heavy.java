package server.card.cardset.moba.swordpaladin;

import java.util.List;

import org.newdawn.slick.geom.Vector2f;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageShoot;
import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;
import server.resolver.AddEffectResolver;
import server.resolver.BlastResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class Heavy extends MinionText {
    public static final String NAME = "Heavy";
    private static final String STRIKE_DESCRIPTION = "<b>Strike</b>: <b>Blast(1)</b> A times.";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: <b>Freeze</b> this minion.";
    public static final String DESCRIPTION = BATTLECRY_DESCRIPTION + "\n" + STRIKE_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.OFFICER);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/moba/heavy.png"),
            CRAFT, TRAITS, RARITY, 6, 6, 1, 10, true, Heavy.class,
            new Vector2f(), -1, new EventAnimationDamageShoot(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.FROZEN, Tooltip.STRIKE, Tooltip.BLAST),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        Effect freezer = new Effect("", EffectStats.builder()
                                .set(Stat.FROZEN, 1)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(owner, freezer));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return -AI.VALUE_OF_FREEZE; // idk
            }

            @Override
            public ResolverWithDescription strike(Minion target) {
                Effect effect = this;
                return new ResolverWithDescription(STRIKE_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        b.pushEventGroup(new EventGroup(EventGroupType.CONCURRENTDAMAGE));
                        int a = owner.finalStats.get(Stat.ATTACK);
                        for (int i = 0; i < a; i++) {
                            this.resolve(b, rq, el, new BlastResolver(effect, 1, new EventAnimationDamageShoot()));
                        }
                        b.popEventGroup();
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_PER_DAMAGE * owner.finalStats.get(Stat.ATTACK);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}

