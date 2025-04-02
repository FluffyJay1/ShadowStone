package server.card.cardset.standard.bloodwarlock;

import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.event.Event;
import server.event.EventDamage;
import server.resolver.ManaChangeResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class EndearingSuccubusLilith extends MinionText {
    public static final String NAME = "Endearing Succubus Lilith";
    public static final String DESCRIPTION = "Whenever your leader takes damage during your turn, recover 1 mana orb.";
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/endearingsuccubuslilith.png"),
            CRAFT, TRAITS, RARITY, 3, 1, 2, 5, true, EndearingSuccubusLilith.class,
            new Vector2f(144, 150), 1.3, new EventAnimationDamageSlash(),
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event event) {
                if (event instanceof EventDamage && this.owner.board.getCurrentPlayerTurn() == this.owner.team) {
                    EventDamage ed = (EventDamage) event;
                    if(this.owner.player.getLeader().isPresent()) {
                        if (ed.m.contains(this.owner.player.getLeader().get())) {
                            return new ResolverWithDescription(DESCRIPTION, new ManaChangeResolver(this.owner.player, 1, true, false, false));
                        }
                    }
                }
                return null;
            }

            @Override
            public double getPresenceValue(int refs) {
                // i really dont know how to evaluate this
                return 3;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
