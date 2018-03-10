package client;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import client.states.StateGame;

public class Game extends StateBasedGame {
	public static int STATE_MENU = 0;
	public static int STATE_GAME = 1;
	public static int STATE_HELP = 2;
	public static final int WINDOW_WIDTH = 1920;
	public static final int WINDOW_HEIGHT = 1080;
	public static final int SERVER_PORT = 9091;
	
	public static Map<String, Image> images = new HashMap<String, Image>();
	
	
	public static void main(String[] args) throws SlickException{
		AppGameContainer app = new AppGameContainer(new Game("ShadowStone"));
		app.setDisplayMode(WINDOW_WIDTH, WINDOW_HEIGHT, false);
		//app.setTargetFrameRate(15);
		app.start();
		
	}
	
	public Game(String title) {
		super(title);
	}

	@Override
	public void initStatesList(GameContainer container) throws SlickException {
		// TODO Auto-generated method stub
		//addState(new StateMenu());
		addState(new StateGame());
			
	}
	public static Image getImage(String path){
		Image i = null;
		if(images.containsKey(path)) {
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
}
