package client.ui.menu;

import org.newdawn.slick.geom.Vector2f;

import client.ui.GenericButton;
import client.ui.Text;
import client.ui.UI;
import client.ui.UIBox;
import server.card.ClassCraft;

public class ClassSelectPanel extends UIBox {
	public static final String SELECT = "classselect";
	public static final String SELECT_CANCEL = "classselectcancel";

	public ClassSelectPanel(UI ui, Vector2f pos) {
		super(ui, pos, new Vector2f(500, 600), "res/ui/uiboxborder.png");
		this.addChild(new Text(ui, new Vector2f(0, -250), "Select a class", 300, 20, "Verdana", 34, 0, 0));
		for (int i = 0; i < ClassCraft.values().length - 1; i++) {
			GenericButton gb = new GenericButton(ui, new Vector2f(i % 2 * 240 - 120, i / 2 * 100 - 150),
					new Vector2f(240, 80), ClassCraft.values()[i + 1].toString(), i + 1) {
				@Override
				public void mouseClicked(int button, int x, int y, int clickCount) {
					this.alert(SELECT, this.index);
				}
			};

			this.addChild(gb);

		}
		GenericButton gb = new GenericButton(ui, new Vector2f(0, 350), new Vector2f(150, 60), "Cancel", 0) {
			@Override
			public void mouseClicked(int button, int x, int y, int clickCount) {
				this.alert(SELECT_CANCEL);
			}
		};
		this.addChild(gb);
	}

	@Override
	public void onAlert(String strarg, int... intarg) {
		switch (strarg) {
		case SELECT:
			this.setHide(true);
			this.alert(strarg, intarg);
			break;
		case SELECT_CANCEL:
			this.setHide(true);
			this.alert(strarg, intarg);
			break;
		default:
			this.alert(strarg, intarg);
			break;
		}
	}
}
