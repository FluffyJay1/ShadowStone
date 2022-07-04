package server.card.cardset.basic.portalhunter;

import client.tooltip.TooltipSpell;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.DrawResolver;
import server.resolver.ManaChangeResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class AugmentationBestowal extends SpellText {
    public static final String NAME = "Augmentation Bestowal";
    public static final String DESCRIPTION = "Give your leader the following effect until the end of turn: " +
            "Whenever an Artifact comes into play, recover 1 mana orb and draw a card.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/basic/augmentationbestowal.png",
            CRAFT, TRAITS, RARITY, 1, AugmentationBestowal.class,
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        owner.player.getLeader().ifPresent(l -> {
                            this.resolve(b, rq, el, new AddEffectResolver(l, new AugmentationBestowalEffect()));
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 4; // it's a bretty good card
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }

    public static class AugmentationBestowalEffect extends Effect {
        public static final String EFFECT_DESCRIPTION = "Whenever an Artifact comes into play, recover 1 mana orb and draw a card (from <b>Augmentation Bestowal</b>).";

        public AugmentationBestowalEffect() {
            super(EFFECT_DESCRIPTION);
            this.untilTurnEndTeam = 0;
        }

        @Override
        public ResolverWithDescription onListenEventWhileInPlay(Event e) {
            if (e != null) {
                int count = (int) e.cardsEnteringPlay().stream()
                        .filter(bo -> bo.team == this.owner.team && bo.finalTraits.contains(CardTrait.ARTIFACT) && bo instanceof Minion)
                        .count();
                if (count > 0) {
                    Effect effect = this;
                    return new ResolverWithDescription(EFFECT_DESCRIPTION, new Resolver(false) {
                        @Override
                        public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                            for (int i = 0; i < count; i++) {
                                this.resolve(b, rq, el, new ManaChangeResolver(owner.player, 1, true, false, false));
                                this.resolve(b, rq, el, new DrawResolver(owner.player, 1));
                            }
                        }
                    });
                }
            }
            return null;
        }
    }
}