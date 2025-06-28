package server.card.cardset.standard.bloodwarlock;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;

import server.Player;
import server.ServerBoard;
import server.ai.AI;
import server.card.Card;
import server.card.CardRarity;
import server.card.CardStatus;
import server.card.CardText;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class MaelstromSerpent extends MinionText {
    public static final String NAME = "Maelstrom Serpent";
    public static final String DESCRIPTION = "<b>Battlecry</b>: Summon a <b>Maelstrom Serpent</b>. Repeat until your area is full if <b>Vengeance</b> is active for you.";
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/maelstromserpent.png"),
            CRAFT, TRAITS, RARITY, 8, 5, 2, 5, true, MaelstromSerpent.class,
            new Vector2f(147, 155), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.VENGEANCE),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances; // for getBattlecryValue, preview the value of the created cards
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int num = owner.player.vengeance() ? Math.max(owner.player.maxPlayAreaSize - owner.player.getPlayArea().size(), 1) : 1;
                        List<Integer> pos = IntStream.range(0, num)
                                .map(i -> owner.getIndex() + (i + 1) * ((i + 1) % 2)) // 1 0 3 0 5 0...
                                .boxed()
                                .toList();
                        List<CardText> summons = Collections.nCopies(num, new MaelstromSerpent());
                        this.resolve(b, rq, el, CreateCardResolver.builder()
                                .withCards(summons)
                                .withTeam(owner.team)
                                .withStatus(CardStatus.BOARD)
                                .withPos(pos)
                                .build());
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = Collections.nCopies(Player.MAX_MAX_BOARD_SIZE, new MaelstromSerpent().constructInstance(this.owner.board));
                }
                int x = owner.player.vengeance() ? 4 : 1;
                return AI.valueForSummoning(this.cachedInstances.subList(0, Math.min(x, this.cachedInstances.size())), refs);
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return owner.player.vengeance();
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
