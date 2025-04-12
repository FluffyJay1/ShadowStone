package server.card.cardset.moba.portalhunter;

import java.util.List;

import org.newdawn.slick.geom.Vector2f;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageShoot;
import server.ServerBoard;
import server.ai.AI;
import server.card.Card;
import server.card.CardText;
import server.card.CardRarity;
import server.card.CardStatus;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.PutCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

public class Engineer extends MinionText {
    public static final String NAME = "Engineer";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Put each of the <b>Engineer's Building</b> cards into your deck.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Put a random Artifact from your deck into play and give it +0/+M/+0.";
    public static final String DESCRIPTION = BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final Tooltip ENGINEERS_BUILDINGS_TOOLTIP = new Tooltip("Engineer's Building Cards",
                    "<b>" + EngineersBuildingSentry.NAME + "</b>, <b>" + EngineersBuildingDispenser.NAME + "</b>, and <b>" + EngineersBuildingTeleporter.NAME + "</b>.",
                    () -> List.of(EngineersBuildingSentry.TOOLTIP, EngineersBuildingDispenser.TOOLTIP, EngineersBuildingTeleporter.TOOLTIP));
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/moba/engineer.png"),
            CRAFT, TRAITS, RARITY, 3, 2, 2, 3, false, Engineer.class,
            new Vector2f(), -1, new EventAnimationDamageShoot(),
            () -> List.of(Tooltip.BATTLECRY, ENGINEERS_BUILDINGS_TOOLTIP, Tooltip.UNLEASH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances; // for getBattlecryValue, preview the value of the created cards

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Integer> pos = SelectRandom.positionsToAdd(owner.player.getDeck().size(), 3);
                        List<CardText> cards = List.of(new EngineersBuildingSentry(), new EngineersBuildingDispenser(), new EngineersBuildingTeleporter());
                        this.resolve(b, rq, el, new CreateCardResolver(cards, owner.team, CardStatus.DECK, pos));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = List.of(new EngineersBuildingSentry().constructInstance(this.owner.board), new EngineersBuildingDispenser().constructInstance(this.owner.board), new EngineersBuildingTeleporter().constructInstance(this.owner.board));
                }
                return AI.valueForAddingToDeck(this.cachedInstances, refs);
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Card> eligible = owner.player.getDeck().stream()
                                .filter(c -> c instanceof Minion && c.finalTraits.contains(CardTrait.ARTIFACT))
                                .toList();
                        if (!eligible.isEmpty()) {
                            Card target = SelectRandom.from(eligible);
                            PutCardResolver pcr = this.resolve(b, rq, el, new PutCardResolver(target, CardStatus.BOARD, owner.team, owner.getIndex() + 1, true));
                            if (pcr.event.attempted.get(0)) {
                                int m = owner.finalStats.get(Stat.MAGIC);
                                Effect buff = new Effect("+0/+" + m  + "/+0 (from <b>" + NAME + "</b>).", EffectStats.builder()
                                        .change(Stat.MAGIC, m)
                                        .build());
                                this.resolve(b, rq, el, new AddEffectResolver(target, buff));
                            }
                        }
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return owner.finalStats.get(Stat.MAGIC) + 2; // ???
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
