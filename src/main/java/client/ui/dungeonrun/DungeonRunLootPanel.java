package client.ui.dungeonrun;

import client.Game;
import client.ui.*;
import client.ui.menu.CardDisplayUnit;
import gamemode.dungeonrun.controller.DungeonRunController;
import gamemode.dungeonrun.model.RunState;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardText;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DungeonRunLootPanel extends UIBox {
    private static final float FAN_WIDTH = 1400;
    Text text;
    List<Integer> lootChoices;
    List<Integer> discardChoices;
    List<OptionPanel> options;
    Runnable onFinish;
    boolean inited;

    public DungeonRunLootPanel(UI ui, Vector2f pos, Runnable onFinish) {
        super(ui, pos, new Vector2f(1600, 500), "res/ui/uiboxborder.png");
        this.text = new Text(ui, new Vector2f(0, -225), "bruh", 1000, 20, Game.DEFAULT_FONT, 34, 0, 0);
        this.addChild(text);
        this.lootChoices = new ArrayList<>();
        this.discardChoices = new ArrayList<>();
        this.options = new ArrayList<>();
        this.onFinish = onFinish;
        this.inited = false;
    }

    public void onUpdateRunStatus() {
        if (DungeonRunController.run == null) {
            this.setVisible(false);
            this.inited = false;
        } else if (DungeonRunController.run.state.equals(RunState.LOOTING)) {
            this.setVisible(true);
            if (!this.inited) {
                this.lootChoices.clear();
                this.discardChoices.clear();
                this.updateLoot();
                this.inited = true;
            }
        } else {
            this.setVisible(false);
            this.inited = false;
        }
    }

    private void clearOptions() {
        for (OptionPanel op : this.options) {
            this.removeChild(op);
        }
        this.options.clear();
    }

    private void showOptions(List<List<CardText>> round, String selectText, Consumer<Integer> onSelect) {
        for (int i = 0; i < round.size(); i++) {
            float x = FAN_WIDTH * ((i + 0.5f) / round.size() - 0.5f);
            int finalI = i;
            OptionPanel op = new OptionPanel(this.ui, new Vector2f(x, 0), round.get(i), selectText, () -> {
                onSelect.accept(finalI);
            });
            this.options.add(op);
            this.addChild(op);
        }
    }

    private void updateLoot() {
        if (this.lootChoices.size() == DungeonRunController.run.lootOptions.size()) {
            this.updateDiscard();
            return;
        }
        this.text.setText("Choose your loot!");
        this.clearOptions();
        List<List<CardText>> currentRound = DungeonRunController.run.lootOptions.get(this.lootChoices.size());
        this.showOptions(currentRound, "Select", i -> {
            this.lootChoices.add(i);
            this.updateLoot();
        });
    }

    private void updateDiscard() {
        if (this.discardChoices.size() == DungeonRunController.run.discardOptions.size()) {
            DungeonRunController.endLooting(this.lootChoices, this.discardChoices);
            this.onFinish.run();
            return;
        }
        this.text.setText("Remove cards from your deck...");
        this.clearOptions();
        List<List<CardText>> currentRound = DungeonRunController.run.discardOptions.get(this.discardChoices.size());
        this.showOptions(currentRound, "Discard", i -> {
            this.discardChoices.add(i);
            this.updateDiscard();
        });
    }

    // a div basically
    private static class OptionPanel extends UIElement {
        private static final float FAN_WIDTH = 300;
        OptionPanel(UI ui, Vector2f pos, List<CardText> cardTexts, String selectText, Runnable onSelect) {
            super(ui, pos);
            for (int i = 0; i < cardTexts.size(); i++) {
                float x = FAN_WIDTH * ((i + 0.5f) / cardTexts.size() - 0.5f);
                CardDisplayUnit cdu = new CardDisplayUnit(ui, new Vector2f(x, 0));
                cdu.setCardText(cardTexts.get(i));
                this.addChild(cdu);
            }
            this.addChild(new GenericButton(ui, new Vector2f(0, 100), new Vector2f(200, 50), selectText, onSelect));
        }
    }
}
