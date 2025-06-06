package client.ui.dungeonrun;

import client.Config;
import client.ui.GenericButton;
import client.ui.ScrollingContext;
import client.ui.Text;
import client.ui.UI;
import client.ui.UIBox;
import gamemode.dungeonrun.controller.DungeonRunController;
import gamemode.dungeonrun.model.RunState;
import org.newdawn.slick.geom.Vector2f;

public class DungeonRunMenu extends UIBox {
    private static final Vector2f BUTTON_DIM = new Vector2f(400, 75);
    private static final float BUTTON_SPACING = 20;
    GenericButton playButton, endRunButton, deckButton;
    ScrollingContext passiveTextScrollingContext;
    Text passiveText;
    public DungeonRunMenu(UI ui, Vector2f pos, Runnable onClickPlay, Runnable onClickDeck, Runnable onClickEndRun, Runnable onClickMenu) {
        super(ui, pos, new Vector2f(500, Config.WINDOW_HEIGHT), "ui/uiboxborder.png");
        this.margins.set(10, 25);
        this.playButton = new GenericButton(ui, new Vector2f(0, -0.5f), BUTTON_DIM, "<b>Play</b>", onClickPlay);
        this.playButton.relpos = true;
        this.playButton.alignv = -1;
        this.addChild(playButton);
        this.passiveTextScrollingContext = new ScrollingContext(ui, new Vector2f(0, this.playButton.getBottom(false, false)), new Vector2f(400, 600));
        this.passiveTextScrollingContext.alignv = -1;
        this.passiveTextScrollingContext.margins = new Vector2f(5, 5);
        this.passiveTextScrollingContext.clip = true;
        this.addChild(this.passiveTextScrollingContext);
        this.passiveText = new Text(ui, new Vector2f(-this.passiveTextScrollingContext.getWidth(true) / 2, -this.passiveTextScrollingContext.getHeight(true) / 2), "", this.passiveTextScrollingContext.getWidth(true), 20, 25, -1, -1);
        this.passiveTextScrollingContext.addChild(this.passiveText);
        GenericButton menuButton = new GenericButton(ui, new Vector2f(0, 0.5f), BUTTON_DIM, "Back to Menu", onClickMenu);
        menuButton.relpos = true;
        menuButton.alignv = 1;
        this.addChild(menuButton);
        this.endRunButton = new GenericButton(ui, new Vector2f(menuButton.getPos().x, menuButton.getTop(false, false) - BUTTON_DIM.y / 2 - BUTTON_SPACING),
                BUTTON_DIM, "End Run", onClickEndRun);
        this.addChild(this.endRunButton);
        this.deckButton = new GenericButton(ui, new Vector2f(menuButton.getPos().x, this.endRunButton.getTop(false, false) - BUTTON_DIM.y / 2 - BUTTON_SPACING),
                BUTTON_DIM, "View Deck", onClickDeck);
        this.addChild(this.deckButton);
        this.onUpdateRunStatus();
    }

    public void onUpdateRunStatus() {
        if (DungeonRunController.run == null) {
            this.playButton.setEnabled(false);
            this.endRunButton.setEnabled(false);
            this.deckButton.setEnabled(false);
            this.passiveText.setText("");
        } else {
            RunState runState = DungeonRunController.run.state;
            this.playButton.setEnabled(runState.equals(RunState.PENDING));
            this.endRunButton.setEnabled(true);
            this.deckButton.setEnabled(true);
            this.passiveText.setText(DungeonRunController.run.player.passives.stream().map(p -> "- " + p.getTooltip().description).reduce((a, b) -> a + "\n" + b).orElse("<i>(no passives)</b>"));
        }
    }
}
