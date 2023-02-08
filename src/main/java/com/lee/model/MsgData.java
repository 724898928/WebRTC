package com.lee.model;

import com.google.gson.Gson;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

public class MsgData {
    private String from;      // 本地用户Id
    private String fromName;  // 本地用户名
    private String to;        // 连接的userId
    private String toName;    // 连接的userName
    private SessionDescription sessionDescription;    // SDP
    private String roomId;     // 连接的房号
    private IceCandidate iceCandidate;  // ICECandidate
    //会话Id
    private String sessionId = "000-111";

    public MsgData(User user,User toUser, SessionDescription sessionDescription, String roomId, IceCandidate iceCandidate) {
        if (null != user){
            this.from = user.getUserId();
            this.fromName = user.getName();
        }
        if (null != toUser){
            this.to = toUser.getUserId();
            this.toName = toUser.getName();
        }
        this.sessionDescription = sessionDescription;
        this.roomId = roomId;
        this.iceCandidate = iceCandidate;
    }

    public IceCandidate getIceCandidate() {
        return iceCandidate;
    }

    public void setIceCandidate(IceCandidate iceCandidate) {
        this.iceCandidate = iceCandidate;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public SessionDescription getSessionDescription() {
        return sessionDescription;
    }

    public void setSessionDescription(SessionDescription sessionDescription) {
        this.sessionDescription = sessionDescription;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
