package com.tkc.appwarp;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.shephertz.app42.gaming.multiplayer.client.WarpClient;
import com.shephertz.app42.gaming.multiplayer.client.command.WarpResponseResultCode;
import com.shephertz.app42.gaming.multiplayer.client.events.RoomEvent;
import com.tkc.adventure.screens.MainGameScreen;

public class WarpController {

    private static WarpController instance;

    private MainGameScreen curScreen;

    private boolean showLog = true;

    private final String apiKey = "d1e921981c6dbda40eb3f2c64e4b3c9cb359fd85b377128be0507e92d19a4a18";
    private final String secretKey = "215cee1c4c8e795136724c294409ade1989d3a2ef3170d4ba7641ab4115a0276";

    private WarpClient warpClient;

    private String localUser;
    private String roomId;

    private boolean isConnected = false;
    boolean isUDPEnabled = false;

    private WarpListener warpListener;

    private int STATE;

    // Game state constants
    public static final int WAITING = 1;
    public static final int STARTED = 2;
    public static final int COMPLETED = 3;
    public static final int FINISHED = 4;

    // Game completed constants
    public static final int GAME_WIN = 5;
    public static final int GAME_LOOSE = 6;
    public static final int ENEMY_LEFT = 7;

    public WarpController() {
        initAppwarp();
        warpClient.addConnectionRequestListener(new ConnectionListener(this));
        warpClient.addChatRequestListener(new com.tkc.appwarp.ChatListener(this));
        warpClient.addZoneRequestListener(new ZoneListener(this));
        warpClient.addRoomRequestListener(new com.tkc.appwarp.RoomListener(this));
        warpClient.addNotificationListener(new com.tkc.appwarp.NotificationListener(this));
    }

    public static WarpController getInstance() {
        if (instance == null) {
            instance = new WarpController();
        }
        return instance;
    }

    public void startApp(String localUser) {
        this.localUser = localUser;
        warpClient.connectWithUserName(localUser);
        System.out.println("connected with username " + localUser);
    }

    public void setCurScreen(MainGameScreen screen) {
        curScreen = screen;
    }

    public void setListener(WarpListener listener) {
        this.warpListener = listener;
    }

    public void stopApp() {
        if (isConnected) {
            warpClient.unsubscribeRoom(roomId);
            warpClient.leaveRoom(roomId);
        }
        warpClient.disconnect();
    }

    private void initAppwarp() {
        try {
            WarpClient.initialize("d1e921981c6dbda40eb3f2c64e4b3c9cb359fd85b377128be0507e92d19a4a18",
                    "215cee1c4c8e795136724c294409ade1989d3a2ef3170d4ba7641ab4115a0276");
            warpClient = WarpClient.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendGameUpdate(String msg) {
        if (isConnected) {
            if (isUDPEnabled) {
                warpClient.sendUDPUpdatePeers((localUser + "#@" + msg).getBytes());
            } else {
                warpClient.sendUpdatePeers((localUser + "#@" + msg).getBytes());
            }
        }
    }

    public void updateResult(int code, String msg) {
        if (isConnected) {
            STATE = COMPLETED;
            HashMap<String, Object> properties = new HashMap<String, Object>();
            properties.put("result", code);
            warpClient.lockProperties(properties);
        }
    }

    public void onConnectDone(boolean status) {
        log("onConnectDone: " + status);
        if (status) {
            warpClient.initUDP();
            warpClient.joinRoom("883447121");
        } else {
            isConnected = false;
            handleError();
        }
    }

    public void onDisconnectDone(boolean status) {

    }

    public void onRoomCreated(String roomId) {
        if (roomId != null) {
            warpClient.joinRoom(roomId);
        } else {
            handleError();
        }
    }

    public void onJoinRoomDone(RoomEvent event) {
        log("onJoinRoomDone: " + event.getResult());
        if (event.getResult() == WarpResponseResultCode.SUCCESS) {// success case
            this.roomId = event.getData().getId();
            warpClient.subscribeRoom(roomId);
        } else if (event.getResult() == WarpResponseResultCode.RESOURCE_NOT_FOUND) {// no such room found
            HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("result", "");
            //warpClient.createRoom("superjumper", "shephertz", 2, data);
        } else {
            warpClient.disconnect();
            handleError();
        }
    }

    public void onRoomSubscribed(String roomId) {
        log("onSubscribeRoomDone: " + roomId);
        if (roomId != null) {
            isConnected = true;
            warpClient.getLiveRoomInfo(roomId);
        } else {
            warpClient.disconnect();
            handleError();
        }
    }

    public void onGetLiveRoomInfo(String[] liveUsers) {
        log("onGetLiveRoomInfo: " + liveUsers.length);
        if (liveUsers != null) {
            for (String username : liveUsers) {
                if (!username.equals(localUser)) {
                    curScreen.newUserJoined(username);
                }
            }
        } else {
            warpClient.disconnect();
            handleError();
        }
    }

    public void onUserJoinedRoom(String roomId, String userName) {
        /*
         * if room id is same and username is different then start the game
		 */
        curScreen.newUserJoined(userName);
    }

    public void onSendChatDone(boolean status) {
        log("onSendChatDone: " + status);
    }

    public void onGameUpdateReceived(String message) {
        //log("onMoveUpdateReceived: message"+ message );
        String userName = message.substring(0, message.indexOf("#@"));
        String xData = message.substring(message.indexOf("#@") + 2, message.indexOf("#@1"));
        String yData = message.substring(message.indexOf("#@1") + 3, message.length());
        if (!localUser.equals(userName)) {
            curScreen.recieveUpdate(userName, xData, yData);
            System.out.println("appwarp recieved update\n" + userName + "\n" + xData + "\n" + yData);
            //warpListener.onGameUpdateReceived(data);
        }
    }

    public void onResultUpdateReceived(String userName, int code) {
        if (localUser.equals(userName) == false) {
            STATE = FINISHED;
            warpListener.onGameFinished(code, true);
        } else {
            warpListener.onGameFinished(code, false);
        }
    }

    public void onUserLeftRoom(String roomId, String userName) {
        log("onUserLeftRoom " + userName + " in room " + roomId);
        curScreen.userLeft(userName);
        if (STATE == STARTED && !localUser.equals(userName)) {// Game Started and other user left the room
            warpListener.onGameFinished(ENEMY_LEFT, true);
        }
    }

    public int getState() {
        return this.STATE;
    }

    private void log(String message) {
        if (showLog) {
            System.out.println(message);
        }
    }

    private void startGame() {
        STATE = STARTED;
        //warpListener.onGameStarted("Start the Game");
    }

    private void waitForOtherUser() {
        STATE = WAITING;
        //warpListener.onWaitingStarted("Waiting for other user");
    }


    private void handleError() {
        if (roomId != null && roomId.length() > 0) {
            warpClient.deleteRoom(roomId);
        }
        disconnect();
    }

    public void handleLeave() {
        if (isConnected) {
            warpClient.unsubscribeRoom(roomId);
            warpClient.leaveRoom(roomId);
            if (STATE != STARTED) {
                warpClient.deleteRoom(roomId);
            }
            warpClient.disconnect();
        }
    }

    private void disconnect() {
        warpClient.removeConnectionRequestListener(new ConnectionListener(this));
        warpClient.removeChatRequestListener(new com.tkc.appwarp.ChatListener(this));
        warpClient.removeZoneRequestListener(new ZoneListener(this));
        warpClient.removeRoomRequestListener(new com.tkc.appwarp.RoomListener(this));
        warpClient.removeNotificationListener(new com.tkc.appwarp.NotificationListener(this));
        warpClient.disconnect();
    }
}