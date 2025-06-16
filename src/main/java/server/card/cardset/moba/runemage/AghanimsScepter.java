package server.card.cardset.moba.runemage;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.newdawn.slick.geom.Vector2f;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.Animation;
import server.ServerBoard;
import server.card.AmuletText;
import server.card.Card;
import server.card.CardRarity;
import server.card.CardStatus;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.cardset.basic.runemage.MagicMissile;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.event.EventCreateCard;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

public class AghanimsScepter extends AmuletText {
    public static final String NAME = "Aghanim's Scepter";
    private static final String ONLISTENEVENT_DESCRIPTION = "Whenever an allied card is created, randomly give it <b>Poisonous</b>, <b>Lifesteal</b>, or <b>Freezing Touch</b>.";
    private static final String ONTURNEND_DESCRIPTION = "At the end of your turn, add a <b>Magic Missile</b> to your hand.";
    public static final String DESCRIPTION = ONTURNEND_DESCRIPTION + "\n" + ONLISTENEVENT_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, () -> new Animation("card/moba/aghanimsscepter.png"),
            CRAFT, TRAITS, RARITY, 8, AghanimsScepter.class,
            new Vector2f(), -1,
            () -> List.of(MagicMissile.TOOLTIP, Tooltip.POISONOUS, Tooltip.LIFESTEAL, Tooltip.FREEZING_TOUCH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION){
            @Override
            public ResolverWithDescription onTurnEndAllied() {
                return new ResolverWithDescription(ONTURNEND_DESCRIPTION, new CreateCardResolver(new MagicMissile(), owner.team, CardStatus.HAND, -1));
            }

            @Override
            public ResolverWithDescription onListenEventWhileInPlay(@Nullable Event event) {
                if (event instanceof EventCreateCard) {
                    EventCreateCard ecc = (EventCreateCard) event;
                    if (ecc.team == owner.team && ecc.successfullyCreatedCards.stream().anyMatch(c -> c != owner)) {
                        return new ResolverWithDescription(ONLISTENEVENT_DESCRIPTION, new Resolver(true) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                List<Effect> buffs = List.of(
                                        new Effect("<b>Poisonous</b>. (From <b>" + NAME + "</b>.)", EffectStats.builder()
                                                .set(Stat.POISONOUS, 1)
                                                .build()),
                                        new Effect("<b>Lifesteal</b>. (From <b>" + NAME + "</b>.)", EffectStats.builder()
                                                .set(Stat.LIFESTEAL, 1)
                                                .build()),
                                        new Effect("<b>Freezing Touch</b>. (From <b>" + NAME + "</b>.)", EffectStats.builder()
                                                .set(Stat.FREEZING_TOUCH, 1)
                                                .build())
                                );
                                List<List<Card>> buffBuckets = new ArrayList<>(buffs.size());
                                for (int i = 0; i < buffs.size(); i++) {
                                    buffBuckets.add(new ArrayList<>());
                                }
                                for (Card card : ecc.successfullyCreatedCards) {
                                    SelectRandom.from(buffBuckets).add(card);
                                }
                                for (int i = 0; i < buffs.size(); i++) {
                                    List<Card> buffTargets = buffBuckets.get(i);
                                    if (!buffTargets.isEmpty()) {
                                        this.resolve(b, rq, el, new AddEffectResolver(buffTargets, buffs.get(i)));
                                    }
                                }
                            }
                        });
                    }
                }
                return null;
            }

            @Override
            public double getPresenceValue(int refs) {
                return 8; // idk
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
