package com.example.tetris.model;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.tetris.Config;

/**
 * Created by Administrator on 2018/3/11.
 */

public class Score {
    // 当前的分数
    public static int score;
    // 最高记录
    public static int record;
    // 当前游戏难度等级
    public static int level;

    // 保存缓存
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;


    public Score(Context context) {
        initScore();
        // 从缓存中提取
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        record = pref.getInt("record", 0);
    }

    /**
     * 初始化Score
     */
    public void initScore(){
        score = 0;
        level = 1;
    }

    /**
     * 根据消行数加分
     * @param lines 消除的行数
     */
    public void addScore(int lines) {
        score += (lines*lines)*1000;
        // 加完分还要判断是否要刷新level
        updateLevel();
    }

    /**
     * 判断刷新纪录，保存
     */
    public void updateRecord() {
        if (score > record) {
            record = score;
            // 保存到缓存中
            editor = pref.edit();
            editor.putInt("record", record);
            editor.apply();
        }
    }

    /**
     * 判断刷新游戏难度等级
     */
    public void updateLevel() {
        if (score >= 5000 && score < 10000) {
            level = 2;
        } else if (score >= 10000 && score < 15000) {
            level = 3;
        } else if (score >= 15000 && score < 20000) {
            level = 4;
        } else if (score >= 20000 && score < 25000) {
            level = 5;
        } else if (score >= 25000 && score < 30000) {
            level = 6;
        } else if (score >= 30000 && score < 38000) {
            level = 7;
        } else if (score >= 38000 && score < 60000) {
            level = 8;
        } else if (score >= 60000 && score < 84000) {
            level = 9;
        } else if (score >= 84000 && score < 100000) {
            level = 10;
        } else if (score >= 100000) {
            score = 100000;
        }
        Config.sleepTime = 1020 - (level-1)*80;
    }
}