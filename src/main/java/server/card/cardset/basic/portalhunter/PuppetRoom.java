package server.card.cardset.basic.portalhunter;

import client.tooltip.*;
import org.newdawn.slick.geom.Vector2f;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.card.target.TargetList;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class PuppetRoom extends AmuletText {
    public static final String NAME = "Puppet Room";
    public static final String DESCRIPTION = "<b>Countdown(3)</b>.\n<b>Battlecry</b>: put a <b>Puppet</b> in your hand.\nAt the end of your turn, put a <b>Puppet</b> in your hand.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, "card/basic/puppetroom.png",
            CRAFT, TRAITS, RARITY, 3, PuppetRoom.class,
            new Vector2f(), -1,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.BATTLECRY, Puppet.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        Effect e = new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.COUNTDOWN, 3)
                .build()
        ) {
            private List<Card> cachedInstances; // for getBattlecryValue, preview the value of the created cards
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                String resolverDescription = "<b>Battlecry</b>: put a <b>Puppet</b> in your hand.";
                return new ResolverWithDescription(resolverDescription, new CreateCardResolver(new Puppet(), this.owner.team, CardStatus.HAND, -1));
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = List.of(new Puppet().constructInstance(this.owner.board));
                }
                return AI.valueForAddingToHand(this.cachedInstances, refs);
            }

            @Override
            public ResolverWithDescription onTurnEndAllied() {
                String resolverDescription = "At the end of your turn, put a <b>Puppet</b> in your hand.";
                return new ResolverWithDescription(resolverDescription, new CreateCardResolver(new Puppet(), this.owner.team, CardStatus.HAND, -1));
            }

            @Override
            public double getPresenceValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = List.of(new Puppet().constructInstance(this.owner.board));
                }
                return AI.valueForAddingToHand(this.cachedInstances, refs) * 3;
            }
        };
        e.effectStats.set.set(Stat.COUNTDOWN, 3);
        return List.of(e);
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
