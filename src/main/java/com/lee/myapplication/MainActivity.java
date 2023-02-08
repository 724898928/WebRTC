package com.lee.myapplication;

import static com.lee.util.MsgContant.JOIN_ROOM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.blankj.utilcode.util.StringUtils;
import com.lee.model.Message;
import com.lee.model.Room;
import com.lee.model.User;
import com.lee.util.RoomsContant;

import org.webrtc.SurfaceViewRenderer;

import java.net.URI;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();
    private EditText user_name_text, room_id_text,toUser,userId;
    private Button call_option;
    private String mSocketAddress ;
    private ListView usersView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());
        setContentView(R.layout.call_view);
        // 隐藏 actionBar
        ActionBar ab = getSupportActionBar();
        ab.hide();

    String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET};
    if (!EasyPermissions.hasPermissions(this, perms)){
        EasyPermissions.requestPermissions(this,"Need permissions for net and camera & microphone", 0, perms);
    }
        mSocketAddress = getResources().getString(R.string.webRTC_host);
        user_name_text = findViewById(R.id.user_name);
        userId = findViewById(R.id.userId);
        toUser = findViewById(R.id.toUser);
        room_id_text = findViewById(R.id.room_id);
        call_option = findViewById(R.id.call);
        usersView = findViewById(R.id.lv);
        usersView.setAdapter(new MyAdapter(this));
        call_option.setOnClickListener(v -> toVideoActivity());


    }

    public void toVideoActivity() {
        Intent intent = new Intent(MainActivity.this, VideoActivity.class);
        String room_id = room_id_text.getText().toString();
        if (StringUtils.isSpace(room_id)) {
            Toast.makeText(MainActivity.this, "请输入room id!", Toast.LENGTH_SHORT).show();
            return;
        }
        intent.putExtra(RoomsContant.ROOM_URL, mSocketAddress);
        intent.putExtra(RoomsContant.ROOM_ID, room_id);
        intent.putExtra(RoomsContant.USER_NAME, user_name_text.getText().toString());
        intent.putExtra(RoomsContant.USER_ID, userId.getText().toString());
        intent.putExtra(RoomsContant.TO_USER, toUser.getText().toString());
        //intent.
        startActivity(intent);
    }
    @TargetApi(19)
    private static int getSystemUiVisibility() {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        return flags;
    }

    public void requestPermission(){

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults,this);
    }

}