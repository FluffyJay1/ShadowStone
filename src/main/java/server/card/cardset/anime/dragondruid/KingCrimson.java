package server.card.cardset.anime.dragondruid;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.ManaChangeResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class KingCrimson extends MinionText {
    public static final String NAME = "King Crimson";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Give both players 2 mana orbs.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Refresh your mana orbs.";
    public static final String DESCRIPTION = "<b>Storm</b>.\n" + BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/anime/kingcrimson.png"),
            CRAFT, TRAITS, RARITY, 4, 2, 2, 2, false, KingCrimson.class,
            new Vector2f(144, 150), 1.2, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.STORM, Tooltip.BATTLECRY, Tooltip.UNLEASH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.STORM, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, new ManaChangeResolver(owner.player, 2, true, true, false));
                        this.resolve(b, rq, el, new ManaChangeResolver(b.getPlayer(owner.team * -1), 2, true, true, false));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 2;
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int amount = Math.max(0, owner.player.maxmana - owner.player.mana);
                        this.resolve(b, rq, el, new ManaChangeResolver(owner.player, amount, true, false, false));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return Math.max(0, this.owner.player.maxmana - 2);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
