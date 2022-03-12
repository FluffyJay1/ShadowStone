package client.ui.dungeonrun;

import client.Config;
import client.Game;
import client.ui.ScrollingContext;
import client.ui.Text;
import client.ui.UI;
import client.ui.UIBox;
import client.ui.menu.CardDisplayUnit;
import gamemode.dungeonrun.controller.DungeonRunController;
import gamemode.dungeonrun.controller.DungeonRunGameRunner;
import gamemode.dungeonrun.model.Contestant;
import gamemode.dungeonrun.model.RunState;
import org.newdawn.slick.Color;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardStatus;

import java.util.ArrayList;
import java.util.List;

public class DungeonRunStatusPanel extends UIBox {
    private static final int ITEM_HEIGHT = 150;
    private static final int ITEM_SPACING = 10;
    private final List<DungeonRunStatusPanelItem> items;
    private final ScrollingContext scroll;
    private final UIBox highlight;
    private final Text gameEndText;
    public DungeonRunStatusPanel(UI ui, Vector2f pos) {
        super(ui, pos, new Vector2f(Config.WINDOW_WIDTH - 500, Config.WINDOW_HEIGHT), "res/ui/uiboxborder.png");
        this.margins.set(30, 30);
        this.items = new ArrayList<>();
        this.scroll = new ScrollingContext(ui, new Vector2f(), new Vector2f((float) this.getWidth(true), (float) this.getHeight(true)));
        this.scroll.clip = true;
        this.addChild(scroll);
        this.highlight = new UIBox(ui, new Vector2f(), new Vector2f(Config.WINDOW_WIDTH - 560, ITEM_HEIGHT), "res/ui/highlight.png");
        this.highlight.setVisible(false);
        this.highlight.ignorehitbox = true;
        this.highlight.setZ(10);
        this.scroll.addChild(this.highlight);
        this.gameEndText = new Text(ui, new Vector2f(), "gg", 1000, 100, Game.DEFAULT_FONT, 80, 0, 0);
        this.gameEndText.setVisible(false);
        this.addChild(gameEndText);
    }

    public void onUpdateRunStatus() {
        if (DungeonRunController.run == null) {
            this.clearList();
            this.highlight.setVisible(false);
            this.gameEndText.setVisible(false);
        } else {
            for (int i = 0; i < DungeonRunController.run.enemies.size(); i++) {
                Contestant enemy = DungeonRunController.run.enemies.get(i);
                if (this.items.size() <= i) {
                    DungeonRunStatusPanelItem item = new DungeonRunStatusPanelItem(this.ui,
                            new Vector2f(0, (float)-this.scroll.getHeight(true) / 2 + i * (ITEM_HEIGHT + ITEM_SPACING)));
                    item.alignv = -1;
                    this.items.add(item);
                    this.scroll.addChild(item);
                }
                DungeonRunStatusPanelItem item = this.items.get(i);
                item.setEnemy(enemy);
            }
            if (DungeonRunController.run.current < this.items.size()) {
                this.highlight.setPos(this.items.get(DungeonRunController.run.current).getCenterPos(), 0.999);
                this.highlight.setVisible(true);
            } else {
                this.highlight.setVisible(false);
            }
            if (DungeonRunController.run.state.equals(RunState.WON)) {
                this.gameEndText.setText("you are <b>win</b>");
                this.gameEndText.setColor(Color.green);
                this.gameEndText.setVisible(true);
            } else if (DungeonRunController.run.state.equals(RunState.LOST)) {
                this.gameEndText.setText("bruh u <i>suck</i>");
                this.gameEndText.setColor(Color.red);
                this.gameEndText.setVisible(true);
            } else {
                this.gameEndText.setVisible(false);
            }
        }
    }

    private void clearList() {
        for (DungeonRunStatusPanelItem item : this.items) {
            this.scroll.removeChild(item);
        }
        this.items.clear();
    }

    private static class DungeonRunStatusPanelItem extends UIBox {
        private static final int MAX_SIGNATURE_CARDS = 3;
        private static final float CARD_SPACING = 10;
        Contestant enemy;
        CardDisplayUnit leader, unleashpower;
        List<CardDisplayUnit> signatureCards;
        Text nameText, classText, signatureCardText;

        public DungeonRunStatusPanelItem(UI ui, Vector2f pos) {
            super(ui, pos, new Vector2f(Config.WINDOW_WIDTH - 560, ITEM_HEIGHT), "res/ui/uiboxborder.png");
            this.margins.set(30, 10);
            this.leader = new CardDisplayUnit(ui, new Vector2f(-(float)this.getWidth(true) / 2, 0));
            this.leader.alignh = -1;
            this.leader.setCardStatus(CardStatus.LEADER);
            this.addChild(this.leader);
            this.unleashpower = new CardDisplayUnit(ui, new Vector2f((float) this.leader.getRight(false, false) + 10, 0));
            this.unleashpower.alignh = -1;
            this.unleashpower.setCardStatus(CardStatus.UNLEASHPOWER);
            this.addChild(this.unleashpower);
            this.nameText = new Text(ui, new Vector2f((float) this.unleashpower.getRight(false, false), 0), "Name",
                    200, 35, Game.DEFAULT_FONT, 44, -1, 0);
            this.addChild(this.nameText);
            this.classText = new Text(ui, new Vector2f((float) this.nameText.getRight(false, false), 0), "Class",
                    200, 22, Game.DEFAULT_FONT, 24, -1, 0);
            this.addChild(this.classText);
            this.signatureCardText = new Text(ui, new Vector2f((float) this.classText.getRight(false, false), 0), "Signature Cards include:",
                    150, 15, Game.DEFAULT_FONT, 20, -1, 0);
            this.addChild(this.signatureCardText);
            this.signatureCards = new ArrayList<>(MAX_SIGNATURE_CARDS);
        }

        public void setEnemy(Contestant enemy) {
            this.enemy = enemy;
            this.leader.setCardText(enemy.leaderText);
            this.leader.setBonusHealth(enemy.getBonusHealth());
            this.unleashpower.setCardText(enemy.unleashPowerText);
            this.nameText.setText(enemy.leaderText.getTooltip().name);
            this.classText.setText(enemy.leaderText.getTooltip().craft.toString());
            this.clearSignatureCards();
            float startX = (float) this.signatureCardText.getRight(false, false) + CARD_SPACING;
            for (int i = 0; i < this.enemy.specialCards.size() && i < MAX_SIGNATURE_CARDS; i++) {
                CardDisplayUnit cdu = new CardDisplayUnit(this.ui, new Vector2f(startX, 0));
                cdu.alignh = -1;
                cdu.setCardText(this.enemy.specialCards.get(i));
                this.signatureCards.add(cdu);
                this.addChild(cdu);
                startX += cdu.getWidth(false) + CARD_SPACING;
            }
        }

        private void clearSignatureCards() {
            for (CardDisplayUnit cdu : this.signatureCards) {
                this.removeChild(cdu);
            }
            this.signatureCards.clear();
        }

    }
}
