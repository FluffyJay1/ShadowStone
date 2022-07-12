package server.card.cardset.standard.portalhunter;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.DiscardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class AmethystGiant extends MinionText {
    public static final String NAME = "Amethyst Giant";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: If there are Artifacts in your hand, discard 1 and gain <b>Rush</b> and <b>Elusive</b>.";
    public static final String DESCRIPTION = "Can attack 2 times per turn.\n" + BATTLECRY_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/standard/amethystgiant.png",
            CRAFT, TRAITS, RARITY, 9, 6, 3, 9, true, AmethystGiant.class,
            new Vector2f(145, 135), 1.4, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.RUSH, Tooltip.ELUSIVE),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.ATTACKS_PER_TURN, 2)
                .build()) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 0, 1, "Discard an Artifact.") {
                    @Override
                    protected boolean criteria(Card c) {
                        return c.status.equals(CardStatus.HAND) && c.team == this.getCreator().owner.team && c.finalTraits.contains(CardTrait.ARTIFACT)
                                && c != this.getCreator().owner;
                    }
                });
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            this.resolve(b, rq, el, new DiscardResolver(c));
                            Effect buff = new Effect("<b>Rush</b> and <b>Elusive</b> (from <b>Battlecry</b>).", EffectStats.builder()
                                    .set(Stat.RUSH, 1)
                                    .set(Stat.ELUSIVE, 1)
                                    .build());
                            this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.valueOfRush(this.owner) + AI.VALUE_OF_ELUSIVE - AI.VALUE_PER_CARD_IN_HAND; // idk
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.player.getHand().stream()
                        .anyMatch(c -> c.finalTraits.contains(CardTrait.ARTIFACT) && c != this.owner);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
