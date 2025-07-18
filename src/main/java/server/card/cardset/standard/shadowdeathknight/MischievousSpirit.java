package server.card.cardset.standard.shadowdeathknight;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.cardset.basic.shadowdeathknight.Ghost;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.NecromancyResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class MischievousSpirit extends MinionText {
    public static final String NAME = "Mischievous Spirit";
    public static final String DESCRIPTION = "<b>Battlecry</b>: <b>Necromancy(3)</b> - Summon a <b>Ghost</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWDEATHKNIGHT;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/mischievousspirit.png"),
            CRAFT, TRAITS, RARITY, 1, 1, 1, 1, true, MischievousSpirit.class,
            new Vector2f(135, 195), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.NECROMANCY, Ghost.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances; // for getBattlecryValue, preview the value of the created cards
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new NecromancyResolver(this, 3, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, CreateCardResolver.builder()
                                .withCard(new Ghost())
                                .withTeam(owner.team)
                                .withStatus(CardStatus.BOARD)
                                .withPos(owner.getIndex() + 1)
                                .build());
                    }
                }));
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = List.of(new Ghost().constructInstance(this.owner.board));
                }
                return this.owner.player.shadows >= 3 ? AI.valueForSummoning(this.cachedInstances, refs) : 0;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.player.shadows >= 3;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
