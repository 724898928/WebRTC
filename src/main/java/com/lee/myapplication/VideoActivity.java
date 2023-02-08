package com.lee.myapplication;

import static com.lee.util.MsgContant.CHANNEL;
import static com.lee.util.MsgContant.JOIN_ROOM;
import static com.lee.util.MsgContant.VOLUME;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.lee.model.Message;
import com.lee.model.MsgData;
import com.lee.model.Room;
import com.lee.model.User;
import com.lee.util.MsgContant;
import com.lee.util.RoomsContant;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class VideoActivity extends AppCompatActivity {

    private final static String TAG = VideoActivity.class.getSimpleName();
    @NonNull
    private SurfaceViewRenderer localSurfaceView, remoteSurfaceView;
    private int videoHeight;
    private int videoWidth;
    private String room_url;
    private String room_id;
    private String user_name;
    private EglBase eglBase;
    private WebSocketClient webSocketClient;
    private PeerConnectionFactory peerConnectionFactory;
    private ArrayList<PeerConnection.IceServer> iceServers;
    private PeerConnection peerConnection;
    private DataChannel channel;
    public static final int VIDEO_RESOLUTION_WIDTH = 320;
    public static final int VIDEO_RESOLUTION_HEIGHT = 240;
    public static final int VIDEO_FPS = 30;
    public static final String VIDEO_TRACK_ID = "videtrack";
    public static final String AUDIO_TRACK_ID = "audiotrack";
    public static final String LOCAL_VIDEO_STREAM = "localVideoStream";
    public static final String LOCAL_AUDIO_STREAM = "localAudioStream";
    private VideoTrack videoTrack;
    private List<String> streamList;
    private AudioTrack audioTrack;
    private AudioSource audioSource;
    private MySdpObserver observer;
    private String toUser;
    private String userId;
    private MsgData msgData;

    private static class MyHandler extends Handler {
        private final WeakReference<Activity> mActivityReference;

        private MyHandler(Activity activity) {
            this.mActivityReference = new WeakReference<Activity>(activity);

        }

        @Override
        public void handleMessage(@NonNull android.os.Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "MyHandler handleMessage msg=" + msg);
            VideoActivity activity = (VideoActivity) mActivityReference.get();
            switch (msg.what) {  // 获取消息,
                case 1:
                    if (null != activity) {
                        activity.createPeerConnection();
                    }
                    break;
                case 2:
                    Looper.prepare();
                    Toast.makeText(activity, "要连接的用户没有上线!", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                    break;

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_video);
        localSurfaceView = findViewById(R.id.localSV);
        remoteSurfaceView = findViewById(R.id.remoterSV);
        getIntentData();
        DisplayMetrics displayMetrics = getDisplayMetrics();
        videoWidth = displayMetrics.widthPixels;
        videoHeight = displayMetrics.heightPixels;
        // 创建EglBase对象
        eglBase = EglBase.create();
        initView();
        startCall();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        room_id = String.valueOf(extras.get(RoomsContant.ROOM_ID));
        room_url = String.valueOf(extras.get(RoomsContant.ROOM_URL));
        user_name = String.valueOf(extras.get(RoomsContant.USER_NAME));
        toUser = String.valueOf(extras.get(RoomsContant.TO_USER));
        userId = String.valueOf(extras.get(RoomsContant.USER_ID));
    }

    @TargetApi(17)
    private DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics;
    }

    private void startCall() {
        try {
            msgData = new MsgData(new User(userId,user_name), new User(toUser,null),null, room_id,null);
            Message message = new Message(JOIN_ROOM, msgData);
            MyHandler myHandler = new MyHandler(this);
            webSocketClient = new WebSocketClient(this, URI.create(room_url), message, myHandler);
            webSocketClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Socket Exception:" + e.getMessage());
            Toast.makeText(this, "服务器连接异常!", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 连接webrtc
     */
    private void createPeerConnection() {
        Log.d(TAG, "createPeerConnection: ");
        PeerConnectionFactory.InitializationOptions initializationOptions = PeerConnectionFactory.InitializationOptions.builder(this)
                .setEnableInternalTracer(true)
                .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
                .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        options.disableEncryption = true;
        options.disableNetworkMonitor = true;
        Log.d(TAG, "createPeerConnection:  options.disableNetworkMonito");
        peerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(eglBase.getEglBaseContext()))
                .setVideoEncoderFactory(new DefaultVideoEncoderFactory(eglBase.getEglBaseContext(), true, true))
                .setOptions(options)
                .createPeerConnectionFactory();
        // 配置stun穿透服务器 进行媒体协商
        iceServers = new ArrayList<>();
        String stun = getResources().getString(R.string.webRTC_stun);
        PeerConnection.IceServer iceServer = PeerConnection.IceServer.builder(stun).createIceServer();
        iceServers.add(iceServer);
        Log.d(TAG, "createPeerConnection:  iceServers.add");
        streamList = new ArrayList<>();
        PeerConnection.RTCConfiguration configuration = new PeerConnection.RTCConfiguration(iceServers);
        PeerConnectionObserver connectionObserver = getObserver();
        peerConnection = peerConnectionFactory.createPeerConnection(configuration, connectionObserver);

       /*
        DataChannel.Init 可配参数说明：
        ordered：是否保证顺序传输；
        maxRetransmitTimeMs：重传允许的最长时间；
        maxRetransmits：重传允许的最大次数；
        */
        DataChannel.Init init = new DataChannel.Init();
        if (null != peerConnection) {
            channel = peerConnection.createDataChannel(CHANNEL, init);
        }
        DateChannelObserver channelObserver = new DateChannelObserver();
        connectionObserver.setObserver(channelObserver);

        webSocketClient.setPeerConnection(peerConnection);
        startLocalVideoCapture(localSurfaceView);
        startLocalAudioCapture();
    }

    @NonNull
    private PeerConnectionObserver getObserver() {
        Log.d(TAG, "getObserver : ");
        return new PeerConnectionObserver() {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                Log.d(TAG, "PeerConnectionObserver onIceCandidate : " + iceCandidate.toString());
                super.onIceCandidate(iceCandidate);
                setIceCandidate(iceCandidate);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                Log.d(TAG, "onAddStream : " + mediaStream.toString());
                List<VideoTrack> videoTracks = mediaStream.videoTracks;
                if (videoTracks != null && videoTracks.size() > 0) {
                    VideoTrack videoTrack = videoTracks.get(0);
                    if (videoTrack != null) {
                        Log.d(TAG, "onAddStream  videoTrack= "+videoTrack);
                        videoTrack.addSink(remoteSurfaceView);
                    }else {
                        Log.d(TAG, "onAddStream  videoTrack= null");
                    }
                }
                List<AudioTrack> audioTracks = mediaStream.audioTracks;
                if (audioTracks != null && audioTracks.size() > 0) {
                    AudioTrack audioTrack = audioTracks.get(0);
                    if (audioTrack != null) {
                        audioTrack.setVolume(VOLUME);
                    }
                }
            }
        };
    }

    /**
     * 呼叫
     *
     * @param iceCandidate
     */
    private void setIceCandidate(IceCandidate iceCandidate) {
        if (null!= msgData){
            msgData.setIceCandidate(iceCandidate);
            Message msg = new Message(MsgContant.CANDIDATE, msgData);
            if (null != webSocketClient) {
                Log.d(TAG, "onIceCandidate iceCandidate msg-> " + msg);
                webSocketClient.send(msg.toString());
            } else {
                Log.d(TAG, "onIceCandidate webSocketClient = null");
            }
        }
    }

    private void initView() {
        initSurfaceView(localSurfaceView);
        initSurfaceView(remoteSurfaceView);
    }

    private void startLocalVideoCapture(SurfaceViewRenderer localSurfaceView) {
        VideoSource videoSource = peerConnectionFactory.createVideoSource(true);
        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create(
                Thread.currentThread().getName(), eglBase.getEglBaseContext());
        VideoCapturer videoCapturer = createVideoCapturer();
        videoCapturer.initialize(surfaceTextureHelper, this, videoSource.getCapturerObserver());
        videoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, VIDEO_FPS);
        videoTrack = peerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        videoTrack.addSink(localSurfaceView);
        MediaStream localMediaStream = peerConnectionFactory.createLocalMediaStream(LOCAL_VIDEO_STREAM);
        localMediaStream.addTrack(videoTrack);
        peerConnection.addTrack(videoTrack, streamList);
        peerConnection.addStream(localMediaStream);

    }

    private VideoCapturer createVideoCapturer() {
        if (Camera2Enumerator.isSupported(this)) {
            return createCameraCapturer(new Camera2Enumerator(this));
        } else {
            return createCameraCapturer(new Camera1Enumerator(true));
        }
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        // First, try to find front facing camera
        Log.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Log.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Log.d(TAG, "Looking for other cameras.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (null != videoCapturer) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }


    /**
     * 创建本地音频
     */
    private void startLocalAudioCapture() {
        // 语音
        MediaConstraints audioConstranints = new MediaConstraints();
        // 回声消除
        audioConstranints.mandatory.add(new MediaConstraints.KeyValuePair("googEchoCancellation", "true"));
        // 自动增益
        audioConstranints.mandatory.add(new MediaConstraints.KeyValuePair("googAutoGainControl", "true"));
        // 高音过滤
        audioConstranints.mandatory.add(new MediaConstraints.KeyValuePair("googHighpassFilter", "true"));
        // 噪音处理
        audioConstranints.mandatory.add(new MediaConstraints.KeyValuePair("googNoiseSuppression", "true"));
        audioSource = peerConnectionFactory.createAudioSource(audioConstranints);
        audioTrack = peerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        MediaStream localMediaStream = peerConnectionFactory.createLocalMediaStream(LOCAL_AUDIO_STREAM);
        localMediaStream.addTrack(audioTrack);
        audioTrack.setVolume(VOLUME);
        peerConnection.addTrack(audioTrack, streamList);
        peerConnection.addStream(localMediaStream);

    }

    private void initSurfaceView(SurfaceViewRenderer localSurfaceView) {
        localSurfaceView.init(eglBase.getEglBaseContext(), null);
        localSurfaceView.setMirror(true);
        localSurfaceView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        localSurfaceView.setKeepScreenOn(true);
        localSurfaceView.setZOrderMediaOverlay(true);
        localSurfaceView.setEnableHardwareScaler(false);
    }

    private void close() {
        if (peerConnection != null) {
            peerConnection.close();
        }
        if (webSocketClient != null) {
            webSocketClient.close();
        }
        if (localSurfaceView != null) {
            localSurfaceView.release();
        }
        if (remoteSurfaceView != null) {
            remoteSurfaceView.release();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        close();
    }

    @Override
    protected void onPause() {
        super.onPause();
        close();
    }
}