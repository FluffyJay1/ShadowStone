package server.card.cardset.anime.havenpriest;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageMagicHit;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class YaeMiko extends MinionText {
    public static final String NAME = "Yae Miko";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: If there are 3 or more allied amulets in play, destroy them and <b>Blast(3)</b> X times. " +
            "X equals the number of amulets destroyed.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Return this minion to your hand and subtract M from its cost, then summon a <b>Sesshou Sakura</b>.";
    public static final String DESCRIPTION = "<b>Rush</b>.\n" + BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/anime/yaemiko.png"),
            CRAFT, TRAITS, RARITY, 5, 3, 2, 6, false, Aqua.class,
            new Vector2f(151, 134), 1.6, new EventAnimationDamageMagicHit(),
            () -> List.of(Tooltip.RUSH, Tooltip.BATTLECRY, Tooltip.BLAST, Tooltip.UNLEASH, SesshouSakura.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.RUSH, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<BoardObject> amulets = b.getBoardObjects(owner.team, false, false, true, true).collect(Collectors.toList());
                        if (amulets.size() >= 3) {
                            this.resolve(b, rq, el, new DestroyResolver(amulets));
                            for (int i = 0; i < amulets.size(); i++) {
                                this.resolve(b, rq, el, new BlastResolver(effect, 3, new EventAnimationDamageMagicHit().toString()));
                            }
                        }
                    }
                });
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.board.getBoardObjects(this.owner.team, false, false, true, true).count() >= 3;
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 4; // idk
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int oldpos = owner.getIndex();
                        int x = owner.finalStats.get(Stat.MAGIC);
                        this.resolve(b, rq, el, new PutCardResolver(owner, CardStatus.HAND, owner.team, -1, true));
                        if (owner.alive) {
                            Effect esc = new Effect("-" + x + " cost (from <b>Unleash</b>).");
                            esc.effectStats.change.set(Stat.COST, -x);
                            this.resolve(b, rq, el, new AddEffectResolver(owner, esc));
                        }
                        this.resolve(b, rq, el, new CreateCardResolver(new SesshouSakura(), owner.team, CardStatus.BOARD, oldpos));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.valueForSummoning(List.of(new SesshouSakura().constructInstance(owner.board)), refs) / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
