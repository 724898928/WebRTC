package com.lee.myapplication;

import static com.lee.util.MsgContant.ANSWER;
import static com.lee.util.MsgContant.CANDIDATE;
import static com.lee.util.MsgContant.OFFER;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.blankj.utilcode.util.StringUtils;
import com.google.gson.Gson;
import com.lee.model.Message;
import com.lee.model.MsgData;
import com.lee.model.Room;
import com.lee.model.User;
import com.lee.util.MsgContant;

import org.java_websocket.handshake.ServerHandshake;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import java.net.URI;
import java.util.ArrayList;

public class WebSocketClient extends org.java_websocket.client.WebSocketClient implements PeerActionObserver {
    private static final String TAG = WebSocketClient.class.getSimpleName();
    private String sessionId;
    private Message startMsg;
    private MsgData msgData;
    private PeerConnection peerConnection;
    private MySdpObserver observer;
    private Handler handler;
    private Context context;
    private  android.os.Message osMsg;

    public void setPeerConnection(PeerConnection peerConnection) {
        Log.d(TAG, "setPeerConnection = " + peerConnection);
        this.peerConnection = peerConnection;
        this.observer = initObserver(this);;
    }


    public WebSocketClient(Context context,URI serverUri, Message<Room> startMsg, Handler handler) {
        super(serverUri);
        this.context = context;
        this.startMsg = startMsg;
        this.msgData = new Gson().fromJson(this.startMsg.getData().toString(), MsgData.class);
        this.handler = handler;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.d(TAG, "onOpen: handshakedata ->" + handshakedata);
        if (null != this.startMsg) {
            this.send(this.startMsg.toString());
        }
        osMsg = new android.os.Message();
        osMsg.what = 1;
        handler.handleMessage(osMsg);
    }

    @Override
    public void onMessage(String message) {
        MsgData getData = null;
        if (!StringUtils.isTrimEmpty(message)) {
            Message msg = new Gson().fromJson(message, Message.class);
            String msgData = new Gson().toJson(msg.getData()) ;
            if (null != msgData){
                getData = new Gson().fromJson(msgData,MsgData.class);
            }
            switch (msg.getType()) {
                case MsgContant.JOIN_ROOM:
                    Log.d(TAG, "onMessage getData -> "+getData);
                    if (null!= getData){
                        this.createOffer();
                        this.onJoinRoom();
                    }else {
                        Log.d(TAG, "onMessage getData = null");
                        osMsg.what = 2;
                        handler.handleMessage(osMsg);
                        handler.getLooper().quitSafely();
                    }
                    break;
                case MsgContant.OFFER:
                    // 提议方发过来的Offer处理
                    SessionDescription sessionDescription1 = getData.getSessionDescription();
                    peerConnection.setRemoteDescription(observer, sessionDescription1);
                    this.createAnswer();
                    break;
                case MsgContant.CANDIDATE:
                    // 服务端 发送 接收方sdp Answer
                    this.onCandidate(getData);
                    break;
                case MsgContant.UPDATE_USER_LIST:
                    this.onUpdateUserList(msg);
                    break;
                case MsgContant.HANGUP:
                    this.onHangUp(msg);
                    break;
                case MsgContant.LEAVE_ROOM:
                    this.onLeaveRoom(msg);
                    break;
                case MsgContant.HEART_PACKAGE:
                    this.heartPackage(msg);
                    break;
                default:
                    Log.d(TAG, "未知消息:" + msg);

            }

        }

    }

    private void onJoinRoom() {
        Log.d(TAG, "onJoinRoom ");
    }

    private void createOffer() {
        MediaConstraints mediaConstraints = new MediaConstraints();
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        if (null != observer){
            peerConnection.createOffer(observer, mediaConstraints);
        }else {
            Log.d(TAG, "createOffer: observer=null");
        }

    }

    private void createAnswer() {
        MediaConstraints mediaConstraints = new MediaConstraints();
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        if (null != observer){
            peerConnection.createAnswer(observer, mediaConstraints);
        }else {
            Log.d(TAG, "createAnswer: observer=null");
        }
    }

    private MySdpObserver initObserver(WebSocketClient webSocketClient) {
        Log.d(TAG, "MySdpObserver initObserver");
        return new MySdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.e(TAG, "onCreateSuccess sessionDescription = " + sessionDescription);
                //将会话描述设置在本地
                webSocketClient.peerConnection.setLocalDescription(this, sessionDescription);
                SessionDescription localDescription = webSocketClient.peerConnection.getLocalDescription();
                SessionDescription.Type type = localDescription.type;
                Log.e(TAG, "onCreateSuccess == " + " type == " + type);
                //接下来使用之前的WebSocket实例将offer发送给服务器
                if (type == SessionDescription.Type.OFFER) {
                    //呼叫
                    webSocketClient.onOffer(sessionDescription);
                } else if (type == SessionDescription.Type.ANSWER) {
                    //应答
                    webSocketClient.onAnswer(sessionDescription);
                } else if (type == SessionDescription.Type.PRANSWER) {
                    //再次应答

                }
            }
        };

    }
    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d(TAG, "onClose: reason ->" + reason);
    }

    @Override
    public void onError(Exception ex) {
        Log.d(TAG, "onError: ex ->" + ex);
    }


    /**
     * 呼叫
     *
     * @param sdp
     */
    @Override
    public void onOffer(SessionDescription sdp) {
        Log.d(TAG, "onOffer!");
        // 应答方法设置远端会话描述SDP
        if (null!= this.msgData){
            this.msgData.setSessionDescription(sdp);
            Message message = new Message(OFFER, this.msgData);
            Log.d(TAG, "onOffer " + message);
            this.send(message.toString());
        }
    }

    @Override
    public void onAnswer(SessionDescription sdp) {
        Log.d(TAG, "onAnswer!");
        if (null!= this.msgData){
            this.msgData.setSessionDescription(sdp);
            Message message = new Message(OFFER, this.msgData);
            Log.d(TAG, "onAnswer " + message);
            this.send(message.toString());
        }
    }

    @Override
    public void onCandidate(MsgData msg) {
        Log.d(TAG, "onCandidate! +msg= "+msg);
        IceCandidate iceCandidate = msg.getIceCandidate();
        if (null != iceCandidate) {
            Log.d(TAG, "onCandidate iceCandidate="+iceCandidate);
            peerConnection.addIceCandidate(iceCandidate);
          //  this.send(message.toString());
        }else {
            Log.d(TAG, "onCandidate iceCandidate=null");
        }
    }

    @Override
    public ArrayList<User> onUpdateUserList(Object msg) {
        Log.d(TAG, "onUpdateUserList! msg -> " + msg);
        Message getMsg = new Gson().fromJson(msg.toString(), Message.class);
        ArrayList<User> users = new Gson().fromJson(getMsg.getData().toString(), ArrayList.class);
        Log.d(TAG, "onUpdateUserList sendMsg -> " + users);
        return users;
    }

    @Override
    public void onHangUp(Object msg) {
        Log.d(TAG, "onHangUp!");
    }

    @Override
    public void onLeaveRoom(Object msg) {
        Log.d(TAG, "onLeaveRoom!");
    }

    @Override
    public void heartPackage(Object msg) {
        Log.d(TAG, "服务心跳包!");
    }

}
