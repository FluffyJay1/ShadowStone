package server.card.cardset.standard.portalhunter;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.*;
import server.card.cardset.basic.portalhunter.AnalyzingArtifact;
import server.card.cardset.basic.portalhunter.AncientArtifact;
import server.card.cardset.basic.portalhunter.MysticArtifact;
import server.card.cardset.basic.portalhunter.RadiantArtifact;
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
    public static final String DESCRIPTION = "<b>Countdown(3)</b>.\n" + ONTURNENDALLIED_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, "card/standard/ancientamplifier.png",
            CRAFT, TRAITS, RARITY, 1, AncientAmplifier.class,
            new Vector2f(150, 163), 1.3,
            () -> List.of(Tooltip.COUNTDOWN, AncientArtifact.TOOLTIP, AnalyzingArtifact.TOOLTIP, MysticArtifact.TOOLTIP, RadiantArtifact.TOOLTIP),
            List.of());
    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.COUNTDOWN, 3)
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
                        this.resolve(b, rq, el, new CreateCardResolver(selected, owner.team, CardStatus.DECK, pos));
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
