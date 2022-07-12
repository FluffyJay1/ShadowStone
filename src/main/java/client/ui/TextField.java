package client.ui;

import org.newdawn.slick.Input;
import org.newdawn.slick.geom.Vector2f;

public class TextField extends UIBox {
    public static final String TEXT_ENTER = "textenter";
    public static final double SPAM_DELAY = 0.5;
    public static final double SPAM_INTERVAL = 0.02;
    public static final double CURSOR_FLASH_INTERVAL = 0.5;
    int cursorPos;
    String text;
    final Text dispText;
    boolean lctrl, rctrl, lshift, rshift, lalt, ralt, letter;
    int pressedKey;
    char pressedChar;
    double spamTimer = 0;
    double cursorFlashTimer = 0;

    public TextField(UI ui, Vector2f pos, Vector2f dim, String text, Text dispText) {
        super(ui, pos, dim, "ui/uiboxborder.png");
        this.text = text;
        this.dispText = dispText;
        this.dispText.setText(text);
        this.cursorPos = this.text.length();
        this.addChild(this.dispText);
    }

    @Override
    public void update(double frametime) {
        super.update(frametime);
        if (this.letter) {
            this.spamTimer += frametime;
            while (this.spamTimer >= SPAM_DELAY + SPAM_INTERVAL) {
                this.input(this.pressedKey, this.pressedChar);
                this.spamTimer -= SPAM_INTERVAL;
            }
        }
        if (this.hasFocus) {
            this.cursorFlashTimer = (this.cursorFlashTimer + frametime) % (CURSOR_FLASH_INTERVAL * 2);
            if (this.cursorFlashTimer < CURSOR_FLASH_INTERVAL) {
                this.dispText.setText(this.text.substring(0, this.cursorPos) + Text.CURSOR_TOKEN + this.text.substring(this.cursorPos));
            } else {
                this.dispText.setText(this.text);
            }
        } else {
            this.dispText.setText(this.text);
        }
    }

    @Override
    public void keyPressed(int key, char c) {
        switch (key) {
        case Input.KEY_ENTER:
            this.alert(TEXT_ENTER);
            this.ui.focusElement(this.getParent());
            break;
        default:
            this.changeKey(key, c, true);
            break;
        }

    }

    @Override
    public void keyReleased(int key, char c) {
        this.changeKey(key, c, false);
    }

    private void changeKey(int key, char c, boolean pressed) {
        switch (key) {
        case Input.KEY_LCONTROL:
            this.lctrl = pressed;
            break;
        case Input.KEY_RCONTROL:
            this.rctrl = pressed;
            break;
        case Input.KEY_LSHIFT:
            this.lshift = pressed;
            break;
        case Input.KEY_RSHIFT:
            this.rshift = pressed;
            break;
        case Input.KEY_LALT:
            this.lalt = pressed;
            break;
        case Input.KEY_RALT:
            this.ralt = pressed;
            break;
        default:
            if (pressed) {
                this.spamTimer = 0;
                this.letter = true;
                this.pressedKey = key;
                this.pressedChar = c;
                this.input(key, c);
            } else if (this.pressedKey == key) {
                this.letter = false;
            }
        }
    }

    public void input(int key, char c) {
        this.cursorFlashTimer = 0;
        switch (key) {
        case Input.KEY_BACK:
            if (this.cursorPos > 0) {
                this.text = this.text.substring(0, this.cursorPos - 1) + this.text.substring(this.cursorPos);
            }
            // no break
        case Input.KEY_LEFT:
            if (this.cursorPos > 0) {
                this.cursorPos--;
            }
            break;
        case Input.KEY_RIGHT:
            if (this.cursorPos < this.text.length()) {
                this.cursorPos++;
            }
            break;
        default:
            if (c != 0) {
                this.write("" + c);
            }
            break;
        }

    }

    public void write(String chars) {
        this.text = this.text.substring(0, this.cursorPos) + chars + this.text.substring(this.cursorPos);
        this.cursorPos += chars.length();
    }

    public void setText(String text) {
        this.text = text;
        this.cursorPos = text.length();
    }

    public String getText() {
        return this.text;
    }
}
