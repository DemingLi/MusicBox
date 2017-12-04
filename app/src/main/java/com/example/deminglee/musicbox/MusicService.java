package com.example.deminglee.musicbox;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.Nullable;

/**
 * Created by Deming Lee on 2017/11/28.
 */
public class MusicService extends Service {
  public MediaPlayer mediaPlayer = new MediaPlayer();
  public final IBinder binder = new MyBinder();
  
  public MusicService() {
    try {
      mediaPlayer.setDataSource("data/melt.mp3");
      mediaPlayer.prepare();
      mediaPlayer.setLooping(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public class MyBinder extends Binder {
    @Override
    protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
      switch (code) {
        case 101://播放/暂停
          if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
          } else mediaPlayer.start();
          break;
        case 102://停止
          if (mediaPlayer != null) {
            mediaPlayer.stop();
            try {
              mediaPlayer.setDataSource("data/melt.mp3");
              mediaPlayer.prepare();
              mediaPlayer.setLooping(true);
              mediaPlayer.start();
              mediaPlayer.pause();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
          break;
        case 103://获取文件长度
          if (mediaPlayer != null) reply.writeInt(mediaPlayer.getDuration());
          break;
        case 104://获取当前播放时间
          if (mediaPlayer != null) reply.writeInt(mediaPlayer.getCurrentPosition());
          break;
        case 105://拖动进度条
          if (mediaPlayer != null) {
            mediaPlayer.seekTo(data.readInt());
          }
          break;
      }
      return super.onTransact(code, data, reply, flags);
    }
  }
  
  
  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }
  
  @Override
  public void onCreate() {
    super.onCreate();
  }
  
  @Override
  public void onDestroy() {
    super.onDestroy();
  }
}
