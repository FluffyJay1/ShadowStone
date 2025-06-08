package server.card.cardset.standard.portalshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageAOEDarkFire;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageAOESlice;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;

import org.jetbrains.annotations.Nullable;
import org.newdawn.slick.geom.Vector2f;

import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class InesMaidenOfClouds extends MinionText {
    public static final String NAME = "Ines, Maiden of Clouds";
    private static final String ONLISTENSTATECHANGE_DESCRIPTION = "Whenever <b>Resonance</b> becomes active for you, deal M damage to all enemies.";
    public static final String DESCRIPTION = "<b>Intimidate</b>.\n" + ONLISTENSTATECHANGE_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.PORTALSHAMAN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/inesmaidenofclouds.png"),
            CRAFT, TRAITS, RARITY, 2, 2, 1, 2, true, InesMaidenOfClouds.class,
            new Vector2f(188, 204), 1.4, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.RUSH, Tooltip.ELUSIVE),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.INTIMIDATE, 1)
                .build()) {
            @Override
            public Object stateToTrack() {
                return owner.player.resonance();
            }

            @Override
            public ResolverWithDescription onListenStateChangeWhileInPlay(@Nullable Object oldState, @Nullable Object newState) {
                if (newState != null) {
                    boolean newResonance = (Boolean) newState;
                    if (newResonance) {
                        Effect effect = this;
                        return new ResolverWithDescription(ONLISTENSTATECHANGE_DESCRIPTION, new Resolver(false) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                List<Minion> targets = b.getMinions(owner.team * -1, true, true).toList();
                                this.resolve(b, rq, el, new DamageResolver(effect, targets, 1, true, new EventAnimationDamageAOEDarkFire(owner.team * -1, true)));
                            }
                        });
                    }
                }
                return null;
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_PER_DAMAGE * 4;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
