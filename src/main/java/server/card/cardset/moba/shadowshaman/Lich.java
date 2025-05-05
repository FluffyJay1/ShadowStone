package server.card.cardset.moba.shadowshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageMagicHit;
import client.ui.game.visualboardanimation.eventanimation.destroy.EventAnimationDestroyDarkElectro;

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
import server.resolver.DestroyResolver;
import server.resolver.DrawResolver;
import server.resolver.ManaChangeResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class Lich extends MinionText {
    public static final String NAME = "Lich";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: If another allied minion is in play, " +
            "destroy one to gain its health as mana orbs for this turn only and draw a card.";
    public static final String DESCRIPTION = "<b>Rush</b>.\n" + BATTLECRY_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/moba/lich.png"),
            CRAFT, TRAITS, RARITY, 4, 4, 2, 4, true, Lich.class,
            new Vector2f(137, 154), 1.5, new EventAnimationDamageMagicHit(),
            () -> List.of(Tooltip.RUSH, Tooltip.BATTLECRY),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.RUSH, 1)
                .build()) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 0, 1, "Destroy an allied minion.") {
                    @Override
                    protected boolean criteria(Card c) {
                        return c.status.equals(CardStatus.BOARD) && c instanceof Minion && c.team == this.getCreator().owner.team;
                    }
                });
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getBattlecryTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            Minion target = (Minion) c;
                            this.resolve(b, rq, el, new DestroyResolver(c, new EventAnimationDestroyDarkElectro()));
                            this.resolve(b, rq, el, new ManaChangeResolver(owner.player, target.health, true, false, true));
                            this.resolve(b, rq, el, new DrawResolver(owner.player, 1));
                        });
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND * 1;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.board.getTargetableCards((CardTargetingScheme) this.getBattlecryTargetingSchemes().get(0)).findFirst().isPresent();
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
