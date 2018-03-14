package com.example.tetris.model;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.SparseIntArray;

import com.example.tetris.Config;
import com.example.tetris.R;

/**
 * Created by Administrator on 2018/3/12.
 */

public class Music {
    // 音效播放组件
    private AudioManager mAudioManager;
    private SoundPool mSoundPool;
    private SparseIntArray mSoundMap;
    public static MediaPlayer mPlayer;
    public Context mContext;

    public Music(Context context) {
        this.mContext = context;
        // 得到AudioManager系统服务
        mAudioManager = (AudioManager) mContext.getSystemService(mContext.AUDIO_SERVICE);
        // 设置声音池，通过标准的扬声器输出每次只播放一个音频
        mSoundPool = new SoundPool(
                1,              // maxStreams参数，该参数为设置同时能够播放多少音效
                AudioManager.STREAM_MUSIC,  // streamType参数，该参数设置音频类型，在游戏中通常设置为：STREAM_MUSIC
                0               // srcQuality参数，该参数设置音频文件的质量，目前还没有效果，设置为0为默认值。
        );
        // 负责装载音频和序列
        mSoundMap = new SparseIntArray();
        int streamId;
        // 开始装载
        streamId = mSoundPool.load(mContext, R.raw.btn, 1);
        mSoundMap.put(R.id.btn_Left, streamId);
        mSoundMap.put(R.id.btn_Right, streamId);
        mSoundMap.put(R.id.btn_Down, streamId);
        streamId = mSoundPool.load(mContext, R.raw.transform, 1);
        mSoundMap.put(R.id.btn_Top, streamId);
        streamId = mSoundPool.load(mContext, R.raw.fixup, 1);
        mSoundMap.put(R.id.btn_QuickDown, streamId);
        streamId = mSoundPool.load(mContext, R.raw.qitabtn, 1);
        mSoundMap.put(R.id.btn_Start, streamId);
        mSoundMap.put(R.id.btn_Pause, streamId);
        mSoundMap.put(R.id.btn_Music, streamId);
        mSoundMap.put(R.id.btn_Chang, streamId);
        mSoundMap.put(R.id.btn_Line, streamId);
        mSoundMap.put(R.id.btn_Exit, streamId);
        streamId = mSoundPool.load(mContext, R.raw.readygo, 1);
        mSoundMap.put(1, streamId);
        streamId = mSoundPool.load(mContext, R.raw.remove, 1);
        mSoundMap.put(2, streamId);
        streamId = mSoundPool.load(mContext, R.raw.win, 1);
        mSoundMap.put(3, streamId);
        streamId = mSoundPool.load(mContext, R.raw.lost, 1);
        mSoundMap.put(4, streamId);
    }

    /**
     * 播放背景音乐
     */
    public void playBGM() {
        // 防止BGM重叠，开始播放前先检查结束
        stopBGM();
        // 返回当前AudioManager对象的音量值
        float streamVolumeCurrent = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        // 返回当前AudioManager对象的最大音量值
        float streamVolumeMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = streamVolumeCurrent / streamVolumeMax;
        try {
            // 背景音乐
            mPlayer = MediaPlayer.create(mContext, R.raw.back1);
            mPlayer.setVolume(volume, volume);
            // 开始播放
            if (Config.isMusic) {
                // 不加这个判断，当关音效进游戏的时候就会播放，这不对
                mPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 监听音频播放完的代码，实现音频的自动循环播放
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer arg0) {
                playBGM();
            }
        });
    }

    /**
     * 停止BGM
     */
    public void stopBGM(){
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    /**
     * 播放音效
     * @param id 对应map里的索引
     */
    public void playSound(int id){
        if (!Config.isMusic) return;
        int streamId = mSoundMap.get(id);
        if (streamId > 0) {
            // 返回当前AudioManager对象的音量值
            float streamVolumeCurrent = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            // 返回当前AudioManager对象的最大音量值
            float streamVolumeMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            float volume = streamVolumeCurrent / streamVolumeMax;
            mSoundPool.play(
                    streamId,   // 播放的音乐id
                    volume,     // 左声道音量
                    volume,     // 右声道音量
                    ((id == 1 || id == 3 || id == 4) ? 2 : 1),  // 优先级，0为最低
                    0,    // 循环次数，0为不循环，-1为永远循环
                    1.0f   // 回放速度 ，该值在0.5-2.0之间，1为正常速度
                    );
        }
    }
}