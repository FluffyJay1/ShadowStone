package client.ui.dungeonrun;

import client.ui.*;
import client.ui.menu.CardDisplayUnit;
import gamemode.dungeonrun.Passive;
import gamemode.dungeonrun.controller.DungeonRunController;
import gamemode.dungeonrun.model.RunState;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardText;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DungeonRunLootPanel extends UIBox {
    private static final float FAN_WIDTH = 1400;
    Text text;
    List<Integer> passiveChoices;
    List<Integer> treasureChoices;
    List<Integer> lootChoices;
    List<Integer> discardChoices;
    List<OptionPanel> options;
    Runnable onFinish;
    boolean inited;

    public DungeonRunLootPanel(UI ui, Vector2f pos, Runnable onFinish) {
        super(ui, pos, new Vector2f(1600, 500), "ui/uiboxborder.png");
        this.text = new Text(ui, new Vector2f(0, -225), "bruh", 1000, 20, 34, 0, 0);
        this.addChild(text);
        this.passiveChoices = new ArrayList<>();
        this.treasureChoices = new ArrayList<>();
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
                this.passiveChoices.clear();
                this.treasureChoices.clear();
                this.lootChoices.clear();
                this.discardChoices.clear();
                this.updatePassives();
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

    private <T> void showOptions(List<List<T>> round, String selectText, Consumer<Integer> onSelect) {
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

    private void updatePassives() {
        if (DungeonRunController.run.passiveOptions == null || this.passiveChoices.size() == DungeonRunController.run.passiveOptions.size()) {
            this.updateTreasures();
            return;
        }
        this.text.setText("Pick a passive ability!");
        this.clearOptions();
        List<List<Passive>> currentRound = DungeonRunController.run.passiveOptions.get(this.passiveChoices.size());
        this.showOptions(currentRound, "Select", i -> {
            this.passiveChoices.add(i);
            this.updatePassives();
        });
    }

    private void updateTreasures() {
        if (this.treasureChoices.size() == DungeonRunController.run.treasureOptions.size()) {
            this.updateLoot();
            return;
        }
        this.text.setText("Pick your treasures!");
        this.clearOptions();
        List<List<CardText>> currentRound = DungeonRunController.run.treasureOptions.get(this.treasureChoices.size());
        this.showOptions(currentRound, "Select", i -> {
            this.treasureChoices.add(i);
            this.updateTreasures();
        });
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
            DungeonRunController.endLooting(this.passiveChoices, this.treasureChoices, this.lootChoices, this.discardChoices);
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
        OptionPanel(UI ui, Vector2f pos, List<?> loots, String selectText, Runnable onSelect) {
            super(ui, pos);
            for (int i = 0; i < loots.size(); i++) {
                float x = FAN_WIDTH * ((i + 0.5f) / loots.size() - 0.5f);
                Object loot = loots.get(i);
                if (loot instanceof CardText) {
                    CardDisplayUnit cdu = new CardDisplayUnit(ui, new Vector2f(x, 0), (CardText) loot);
                    this.addChild(cdu);
                } else if (loot instanceof Passive) {
                    Text text = new Text(ui,
                            new Vector2f(x, 0), ((Passive) loot).getTooltip().description,
                            450, 24, 30, 0, 0);
                    this.addChild(text);
                }
            }
            this.addChild(new GenericButton(ui, new Vector2f(0, 100), new Vector2f(200, 50), selectText, onSelect));
        }
    }
}
