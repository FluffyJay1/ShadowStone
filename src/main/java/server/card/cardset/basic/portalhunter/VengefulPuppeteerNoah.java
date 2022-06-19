package server.card.cardset.basic.portalhunter;

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
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class VengefulPuppeteerNoah extends MinionText {
    public static final String NAME = "Vengeful Puppeteer Noah";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Put a <b>Puppet</b> in your hand. " +
            "Give +1/+0/+0 and <b>Storm</b> to all <b>Puppets</b> in your hand.";
    public static final String DESCRIPTION = "<b>Storm</b>.\n" + BATTLECRY_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/vengefulpuppeteernoah.png",
            CRAFT, TRAITS, RARITY, 9, 3, 3, 6, true, VengefulPuppeteerNoah.class,
            new Vector2f(162, 136), 1.35, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.STORM, Tooltip.BATTLECRY, Puppet.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.STORM, 1)
                .build()) {
            private List<Card> cachedInstances;
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, new CreateCardResolver(new Puppet(), owner.team, CardStatus.HAND, -1));
                        List<Card> buffTargets = owner.player.getHand().stream()
                                .filter(c -> c.getCardText() instanceof Puppet)
                                .collect(Collectors.toList());
                        Effect buff = new Effect("+1/+0/+0 and <b>Storm</b> (from <b>Vengeful Puppeteer Noah</b>)", EffectStats.builder()
                                .set(Stat.STORM, 1)
                                .change(Stat.ATTACK, 1)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(buffTargets, buff));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = List.of(new Puppet().constructInstance(this.owner.board));
                }
                int numPuppets = (int) this.owner.player.getHand().stream()
                        .filter(c -> c.getCardText() instanceof Puppet)
                        .count();
                return AI.valueForAddingToHand(this.cachedInstances, 1) + (AI.valueForBuff(1, 0, 0) + AI.valueOfStorm(2)) * numPuppets;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
