package com.xdw.greedysnake;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class GameActivity extends Activity implements View.OnClickListener {
    private SharedPreferences saved;
    private static String ICICLE_KEY = "snake-view";    //  个人认为这个变量就是一个中间值，在该类的最后一个方法中传入该变量，完成操作。
    private SnakeView mSnakeView;
    private ImageButton change_stop,change_start,change_quit;
    private ImageButton mLeft;
    private ImageButton mRight;
    private ImageButton mUp;
    private ImageButton mDown;
    private static final int UP = 1;
    private static final int DOWN = 2;
    private static final int RIGHT = 3;
    private static final int LEFT = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        mSnakeView=findViewById(R.id.snake);  //给自定义View实例化，把这个布局当一个UI控件一样插入进来
        mSnakeView.setTextView((TextView) findViewById(R.id.text_show));

        change_stop=findViewById(R.id.game_stop);
        change_start=findViewById(R.id.game_start);
        change_quit=findViewById(R.id.game_quit);

        mLeft=findViewById(R.id.left);
        mRight=findViewById(R.id.right);
        mUp=findViewById(R.id.up);
        mDown=findViewById(R.id.down);

        change_start=findViewById(R.id.game_start);
        change_stop=findViewById(R.id.game_stop);
        change_quit=findViewById(R.id.game_quit);

        saved = PreferenceManager.getDefaultSharedPreferences(this);
        boolean playMusic = saved.getBoolean("ifon" ,true);     // 获取背景音乐开关的状态变量，在设置开关界面存储，在这里读取
        if(playMusic) {    // 如果设置背景音乐打开，则开启服务，播放音乐
            Intent intent_service = new Intent(GameActivity.this, MusicService.class);
            startService(intent_service);
        }
        SnakeView.mMoveDelay=saved.getInt("nandu",500);     // 获取当前设置的代表游戏难度的变量，在难度设置界面保存，在这里读取

        // 判断是否有保存数据，如果数据为空就准备重新开始游戏
        if (savedInstanceState == null) {
            mSnakeView.setMode(SnakeView.READY);
        } else {
            // 暂停后的恢复
            Bundle map = savedInstanceState.getBundle(ICICLE_KEY);
            if (map != null) {
                mSnakeView.restoreState(map);
            } else {
                mSnakeView.setMode(SnakeView.PAUSE);
            }
        }
        mDown.setOnClickListener(this);
        mUp.setOnClickListener(this);
        mRight.setOnClickListener(this);
        mLeft.setOnClickListener(this);
        change_start.setOnClickListener(this);
        change_stop.setOnClickListener(this);
        change_quit.setOnClickListener(this);
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        saved = PreferenceManager.getDefaultSharedPreferences(this);
        boolean playMusic = saved.getBoolean("ifon" ,true);
        if(playMusic) {
            Intent intent_service = new Intent(GameActivity.this, MusicService.class);
            stopService(intent_service);
        }

    }
    // 给开始，暂停，退出，上下左右按钮设置监听。根据当前的状态来决定界面的更新操作
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.game_start:
                // 重新开始游戏，这里延时变量必须初始化，不然每次游戏重新开始之后，蛇的运动速度不会初始化
                if ( mSnakeView.mMode == SnakeView.READY || mSnakeView.mMode == SnakeView.LOSE) {
                SnakeView.mMoveDelay=saved.getInt("nandu",500);
                mSnakeView.initNewGame();
                mSnakeView.setMode(SnakeView.RUNNING);
                mSnakeView.update();
            }
                // 暂停后开始游戏，继续暂停前的界面
                if ( mSnakeView.mMode == SnakeView.PAUSE) {
                    mSnakeView.setMode(SnakeView.RUNNING);
                    mSnakeView.update();
                }
                break;
            case R.id.game_stop:    //  暂停
                if(mSnakeView.mMode == SnakeView.RUNNING) {
                    mSnakeView.setMode(SnakeView.PAUSE);
                }
                break;
            case R.id.game_quit:   //  退出，返回菜单界面
                mSnakeView.setMode(SnakeView.QUIT);
                finish();
                break;
            // 使界面上的方向按钮起作用
            case R.id.left:
                if (SnakeView.mDirection != RIGHT) {
                    SnakeView.mNextDirection = LEFT;
                }
                break;
            case R.id.right:
                if (SnakeView.mDirection != LEFT) {
                    SnakeView.mNextDirection = RIGHT;
                }
                break;
            case R.id.up:
                if (SnakeView.mDirection != DOWN) {
                    SnakeView.mNextDirection = UP;
                }
                break;
            case R.id.down:
                if (SnakeView.mDirection != UP) {
                    SnakeView.mNextDirection = DOWN;
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSnakeView.setMode(SnakeView.PAUSE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //保存游戏状态
        outState.putBundle(ICICLE_KEY, mSnakeView.saveState());
    }
}


