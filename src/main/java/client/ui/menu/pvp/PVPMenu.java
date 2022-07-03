package client.ui.menu.pvp;

import client.Config;
import client.Game;
import client.states.StatePVP;
import client.ui.*;
import network.DataStream;
import network.ServerGameThread;
import org.newdawn.slick.geom.Vector2f;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.function.Consumer;

public class PVPMenu extends UIBox {
    private static final double ERROR_DISPLAY_TIME = 5;
    private Consumer<DataStream> onConnect;
    private TextField ipInput;
    private GenericButton hostButton;
    private DataStream dsclient;
    private final Text waitingText, errorText;
    private GenericButton cancelHostButton;
    private boolean triggeredOnConnect;

    private double errorTimer;

    public PVPMenu(UI ui, Consumer<DataStream> onConnect) {
        super(ui, new Vector2f(), new Vector2f(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT));
        this.ipInput = new TextField(ui, new Vector2f(-0.25f, 0), new Vector2f(350, 50), "",
                new Text(ui, new Vector2f(), "benis", 300, 50, 40, 0, 0));
        this.ipInput.relpos = true;
        this.addChild(this.ipInput);
        GenericButton connectButton = new GenericButton(ui, new Vector2f(0.5f, 0), new Vector2f(100, 50), "Connect", this::tryConnectToServer);
        connectButton.relpos = true;
        connectButton.alignh = -1;
        this.ipInput.addChild(connectButton);
        Text enterIPText = new Text(ui, new Vector2f(0, -0.5f), "Enter IP of host:", 300, 50, 40, 0, 1);
        enterIPText.relpos = true;
        this.ipInput.addChild(enterIPText);
        this.hostButton = new GenericButton(ui, new Vector2f(0.25f, 0), new Vector2f(300, 50), "Host a game", this::hostGame);
        this.hostButton.relpos = true;
        this.addChild(this.hostButton);
        this.waitingText = new Text(this.ui, new Vector2f(), "Waiting for client to connect...", 1000, 50, 40, 0, 0);
        this.waitingText.setVisible(false);
        this.addChild(this.waitingText);
        this.errorText = new Text(this.ui, new Vector2f(0, 0.4f), "Error", 1000, 50, 40, 0, 0);
        this.errorText.relpos = true;
        this.errorText.setVisible(false);
        this.addChild(this.errorText);
        this.cancelHostButton = new GenericButton(ui, new Vector2f(0, 0.2f), new Vector2f(200, 100), "Cancel", this::cancelHost);
        this.cancelHostButton.relpos = true;
        this.cancelHostButton.setVisible(false);
        this.addChild(this.cancelHostButton);

        this.onConnect = onConnect;
        this.triggeredOnConnect = false;
        this.errorTimer = 0;
    }

    @Override
    public void onAlert(String strarg, int... intarg) {
        switch (strarg) {
            case TextField.TEXT_ENTER -> this.tryConnectToServer();
        }
    }

    private void tryConnectToServer() {
        try {
            Socket socket = new Socket(this.ipInput.getText(), Game.SERVER_PORT);
            this.dsclient = new DataStream(socket);
            this.onConnect.accept(this.dsclient);
            this.triggeredOnConnect = true;
        } catch (UnknownHostException e) {
            this.showError("Error connecting to [" + this.ipInput.getText() + "]: unknown host");
        } catch (ConnectException e) {
            this.showError("Error connecting to [" + this.ipInput.getText() + "]: connection refused");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void hostGame() {
        this.ipInput.setVisible(false);
        this.hostButton.setVisible(false);
        this.waitingText.setVisible(true);
        this.cancelHostButton.setVisible(true);
        this.dsclient = new DataStream();
        DataStream dsserver = new DataStream();
        DataStream.pair(this.dsclient, dsserver);
        StatePVP.serverGameThread = new ServerGameThread(dsserver, true);
    }

    private void cancelHost() {
        this.ipInput.setVisible(true);
        this.hostButton.setVisible(true);
        this.waitingText.setVisible(false);
        this.cancelHostButton.setVisible(false);
        this.shutdownHostServer();
    }

    public void shutdownHostServer() {
        if (this.dsclient != null) {
            this.dsclient.close();
        }
        if (StatePVP.serverGameThread != null) {
            StatePVP.serverGameThread.interrupt();
            StatePVP.serverGameThread = null;
        }
    }

    private void showError(String message) {
        this.errorText.setText(message);
        this.errorTimer = ERROR_DISPLAY_TIME;
    }

    @Override
    public void update(double frametime) {
        super.update(frametime);
        if (this.errorTimer > 0) {
            this.errorTimer -= frametime;
        }
        this.errorText.setVisible(this.errorTimer > 0);
        if (StatePVP.serverGameThread != null && StatePVP.serverGameThread.isPeerConnected() && !this.triggeredOnConnect) {
            this.onConnect.accept(this.dsclient);
            this.triggeredOnConnect = true;
        }
    }
}
