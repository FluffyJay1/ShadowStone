package server.card.cardset.moba.runemage;

import java.util.Collections;
import java.util.List;

import org.newdawn.slick.geom.Vector2f;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageEnergyBeamQuick;
import server.ServerBoard;
import server.card.CardRarity;
import server.card.CardStatus;
import server.card.CardText;
import server.card.CardTrait;
import server.card.CardVisibility;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.SpellText;
import server.card.cardset.CardSet;
import server.card.cardset.basic.runemage.ClayGolem;
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
import utils.SelectRandom;

public class Aghanim extends MinionText {
    public static final String NAME = "Aghanim";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Summon a <b>Aghanim's Scepter</b>. If one already exists, summon 2 <b>Clay Golems</b> instead.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Add 2 random spells to your hand and subtract M from their costs.";
    public static final String DESCRIPTION = BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/moba/aghanim.png"),
            CRAFT, TRAITS, RARITY, 8, 8, 3, 8, false, Aghanim.class,
            new Vector2f(160, 170), 1.1, new EventAnimationDamageEnergyBeamQuick(),
            () -> List.of(Tooltip.BATTLECRY, AghanimsScepter.TOOLTIP, ClayGolem.TOOLTIP, Tooltip.UNLEASH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        if (owner.player.getPlayArea().stream().anyMatch(bo -> bo.getCardText() instanceof AghanimsScepter)) {
                            this.resolve(b, rq, el, CreateCardResolver.builder()
                                    .withCards(List.of(new ClayGolem(), new ClayGolem()))
                                    .withTeam(owner.team)
                                    .withStatus(CardStatus.BOARD)
                                    .withPos(List.of(owner.getIndex() + 1, owner.getIndex()))
                                    .build());
                        } else {
                            this.resolve(b, rq, el, CreateCardResolver.builder()
                                    .withCard(new AghanimsScepter())
                                    .withTeam(owner.team)
                                    .withStatus(CardStatus.BOARD)
                                    .withPos(owner.getIndex() + 1)
                                    .build());
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 6; // idk
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<CardText> randomCards = SelectRandom.from(CardSet.PLAYABLE_SET.get().stream().filter(ct -> ct instanceof SpellText).toList(), 2);
                        if (!randomCards.isEmpty()) {
                            int m = owner.finalStats.get(Stat.MAGIC);
                            Effect costBuff = new Effect("-" + m + " cost. (From <b>" + NAME + "</b>.)", EffectStats.builder()
                                    .change(Stat.COST, -m)
                                    .build());
                            this.resolve(b, rq, el, CreateCardResolver.builder()
                                    .withCards(randomCards)
                                    .withTeam(owner.team)
                                    .withStatus(CardStatus.HAND)
                                    .withPos(Collections.nCopies(randomCards.size(), -1))
                                    .withVisibility(CardVisibility.ALLIES)
                                    .withAdditionalEffectForAll(costBuff)
                                    .build());
                        }
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return owner.finalStats.get(Stat.MAGIC) * 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}

