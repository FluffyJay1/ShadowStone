package server.card.cardset.standard.portalhunter;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.*;
import server.card.cardset.basic.portalhunter.AnalyzingArtifact;
import server.card.cardset.basic.portalhunter.AncientArtifact;
import server.card.cardset.basic.portalhunter.MysticArtifact;
import server.card.cardset.basic.portalhunter.RadiantArtifact;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.ArrayList;
import java.util.List;

public class MechWingSwordsman extends MinionText {
    public static final String NAME = "Mech Wing Swordsman";
    private static final String DESCRIPTION = "<b>Battlecry</b>: Randomly put 2 of the following into your deck: " +
            "<b>Ancient Artifact</b>, <b>Analyzing Artifact</b>, <b>Mystic Artifact</b>, and <b>Radiant Artifact</b>.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/mechwingswordsman.png"),
            CRAFT, TRAITS, RARITY, 2, 2, 1, 3, true, MechWingSwordsman.class,
            new Vector2f(152, 130), 1.5, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.COUNTDOWN, AncientArtifact.TOOLTIP, AnalyzingArtifact.TOOLTIP, MysticArtifact.TOOLTIP, RadiantArtifact.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
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
            public double getBattlecryValue(int refs) {
                return 2; // it's hard
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
