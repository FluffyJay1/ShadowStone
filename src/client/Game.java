package client;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.font.effects.OutlineEffect;
import org.newdawn.slick.state.StateBasedGame;

import client.states.*;
import server.card.cardpack.ConstructedDeck;

public class Game extends StateBasedGame {
	public static int STATE_MENU = 0;
	public static int STATE_GAME = 1;
	public static int STATE_HELP = 2;
	public static int STATE_DECKBUILD = 3;
	public static final int WINDOW_WIDTH = 1920;
	public static final int WINDOW_HEIGHT = 1080;
	public static final int SERVER_PORT = 9091;

	public static final String STRING_START = "\u0fd5", STRING_END = "\u0fd6";
	public static Map<String, Image> images = new HashMap<String, Image>();

	public static Map<String, UnicodeFont> fonts = new HashMap<String, UnicodeFont>();

	public static void main(String[] args) throws SlickException {
		AppGameContainer app = new AppGameContainer(new Game("ShadowStone"));
		app.setDisplayMode(WINDOW_WIDTH, WINDOW_HEIGHT, false);
		// app.setTargetFrameRate(15);
		app.start();

	}

	public Game(String title) {
		super(title);
		ConstructedDeck.loadFromFile();
	}

	@Override
	public void initStatesList(GameContainer container) throws SlickException {
		// TODO Auto-generated method stub
		addState(new StateMenu());
		addState(new StateGame());
		addState(new StateDeckbuild());
	}

	public static Image getImage(String path) {
		Image i = null;
		if (images.containsKey(path)) {
			i = images.get(path).copy();
		} else {
			try {
				i = new Image(path);
				images.put(path, i.copy());
			} catch (SlickException e) {
				System.out.println("Unable to load: " + path);
				e.printStackTrace();
			} finally {
				System.out.println("loaded into memory: " + path);
			}
		}
		return i;
	}

	public static UnicodeFont getFont(String font, double size, boolean bold, boolean italic, Color fillc, Color outc) {
		String condensed = font + (int) size + bold + italic + fillc.toString() + outc.toString(); // map
																									// lifehack
		UnicodeFont f = null;
		if (fonts.containsKey(condensed)) {
			f = fonts.get(condensed);
		} else {
			f = new UnicodeFont(new Font(font, Font.BOLD, (int) size), (int) size, bold, italic);

			try {
				f.getEffects().add(new ColorEffect(fillc));
				f.getEffects().add(new OutlineEffect(1, outc));
				f.addAsciiGlyphs();
				f.loadGlyphs();
			} catch (SlickException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fonts.put(condensed, f);
			System.out.println("loaded new font: " + font + ", size = " + (int) size + (bold ? " bold" : "")
					+ (italic ? " italic" : ""));

		}

		if (f == null) {
			System.out.println("fuckin kil lmyself");
		}
		return f;
	}

	public static UnicodeFont getFont(String font, double size, boolean bold, boolean italic) {
		return getFont(font, size, bold, italic, Color.white, Color.black);
	}

	public static UnicodeFont getFont(String font, double size) {
		return getFont(font, size, false, false);
	}

	public static <T> T selectRandom(ArrayList<T> list) {
		ArrayList<T> ret = selectRandom(list, 1);
		return ret.get(0);
	}

	public static <T> ArrayList<T> selectRandom(ArrayList<T> list, int num) { // helper
		ArrayList<T> copy = new ArrayList<T>();
		copy.addAll(list);
		ArrayList<T> ret = new ArrayList<T>();
		for (int i = 0; i < num && !copy.isEmpty(); i++) {
			int randind = (int) (Math.random() * copy.size());
			ret.add(copy.remove(randind));
		}
		return ret;
	}
}
