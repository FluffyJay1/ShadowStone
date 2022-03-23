package client;

import java.awt.*;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import gamemode.dungeonrun.controller.DungeonRunController;
import org.newdawn.slick.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.font.effects.*;
import org.newdawn.slick.state.*;
import org.newdawn.slick.util.Log;

import client.states.*;
import org.newdawn.slick.util.ResourceLoader;
import server.card.cardset.*;

public class Game extends StateBasedGame {
    public static final int STATE_MENU = 0;
    public static final int STATE_GAME = 1;
    public static int STATE_HELP = 2;
    public static final int STATE_DECKBUILD = 3;
    public static final int STATE_DUNGEONRUN = 4;
    public static final int SERVER_PORT = 9091;
    public static Font[] DEFAULT_FONT;
    static {
        try {
            DEFAULT_FONT = new Font[]{
                    Font.createFont(Font.TRUETYPE_FONT, ResourceLoader.getResourceAsStream("res/font/iunito/Iunito-Regular.ttf")), // regular
                    Font.createFont(Font.TRUETYPE_FONT, ResourceLoader.getResourceAsStream("res/font/iunito/Iunito-Black.ttf")), // bold
                    Font.createFont(Font.TRUETYPE_FONT, ResourceLoader.getResourceAsStream("res/font/iunito/Iunito-Italic.ttf")), // italic
                    Font.createFont(Font.TRUETYPE_FONT, ResourceLoader.getResourceAsStream("res/font/iunito/Iunito-BlackItalic.ttf")), // bold + italic
            };
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }
    private static final Color FONT_COLOR = new Color(0.98f, 0.96f, 0.96f);

    public static final String STRING_END = "\u00bd", BLOCK_END = "\u00be", EVENT_END = "\u00b6", EVENT_BURST_END = "\u00bc";
    public static final Map<String, Image> images = new HashMap<>();

    public static final Map<String, UnicodeFont> fonts = new HashMap<>();

    public static void main(String[] args) throws SlickException {
        AppGameContainer app = new AppGameContainer(new Game("ShadowStone"));
        app.setDisplayMode(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT, true);
        // app.setTargetFrameRate(15);
        Log.setVerbose(false);
        app.start();

    }

    public Game(String title) {
        super(title);
        ConstructedDeck.loadFromFile();
        DungeonRunController.loadFromFile();
    }

    @Override
    public void initStatesList(GameContainer container) {
        // TODO Auto-generated method stub
        addState(new StateMenu());
        addState(new StateGame());
        addState(new StateDeckbuild());
        addState(new StateDungeonRun());
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
                // System.out.println("loaded into memory: " + path);
            }
        }
        return i;
    }

    @SuppressWarnings("unchecked")
    public static UnicodeFont getFont(int size, boolean bold, boolean italic, Color fillc, Color outc) {
        String condensed = "" + size + bold + italic + fillc.toString() + outc.toString(); // map lifehack
        int flags = 0;
        if (bold) {
            flags |= 1;
        }
        if (italic) {
            flags |= 2;
        }
        UnicodeFont f;
        if (fonts.containsKey(condensed)) {
            f = fonts.get(condensed);
        } else {
            f = new UnicodeFont(DEFAULT_FONT[flags], size, bold, italic);

            try {
                f.getEffects().add(new ColorEffect(toAwtColor(fillc)));
                if (size * (bold ? 2f : 1.0f) > 40) {
                    f.getEffects().add(new OutlineEffect(1, toAwtColor(outc)));
                }
                f.addAsciiGlyphs();
                f.loadGlyphs();
            } catch (SlickException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            fonts.put(condensed, f);
            /*
             * System.out.println("loaded new font: " + font + ", size = " + (int) size +
             * (bold ? " bold" : "") + (italic ? " italic" : ""));
             */

        }

        if (f == null) {
            System.out.println("fuckin kil lmyself");
        }
        return f;
    }

    public static UnicodeFont getFont(int size, boolean bold, boolean italic) {
        return getFont(size, bold, italic, FONT_COLOR, Color.black);
    }

    public static UnicodeFont getFont(int size) {
        return getFont(size, false, false);
    }

    public static java.awt.Color toAwtColor(Color color) {
        return new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static Color toSlickColor(java.awt.Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static <T> T selectRandom(List<T> list) {
        int randind = (int) (Math.random() * list.size());
        return list.get(randind);
    }

    public static <T> List<T> selectRandom(List<T> list, int num) { // helper
        List<T> copy = new ArrayList<>(list);
        List<T> ret = new ArrayList<>(num);
        for (int i = 0; i < num && !copy.isEmpty(); i++) {
            int randind = (int) (Math.random() * copy.size());
            ret.add(copy.remove(randind));
        }
        return ret;
    }
}
