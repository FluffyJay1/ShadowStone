package client.ui.game;

import java.util.ArrayList;

import org.newdawn.slick.geom.Vector2f;

import client.tooltip.Tooltip;
import client.ui.ScrollingContext;
import client.ui.Text;
import client.ui.UI;
import client.ui.UIBox;
import client.ui.UIElement;

public class CardSelectTooltipPanel extends UIBox {
	ScrollingContext scroll;
	Tooltip tooltip;
	CardSelectTooltipPanel child;
	ArrayList<TooltipDisplayPanel> childTooltips = new ArrayList<TooltipDisplayPanel>();

	public CardSelectTooltipPanel(UI ui, int layers) {
		super(ui, new Vector2f(300, 0), new Vector2f(300, 400), "src/res/ui/uiboxborder.png");
		this.margins.set(10, 10);
		if (layers > 0) {
			this.child = new CardSelectTooltipPanel(ui, layers - 1);
			this.addChild(this.child);
		}
		this.scroll = new ScrollingContext(ui, new Vector2f(), this.getDim(true));
		this.scroll.clip = true;
		this.addChild(this.scroll);
	}

	public void setReferenceTooltip(Tooltip tooltip) {
		if (tooltip == null || tooltip.references == null || tooltip.references.length == 0) {
			this.tooltip = null;
			this.hide = true;
		} else {
			this.scroll.childoffset.y = 0;
			this.tooltip = tooltip;
			this.hide = false;
			if (this.child != null) {
				this.child.hide = true;
			}
			// purge the children
			for (UIElement ue : this.scroll.getChildren()) {
				if (this.childTooltips.contains(ue)) {
					this.scroll.removeChild(ue);
				}
			}
			this.childTooltips.clear();
			double lasty = this.getTop(false, true);
			for (Tooltip t : tooltip.references) {
				TooltipDisplayPanel tdp = new TooltipDisplayPanel(this.ui) {
					@Override
					public void mouseClicked(int button, int x, int y, int clickCount) {
						if (((CardSelectTooltipPanel) this.getParent().getParent()).child != null) {
							if (this.pointIsInHitbox(new Vector2f(x, y))) {
								((CardSelectTooltipPanel) this.getParent().getParent()).child.setReferenceTooltip(t);
							} else {
								((CardSelectTooltipPanel) this.getParent().getParent()).child.setReferenceTooltip(null);
							}
						}
					}
				};
				tdp.setTooltip(t);
				tdp.setPos(new Vector2f(0, (float) lasty), 1);
				lasty += tdp.getHeight(false);

				this.childTooltips.add(tdp);
				this.scroll.addChild(tdp);
			}
		}
	}

}
