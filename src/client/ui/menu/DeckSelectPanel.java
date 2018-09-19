package client.ui.menu;

import java.util.ArrayList;

import org.newdawn.slick.geom.Vector2f;

import client.Game;
import client.ui.GenericButton;
import client.ui.ScrollingContext;
import client.ui.Text;
import client.ui.UI;
import client.ui.UIBox;
import server.card.cardpack.ConstructedDeck;

public class DeckSelectPanel extends UIBox {
	public static final String DECK_CONFIRM = "deckselectpanelconfirm";
	public static final String DECK_CANCEL = "deckselectpanelcancel";
	public static final String DECK_DELETE = "deckselectpaneldelete";
	ScrollingContext scroll;
	GenericButton confirmButton, cancelButton, deleteButton;
	ArrayList<DeckSelectUnit> deckButtons = new ArrayList<DeckSelectUnit>();
	public DeckSelectUnit selectedDeckUnit;
	UIBox highlight;
	// who needs inheritance when u can have just one class behave differently
	// depending on what's passed in the constructor
	boolean deckbuild;

	public DeckSelectPanel(UI ui, Vector2f pos, boolean deckbuild) {
		super(ui, pos, new Vector2f(700, 600), "res/ui/uiboxborder.png");
		this.margins.set(10, 10);
		this.deckbuild = deckbuild;
		this.addChild(new Text(ui, new Vector2f(0, -250), "Select a deck", 300, 20, "Verdana", 34, 0, 0));
		this.confirmButton = new GenericButton(ui, new Vector2f(deckbuild ? -125 : -75, 250), new Vector2f(100, 50),
				"Confirm", 0) {
			@Override
			public void mouseClicked(int button, int x, int y, int clickCount) {
				this.alert(DECK_CONFIRM);
			}
		};
		this.addChild(this.confirmButton);
		this.cancelButton = new GenericButton(ui, new Vector2f(deckbuild ? 125 : 75, 250), new Vector2f(100, 50),
				"Back", 1) {
			@Override
			public void mouseClicked(int button, int x, int y, int clickCount) {
				this.alert(DECK_CANCEL);
			}
		};
		this.addChild(this.cancelButton);
		if (deckbuild) {
			this.deleteButton = new GenericButton(ui, new Vector2f(0, 250), new Vector2f(100, 50), "Delete", 1) {
				@Override
				public void mouseClicked(int button, int x, int y, int clickCount) {
					this.alert(DECK_DELETE);
				}
			};
			this.addChild(this.deleteButton);
		}
		this.scroll = new ScrollingContext(ui, new Vector2f(), new Vector2f((float) this.getWidth(true), 400));
		this.scroll.clip = true;
		this.addChild(this.scroll);
		this.highlight = new UIBox(ui, new Vector2f(), new Vector2f(190, 110), "res/ui/highlight.png");
		this.highlight.hide = true;
		this.highlight.ignorehitbox = true;
		this.scroll.addChild(this.highlight);
		this.updateDecks();
	}

	@Override
	public void onAlert(String strarg, int... intarg) {
		switch (strarg) {
		case DECK_CONFIRM:
		case DECK_CANCEL:
			if (this.selectedDeckUnit != null) {
				this.hide = true;
			}
			this.alert(strarg, intarg);
			break;
		case DECK_DELETE:
			if (this.selectedDeckUnit != null) {
				ConstructedDeck.decks.remove(this.selectedDeckUnit.deck);
				ConstructedDeck.saveToFile();
				this.selectedDeckUnit = null;
				this.updateDecks();
			}
			break;
		default:
			this.alert(strarg, intarg);
			break;
		}
	}

	@Override
	public void update(double frametime) {
		super.update(frametime);
		if (this.selectedDeckUnit != null) {
			this.highlight.hide = false;
			this.highlight.setPos(this.selectedDeckUnit.getPos(), 0.99999);
			if (this.deckbuild) {
				if (this.selectedDeckUnit.deck == null) {
					this.deleteButton.hide = true;
				} else {
					this.deleteButton.hide = false;
				}
			}
		} else {
			this.highlight.hide = true;
		}
	}

	public void updateDecks() {
		for (DeckSelectUnit button : this.deckButtons) {
			this.scroll.removeChild(button);
		}
		this.deckButtons.clear();
		int i = 0;
		for (; i < ConstructedDeck.decks.size(); i++) {
			ConstructedDeck cd = ConstructedDeck.decks.get(i);
			DeckSelectUnit button = new DeckSelectUnit(ui);
			button.setDeck(cd);
			button.setPos(new Vector2f((i % 3) * 225 - 225, i / 3 * 125 - 125), 1);
			this.deckButtons.add(button);
			this.scroll.addChild(button);
		}
		if (this.deckbuild) {
			DeckSelectUnit button = new DeckSelectUnit(ui);
			button.setDeck(null);
			button.setPos(new Vector2f((i % 3) * 225 - 225, i / 3 * 125 - 125), 1);
			this.deckButtons.add(button);
			this.scroll.addChild(button);
		}
	}
}
