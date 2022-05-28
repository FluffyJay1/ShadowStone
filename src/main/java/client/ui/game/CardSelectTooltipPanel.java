package client.ui.game;

import java.util.ArrayList;

import org.newdawn.slick.geom.Vector2f;

import client.tooltip.Tooltip;
import client.ui.ScrollingContext;
import client.ui.UI;
import client.ui.UIBox;
import client.ui.UIElement;

public class CardSelectTooltipPanel extends UIBox {
    final ScrollingContext scroll;
    Tooltip tooltip;
    CardSelectTooltipPanel child;
    final ArrayList<TooltipDisplayPanel> childTooltips = new ArrayList<>();

    public CardSelectTooltipPanel(UI ui, Vector2f pos, int layers) {
        super(ui, pos, new Vector2f(450, 550), "res/ui/uiboxborder.png");
        this.margins.set(10, 10);
        if (layers > 0) {
            this.child = new CardSelectTooltipPanel(ui, new Vector2f(450, 0), layers - 1);
            this.addChild(this.child);
        }
        this.scroll = new ScrollingContext(ui, new Vector2f(), this.getDim(true));
        this.scroll.clip = true;
        this.addChild(this.scroll);
    }

    // singular tooltip
    public void setTooltip(Tooltip tooltip) {
        if (tooltip == null) {
            this.tooltip = null;
            this.setVisible(false);
        } else {
            this.tooltip = tooltip;
            this.reset();
            double lasty = -this.getHeight(true) / 2;
            TooltipDisplayPanel tdp = this.createTooltipDisplayPanel(tooltip);
            tdp.setPos(new Vector2f(0, (float) lasty), 1);
            lasty += tdp.getHeight(false);

            this.childTooltips.add(tdp);
            this.scroll.addChild(tdp);
        }
    }

    public void setReferenceTooltip(Tooltip tooltip) {
        if (tooltip == null || tooltip.references == null || tooltip.references.get().size() == 0) {
            this.tooltip = null;
            this.setVisible(false);
        } else {
            this.tooltip = tooltip;
            this.reset();
            double lasty = -this.getHeight(true) / 2;
            for (Tooltip t : tooltip.references.get()) {
                TooltipDisplayPanel tdp = this.createTooltipDisplayPanel(t);
                tdp.setPos(new Vector2f(0, (float) lasty), 1);
                lasty += tdp.getHeight(false);

                this.childTooltips.add(tdp);
                this.scroll.addChild(tdp);
            }
        }
    }

    private void reset() {
        this.scroll.childoffset.y = 0;
        this.setVisible(true);
        if (this.child != null) {
            this.child.setVisible(false);
        }
        // purge the children
        for (UIElement ue : this.scroll.getChildren()) {
            if (this.childTooltips.contains(ue)) {
                this.scroll.removeChild(ue);
            }
        }
        this.childTooltips.clear();
    }

    private TooltipDisplayPanel createTooltipDisplayPanel(Tooltip t) {
        TooltipDisplayPanel tdp = new TooltipDisplayPanel(this.ui) {
            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                if (((CardSelectTooltipPanel) this.getParent().getParent()).child != null) {
                    if (this.pointIsInHitbox(x, y)) {
                        ((CardSelectTooltipPanel) this.getParent().getParent()).child.setReferenceTooltip(t);
                    } else {
                        ((CardSelectTooltipPanel) this.getParent().getParent()).child.setReferenceTooltip(null);
                    }
                }
            }
        };
        tdp.setTooltip(t);
        return tdp;
    }

}
