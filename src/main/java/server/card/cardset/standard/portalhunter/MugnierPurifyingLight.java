package server.card.cardset.standard.portalhunter;

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
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.BanishResolver;
import server.resolver.MuteResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class MugnierPurifyingLight extends MinionText {
    public static final String NAME = "Mugnier, Purifying Light";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: <b>Mute</b> all other minions in play.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: <b>Banish</b> an enemy amulet. Then gain +M/+0/+0 and <b>Rush</b> until the end of the turn.";
    public static final String DESCRIPTION = BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/mugnierpurifyinglight.png"),
            CRAFT, TRAITS, RARITY, 2, 2, 1, 2, false, MugnierPurifyingLight.class,
            new Vector2f(145, 150), 1.7, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.MUTE, Tooltip.UNLEASH, Tooltip.BANISH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<BoardObject> relevant = owner.board.getBoardObjects(0, false, true, false, true).filter(bo -> bo != owner).toList();
                        this.resolve(b, rq, el, new MuteResolver(relevant, true));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_OF_MUTE * 3;
            }

            @Override
            public List<TargetingScheme<?>> getUnleashTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 0, 1, "<b>Banish</b> an enemy amulet.") {
                    @Override
                    protected boolean criteria(Card c) {
                        return c instanceof Amulet && c.isInPlay() && c.status.equals(CardStatus.BOARD)
                                && c != this.getCreator().owner && c.team != owner.team;
                    }
                });
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        getStillTargetableCards(Effect::getUnleashTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                            this.resolve(b, rq, el, new BanishResolver((c)));
                        });
                        int x = owner.finalStats.get(Stat.MAGIC);
                        Effect buff = new Effect("+" + x + "/+0/+0 and <b>Rush</b> until the end of the turn (from <b>Unleash</b>).", EffectStats.builder()
                                .change(Stat.ATTACK, x)
                                .set(Stat.RUSH, 1)
                                .build(),
                                e -> e.untilTurnEndTeam = 0);
                        this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return (AI.VALUE_OF_BANISH / 2 + AI.valueOfRush(this.owner.finalStats.get(Stat.MAGIC) + this.owner.finalStats.get(Stat.ATTACK))) / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
