package com.example.deminglee.musicbox;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {
  private ObjectAnimator objectAnimator; //旋转动画
  private ImageView img;
  private Button play, stop, quit;
  private TextView state, currenttime, endtime;
  private SeekBar seekBar;
  private ServiceConnection sc;
  private IBinder mBinder;
  private SimpleDateFormat time = new SimpleDateFormat("mm:ss");
  private boolean hasPermission = false;
  
  private static final int REQUEST_EXTERNAL_STORAGE = 1;
  private static String[] PERMISSIONS_STORAGE = {
          Manifest.permission.READ_EXTERNAL_STORAGE,
          Manifest.permission.WRITE_EXTERNAL_STORAGE
  };
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    verifyStoragePermissions(MainActivity.this);
    sc = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName componentName, IBinder service) {
        mBinder = service;
      }
  
      @Override
      public void onServiceDisconnected(ComponentName componentName) {
        sc = null;
      }
    };
    findView();
    bindButton();
    connection();
    setAnimator();
    mThread.start();
  }
  
  private Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
        case 123:
          endtime.setText(time.format(GetDuration_Position(103)));
          currenttime.setText(time.format(GetDuration_Position(104)));
          seekBar.setMax(GetDuration_Position(103));
          seekBar.setProgress(GetDuration_Position(104));
          seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
              if (fromUser) {
                Seek(seekBar.getProgress());
              }
            }
  
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
    
            }
  
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
    
            }
          });
          break;
      }
    }
  };
  
  Thread mThread = new Thread() {
    @Override
    public void run() {
      while (true) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        if (sc != null && hasPermission) {
          mHandler.obtainMessage(123).sendToTarget();
        }
      }
    }
  };
  
  private boolean flag = false;//当前是否正在播放
  public class MyListener implements View.OnClickListener {
    @Override
    public void onClick(View view) {
      switch (view.getId()) {
        case R.id.play:
          Play_Stop(101);
          if (flag) {//当前正在播放
            objectAnimator.pause();
            state.setText("Paused");
            play.setText("PLAY");
            flag = false;
          } else {
            objectAnimator.resume();
            state.setText("Playing");
            play.setText("PAUSE");
            flag = true;
          }
          break;
        
        case R.id.stop:
          Play_Stop(102);
          state.setText("Stopped");
          play.setText("PLAY");
          objectAnimator.start();
          objectAnimator.pause();
          flag = false;
          break;
        
        case R.id.quit:
          mHandler.removeCallbacks(mThread);
          unbindService(sc);
          sc = null;
          try {
            MainActivity.this.finish();
            System.exit(0);
          } catch (Exception e) {
            e.printStackTrace();
          }
          break;
      }
    }
  }
  
  private void Play_Stop(int code) { //101:PLAY 102:STOP
    Parcel data = Parcel.obtain();
    Parcel reply = Parcel.obtain();
    try {
      mBinder.transact(code, data, reply, 0);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }
  
  private int GetDuration_Position(int code) { // 103:时长  104:当前时间
    Parcel data = Parcel.obtain();
    Parcel reply = Parcel.obtain();
    try {
      mBinder.transact(code, data, reply, 0);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    return reply.readInt();
  }
  
  private void Seek(int position) { //设置播放器的播放位置
    int code = 105;
    Parcel data = Parcel.obtain();
    data.writeInt(position);
    Parcel reply = Parcel.obtain();
    try {
      mBinder.transact(code, data, reply, 0);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }
  
  public void findView() {
    img = (ImageView) findViewById(R.id.img);
    play = (Button) findViewById(R.id.play);
    stop = (Button) findViewById(R.id.stop);
    quit = (Button) findViewById(R.id.quit);
    state = (TextView) findViewById(R.id.state);
    currenttime = (TextView) findViewById(R.id.currenttime);
    endtime = (TextView) findViewById(R.id.endtime);
    seekBar = (SeekBar) findViewById(R.id.seekBar);
    seekBar.setEnabled(true);
  }
  
  private void connection() {//Activity启动时绑定Service
    Intent intent = new Intent(this,MusicService.class);
    startService(intent);
    bindService(intent, sc, Context.BIND_AUTO_CREATE);
  }
  
  private void bindButton() {
    play.setOnClickListener(new MyListener());
    stop.setOnClickListener(new MyListener());
    quit.setOnClickListener(new MyListener());
  }
  
  public void setAnimator() {//设置旋转动画
    objectAnimator = ObjectAnimator.ofFloat(img, "rotation", 0, 360);
    objectAnimator.setDuration(10000);//动画时长
    objectAnimator.setInterpolator(new LinearInterpolator());
    objectAnimator.setRepeatCount(ObjectAnimator.INFINITE);
    objectAnimator.start();
    objectAnimator.pause();
  }
  
  public void verifyStoragePermissions(Activity activity) {
    try {
      int permission = ActivityCompat.checkSelfPermission(activity, "READ_EXTERNAL_STORAGE");
      if (permission != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
      } else {
        hasPermission = true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      hasPermission = true;
    } else {
      Toast.makeText(this, "请允许申请权限之后重新启动应用！", Toast.LENGTH_SHORT).show();
    }
  }
  
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      moveTaskToBack(false);
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }
  
  /**
   * Take care of popping the fragment back stack or finishing the activity
   * as appropriate.
   */
  @Override
  public void onBackPressed() {
    moveTaskToBack(true);
    super.onBackPressed();
  }
}
