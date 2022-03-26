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
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, "res/card/basic/puppetroom.png",
            CRAFT, RARITY, 3, PuppetRoom.class,
            new Vector2f(), -1,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.BATTLECRY, Puppet.TOOLTIP));

    @Override
    protected List<Effect> getSpecialEffects() {
        Effect e = new Effect(DESCRIPTION, EffectStats.builder()
                .set(EffectStats.COUNTDOWN, 3)
                .build()
        ) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                String resolverDescription = "<b>Battlecry</b>: put a <b>Puppet</b> in your hand.";
                return new ResolverWithDescription(resolverDescription, new CreateCardResolver(new Puppet(), this.owner.team, CardStatus.HAND, -1));
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND;
            }

            @Override
            public ResolverWithDescription onTurnEnd() {
                String resolverDescription = "At the end of your turn, put a <b>Puppet</b> in your hand.";
                return new ResolverWithDescription(resolverDescription, new CreateCardResolver(new Puppet(), this.owner.team, CardStatus.HAND, -1));
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND * 3;
            }
        };
        e.effectStats.set.setStat(EffectStats.COUNTDOWN, 3);
        return List.of(e);
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
