package client.ui;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Vector2f;

import client.Game;

public class Text extends UIElement {
	private String text = ""; // private cuz fuck you
	private ArrayList<ArrayList<String>> lines = new ArrayList<ArrayList<String>>();
	private ArrayList<Double> lineWidths = new ArrayList<Double>();
	double lineWidth, lineHeight, fontsize;

	String font;
	// 0 = normal, 1 = bold, 2 = italics, 3 = both
	UnicodeFont[] uFontFamily = new UnicodeFont[4];

	public Text(UI ui, Vector2f pos, String text, double linewidth, double lineheight, String font, double fontsize,
			int alignh, int alignv) {
		super(ui, pos);
		this.ignorehitbox = true;
		this.lineWidth = linewidth;
		this.lineHeight = lineheight;
		this.alignh = alignh;
		this.alignv = alignv;
		this.setFont(font, fontsize);
		this.setText(text);

	}

	public void setFont(String font, double fontsize) {
		this.font = font;
		this.fontsize = fontsize;
		for (int i = 0; i < 4; i++) {
			this.uFontFamily[i] = Game.getFont(font, fontsize, (i & 1) > 0, (i & 2) > 0);
		}
		if (this.text != null) {
			this.updateText();
		}
	}

	public void setText(String text) {
		if (this.text.equals(text)) { // optimization lul
			return;
		}
		this.text = text;
		this.updateText();
	}

	public void updateText() {
		int flags = 0; // bold and italics
		this.lines.clear();
		this.lineWidths.clear();
		StringTokenizer stlines = new StringTokenizer(text, "\n");
		while (stlines.hasMoreTokens()) {
			// StringTokenizer st = new StringTokenizer(stlines.nextToken(), "
			// ");
			String[] words = stlines.nextToken().split(" ");
			ArrayList<String> line = new ArrayList<String>();
			String sameFontStreak = ""; // to reduce calls to drawString
			double currlinewidth = 0;

			for (String token : words) { // why do i do this
				// String token = st.nextToken();
				if (token.indexOf('<') > -1 && token.indexOf('>') > -1) {
					if (!sameFontStreak.isEmpty()) {
						line.add(sameFontStreak);
						sameFontStreak = "";
					}
					line.add(token);
					if (token.equals("<b>")) {
						flags = flags | 1;
					} else if (token.equals("</b>")) {
						flags = flags & ~1;
					} else if (token.equals("<i>")) {
						flags = flags | 2;
					} else if (token.equals("</i>")) {
						flags = flags & ~2;
					}
				} else {
					if (currlinewidth == 0) { // first word
						if (!token.isEmpty()) {
							sameFontStreak += token;
							currlinewidth += this.uFontFamily[flags].getWidth(token);
						}
					} else if (currlinewidth + this.uFontFamily[flags].getWidth(" " + token) > this.lineWidth) { // newline
						// remove excess spaces at end of line
						for (int i = line.size() - 1; i >= 0 && line.get(i).isEmpty(); i--) {
							currlinewidth -= this.uFontFamily[flags].getWidth(" ");
							line.remove(i);
						}
						if (!sameFontStreak.isEmpty()) {
							line.add(sameFontStreak);
						}
						sameFontStreak = token;
						this.lines.add(line);
						this.lineWidths.add(currlinewidth);
						line = new ArrayList<String>();
						currlinewidth = this.uFontFamily[flags].getWidth(token);
					} else {
						if (sameFontStreak.isEmpty()) {
							sameFontStreak = token;
						} else {
							sameFontStreak += " " + token;
						}
						currlinewidth += this.uFontFamily[flags].getWidth(" " + token);
					}
				}
			}
			if (!sameFontStreak.isEmpty()) {
				line.add(sameFontStreak);
			}
			this.lines.add(line);
			this.lineWidths.add(currlinewidth);
		}
	}

	@Override
	public double getWidth(boolean margin) {
		return this.lineWidth;
	}

	@Override
	public double getHeight(boolean margin) {
		return this.lines.size() == 0 ? 0 : this.lineHeight * (this.lines.size() - 1) + this.fontsize * 1.5; // HELP
	}

	@Override
	public void draw(Graphics g) {
		long start = System.nanoTime();
		if (!this.getHide()) {
			int flags = 0;
			for (int i = 0; i < this.lines.size(); i++) {
				double currlinewidth = 0;
				for (String token : this.lines.get(i)) { // why do i do this
					if (token.equals("<b>")) {
						flags = flags | 1;
					} else if (token.equals("</b>")) {
						flags = flags & ~1;
					} else if (token.equals("<i>")) {
						flags = flags | 2;
					} else if (token.equals("</i>")) {
						flags = flags & ~2;
					} else {
						float drawx = (float) (this.getFinalPos().x + currlinewidth
								- this.lineWidths.get(i) * (this.alignh + 1) / 2.);
						float drawy = (float) (this.getFinalPos().y + this.lineHeight * i - this.getVOff());
						g.setFont(this.uFontFamily[flags]);
						g.drawString(token, drawx, drawy);
						// this.uFontFamily[flags].drawString(drawx, drawy,
						// token);

						currlinewidth += this.uFontFamily[flags].getWidth(token + " ");
					}
				}
			}
			this.drawChildren(g);// why not
			// System.out.println((System.nanoTime() - start) + " for " + this.text);

		}
	}

	@Override
	public String debug() {
		return super.debug() + " " + this.text;
	}
}
