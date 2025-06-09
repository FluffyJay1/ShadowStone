package server.card.cardset.standard.shadowdeathknight;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageAOECloud;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class ThreadsOfDespair extends SpellText {
    public static final String NAME = "Threads of Despair";
    public static final String EFFECT_DESCRIPTION = "<b>Last Words</b>: Deal 1 damage to all minions.";
    public static final String DESCRIPTION = "Give all minions \"" + EFFECT_DESCRIPTION + "\"";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWDEATHKNIGHT;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/standard/threadsofdespair.png"),
            CRAFT, TRAITS, RARITY, 3, ThreadsOfDespair.class,
            () -> List.of(Tooltip.LASTWORDS),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> relevant = b.getMinions(0, false, true).toList();
                        this.resolve(b, rq, el, new AddEffectResolver(relevant, new EffectThreadsOfDespair()));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 4; // idk
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }

    public static class EffectThreadsOfDespair extends Effect {
        private static String ADDED_EFFECT_DESCRIPTION = EFFECT_DESCRIPTION + " (From <b>" + NAME + "</b>.)";
        // required for reflection
        public EffectThreadsOfDespair() {
            super(ADDED_EFFECT_DESCRIPTION);
        }

        @Override
        public ResolverWithDescription lastWords() {
            Effect effect = this;
            return new ResolverWithDescription(ADDED_EFFECT_DESCRIPTION, new Resolver(false) {
                @Override
                public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                    List<Minion> relevant = b.getMinions(0, false, true).toList();
                    this.resolve(b, rq, el, new DamageResolver(effect, relevant, 1, true, new EventAnimationDamageAOECloud()));
                }
            });
        }
    }
}
