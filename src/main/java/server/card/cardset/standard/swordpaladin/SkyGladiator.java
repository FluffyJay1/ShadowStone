package server.card.cardset.standard.swordpaladin;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class SkyGladiator extends MinionText {
    public static final String NAME = "Sky Gladiator";
    public static final String ONLISTENEVENT_DESCRIPTION = "Whenever an allied Officer minion comes into play, give it <b>Intimidate</b>.";
    public static final String OTHER_DESCRIPTION = "<b>Intimidate</b>.";
    public static final String DESCRIPTION = OTHER_DESCRIPTION + "\n" + ONLISTENEVENT_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.COMMANDER);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/skygladiator.png"),
            CRAFT, TRAITS, RARITY, 4, 3, 2, 3, true, SkyGladiator.class,
            new Vector2f(175, 126), 1.5, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.INTIMIDATE),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.INTIMIDATE, 1)
                .build()) {
            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event e) {
                if (e != null) {
                    List<BoardObject> relevant = e.cardsEnteringPlay().stream()
                            .filter(bo -> bo.finalTraits.contains(CardTrait.OFFICER) && bo != this.owner && bo.team == this.owner.team)
                            .toList();
                    if (!relevant.isEmpty()) {
                        return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                Effect buff = new Effect("<b>Intimidate</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                        .set(Stat.INTIMIDATE, 1)
                                        .build());
                                this.resolve(b, rq, el, new AddEffectResolver(relevant, buff));
                            }
                        });
                    }
                }
                return null;
            }

            @Override
            public double getPresenceValue(int refs) {
                // idk
                return AI.VALUE_OF_INTIMIDATE * 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
