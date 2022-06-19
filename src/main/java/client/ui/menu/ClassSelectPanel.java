package client.ui.menu;

import org.newdawn.slick.geom.*;

import client.ui.*;
import server.card.*;

public class ClassSelectPanel extends UIBox {
    public static final String SELECT = "classselect";
    public static final String SELECT_CANCEL = "classselectcancel";

    public ClassSelectPanel(UI ui, Vector2f pos, boolean cancelable) {
        super(ui, pos, new Vector2f(500, 600), "res/ui/uiboxborder.png");
        this.addChild(new Text(ui, new Vector2f(0, -250), "Select a class", 300, 20, 34, 0, 0));
        for (int i = 0; i < ClassCraft.values().length - 1; i++) {
            int finalI = i;
            GenericButton gb = new GenericButton(ui, new Vector2f(finalI % 2 * 240 - 120, finalI / 2 * 100 - 150),
                    new Vector2f(240, 80), ClassCraft.values()[finalI + 1].toString(),
                    () -> {
                        this.setVisible(false);
                        this.alert(SELECT, finalI + 1);
                    }
            );

            this.addChild(gb);

        }
        if (cancelable) {
            GenericButton gb = new GenericButton(ui, new Vector2f(0, 350), new Vector2f(150, 60), "Cancel",
                    () -> {
                        this.setVisible(false);
                        this.alert(SELECT_CANCEL);
                    }
            );
            this.addChild(gb);
        }
    }
}
