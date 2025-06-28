package server.card.cardset.standard.portalshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.Animation;

import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.*;
import server.card.cardset.basic.portalshaman.AnalyzingArtifact;
import server.card.cardset.basic.portalshaman.AncientArtifact;
import server.card.cardset.basic.portalshaman.MysticArtifact;
import server.card.cardset.basic.portalshaman.RadiantArtifact;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.ArrayList;
import java.util.List;

public class AncientAmplifier extends AmuletText {
    public static final String NAME = "Ancient Amplifier";
    private static final String ONTURNENDALLIED_DESCRIPTION = "At the end of your turn, randomly put 2 of the following into your deck: " +
            "<b>Ancient Artifact</b>, <b>Analyzing Artifact</b>, <b>Mystic Artifact</b>, and <b>Radiant Artifact</b>.";
    public static final String DESCRIPTION = "<b>Countdown(4)</b>.\n" + ONTURNENDALLIED_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.PORTALSHAMAN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, () -> new Animation("card/standard/ancientamplifier.png"),
            CRAFT, TRAITS, RARITY, 1, AncientAmplifier.class,
            new Vector2f(150, 163), 1.3,
            () -> List.of(Tooltip.COUNTDOWN, AncientArtifact.TOOLTIP, AnalyzingArtifact.TOOLTIP, MysticArtifact.TOOLTIP, RadiantArtifact.TOOLTIP),
            List.of());
    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.COUNTDOWN, 4)
                .build()) {
            @Override
            public ResolverWithDescription onTurnEndAllied() {
                return new ResolverWithDescription(ONTURNENDALLIED_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<CardText> selected = new ArrayList<>(2);
                        for (int i = 0; i < 2; i++) {
                            selected.add(SelectRandom.from(List.of(new AncientArtifact(), new AnalyzingArtifact(), new MysticArtifact(), new RadiantArtifact())));
                        }
                        List<Integer> pos = SelectRandom.positionsToAdd(owner.player.getDeck().size(), 2);
                        this.resolve(b, rq, el, CreateCardResolver.builder()
                                .withCards(selected)
                                .withTeam(owner.team)
                                .withStatus(CardStatus.DECK)
                                .withPos(pos)
                                .build());
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return 2; // it's hard
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
