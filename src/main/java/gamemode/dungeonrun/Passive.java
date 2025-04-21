package gamemode.dungeonrun;

import java.io.Serializable;
import java.util.List;

import client.tooltip.Tooltip;
import gamemode.dungeonrun.passive.AfterDamageArmor;
import gamemode.dungeonrun.passive.AuraLeftAttackLifesteal;
import gamemode.dungeonrun.passive.TurnEndDeckCost;
import gamemode.dungeonrun.passive.TurnEndHandCost;
import gamemode.dungeonrun.passive.TurnEndLategameBlast;
import gamemode.dungeonrun.passive.TurnEndLowAttackBuff;
import gamemode.dungeonrun.passive.GameStartHandCost;
import gamemode.dungeonrun.passive.OnAttackStats;
import gamemode.dungeonrun.passive.OnPlaySameCardBuff;
import gamemode.dungeonrun.passive.OnPlayThirdCardSummonDraw;
import gamemode.dungeonrun.passive.TurnEndRandomSpell;
import gamemode.dungeonrun.passive.TurnStartReanimate;
import server.card.effect.Effect;

public abstract class Passive implements Serializable {
    public static final List<Passive> ALL = List.of(new AfterDamageArmor(), new AuraLeftAttackLifesteal(), new TurnEndDeckCost(), new TurnEndHandCost(), new TurnEndLategameBlast(), new TurnEndRandomSpell(), new GameStartHandCost(), new OnAttackStats(),
            new OnPlayThirdCardSummonDraw(), new TurnStartReanimate(), new TurnEndLowAttackBuff(), new OnPlaySameCardBuff());
    public abstract Tooltip getTooltip();
    public abstract List<Effect> getEffects();
}
