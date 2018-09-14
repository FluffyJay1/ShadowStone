package client;

// Turns the game state into a byte array,
// ready to send to the server.
public class GameByteState {

	public GameByteState() {

	}

	public byte[] toByteArray() {
		byte[] b = new byte[1024];
		return b;
	}

}
