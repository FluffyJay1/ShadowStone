package server.card.cardset.basic.dragondruid;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.card.target.TargetList;
import server.event.*;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class DragonOracle extends SpellText {
    public static final String NAME = "Dragon Oracle";
    public static final String DESCRIPTION = "Gain one empty mana orb. If <b>Overflow</b> is active for you, draw a card.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/basic/dragonoracle.png",
            CRAFT, TRAITS, RARITY, 2, DragonOracle.class,
            () -> List.of(Tooltip.OVERFLOW),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        Player player = owner.board.getPlayer(owner.team);
                        if (player.overflow()) {
                            this.resolve(b, rq, el, new DrawResolver(player, 1));
                        }
                        this.resolve(b, rq, el, new ManaChangeResolver(player, 1, false, true));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_RAMP + AI.VALUE_PER_CARD_IN_HAND / 2;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.player.overflow();
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
