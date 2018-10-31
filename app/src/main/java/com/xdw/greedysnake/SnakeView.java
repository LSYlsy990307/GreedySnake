package com.xdw.greedysnake;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class SnakeView extends TileView{

    static int mMoveDelay = 500;
    private long mLastMove;

    private static final int RED_STAR = 1;
    private static final int YELLOW_STAR = 2;
    private static final int GREEN_STAR = 3;

    private static final int UP = 1;
    private static final int DOWN = 2;
    private static final int RIGHT = 3;
    private static final int LEFT = 4;
    static int mDirection = RIGHT;
    static int mNextDirection = RIGHT;
    // 这里把游戏界面分为5种状态，便于逻辑实现
    public static final int PAUSE = 0;
    public static final int READY = 1;
    public static final int RUNNING = 2;
    public static final int LOSE = 3;
    public static final int QUIT = 4;
    public int mMode = READY;
    public int newMode;

    private TextView mStatusText;    // 用于每个状态下的文字提醒
    public long mScore = 0;

    private ArrayList<Coordinate> mSnakeTrail = new ArrayList<Coordinate>(); // 存储蛇的所有坐标的数组
    private ArrayList<Coordinate> mAppleList = new ArrayList<Coordinate>();  // 存储苹果的所有坐标的数组

    private static final Random RNG = new Random();    //用于生成苹果坐标的随机数
//    private static final String TAG = "SnakeView";

    //开启线程，不断调用更新和重绘。这里利用了Handler类来实现异步消息处理机制
    MyHandler handler=new MyHandler();
    class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            SnakeView.this.update();         //不断调用update()方法
            SnakeView.this.invalidate();    //请求重绘，不断调用ondraw()方法
        }
        //调用sleep后,在一段时间后再sendmessage进行UI更新
        public void sleep(int delayMillis) {
            this.removeMessages(0);         //清空消息队列
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    }
    //这是三个构造方法
    public SnakeView(Context context, AttributeSet attrs, int defStyle){
        super(context,attrs,defStyle);
        initNewGame();
    }
    public SnakeView(Context context, AttributeSet attrs){
        super(context,attrs);
        setFocusable(true);
        initNewGame();
    }
    public SnakeView(Context context){
        super(context);
    }
    //添加苹果的方法，最后将生成的苹果坐标存储在上面定义的数组中
    private void addRandomApple() {
        Coordinate newCoord = null;
        boolean found = false;
        while (!found) {
            // 这里设定了苹果坐标能随机生成的范围，并生成随机坐标。
            // mXTileCount和mYTileCount，编译时会报错，因为随机数不能生成负数，而直接使用这两个变量程序不能
            // 识别这个变量减去一个数后是否会是负数，所以我把TileView里输出的确切值放了进去
            int newX = 1 + RNG.nextInt(24-2);
            int newY = 3 + RNG.nextInt(35-12);
            newCoord = new Coordinate(newX, newY);

            boolean collision = false;
            int snakelength = mSnakeTrail.size();
            //遍历snake, 看新添加的apple是否在snake体内, 如果是,重新生成坐标
            for (int index = 0; index < snakelength; index++) {
                if (mSnakeTrail.get(index).equals(newCoord)) {
                    collision = true;
                }
            }
            found = !collision;
        }
//        if (newCoord == null) {
//            Log.e(TAG, "Somehow ended up with a null newCoord!");
//        }
        mAppleList.add(newCoord);
    }

    //绘制边界的墙
    private void updateWalls() {
        for (int x = 0; x < mXTileCount; x++) {
            setTile(GREEN_STAR, x, 2);
            setTile(GREEN_STAR, x, mYTileCount - 11);
        }
        for (int y = 2; y < mYTileCount - 11; y++) {
            setTile(GREEN_STAR, 0, y);
            setTile(GREEN_STAR, mXTileCount - 1, y);
        }
    }
    //更新蛇的运动轨迹
    private void updateSnake(){
        boolean growSnake = false;
        Coordinate head = mSnakeTrail.get(0);
        Coordinate newHead = new Coordinate(1, 1);

        mDirection = mNextDirection;
        switch (mDirection) {
            case RIGHT: {
                newHead = new Coordinate(head.x + 1, head.y);
                break;
            }
            case LEFT: {
                newHead = new Coordinate(head.x - 1, head.y);
                break;
            }
            case UP: {
                newHead = new Coordinate(head.x, head.y - 1);
                break;
            }
            case DOWN: {
                newHead = new Coordinate(head.x, head.y + 1);
                break;
            }
        }
        //检测是否撞墙
        if ((newHead.x < 1) || (newHead.y < 3) || (newHead.x > mXTileCount - 2)
                || (newHead.y > mYTileCount - 11)) {
            setMode(LOSE);
            return;
        }
        //检测蛇头是否撞到自己
        int snakelength = mSnakeTrail.size();
        for (int snakeindex = 0; snakeindex < snakelength; snakeindex++) {
            Coordinate c = mSnakeTrail.get(snakeindex);
            if (c.equals(newHead)) {
                setMode(LOSE);
                return;
            }
        }
        //检测蛇是否吃到苹果
        int applecount = mAppleList.size();
        for (int appleindex = 0; appleindex < applecount; appleindex++) {
            Coordinate c = mAppleList.get(appleindex);
            if (c.equals(newHead)) {
                mAppleList.remove(c);
                addRandomApple();
                mScore++;
                mMoveDelay *= 0.95;    //蛇每迟到一个苹果，延时就会减少，蛇的速度就会加快
                growSnake = true;
            }
        }
        mSnakeTrail.add(0,newHead);
        if(!growSnake) {
            mSnakeTrail.remove(mSnakeTrail.size() - 1);
        }
        //蛇头和蛇身分别设置不同的图片
        int index=0;
        for(Coordinate c:mSnakeTrail) {
            if(index == 0) {
                setTile(RED_STAR, c.x, c.y);
            } else {
                setTile(YELLOW_STAR,c.x,c.y);
            }
            index++;
        }
    }
    //给苹果加载对应的图片
    private void updateApples() {
        for (Coordinate c : mAppleList) {
            setTile(YELLOW_STAR, c.x, c.y);
        }
    }
    // 该方法很重要，用于更新蛇，苹果和墙的坐标
    // 这里设置了更新的时间间隔，我发现不加这个延时的话蛇运动时容易出现一下跳很多格的情况
    public void update(){
        if(mMode == RUNNING) {
            long now = System.currentTimeMillis();
            if (now - mLastMove > mMoveDelay) {
                clearTiles();
                updateWalls();
                updateSnake();
                updateApples();
                mLastMove = now;
            }
            handler.sleep(mMoveDelay);
        }
    }

    //图像初始化，引入图片资源
    private void initSnakeView() {
        setFocusable(true);     //添加焦点
        Resources r = this.getContext().getResources();
        //添加几种不同的tile
        resetTiles(4);
        //从文件中加载图片
        loadTile(RED_STAR, r.getDrawable(R.drawable.redstar));
        loadTile(YELLOW_STAR, r.getDrawable(R.drawable.yellowstar));
        loadTile(GREEN_STAR, r.getDrawable(R.drawable.greenstar));
        update();
    }
    // 数据初始化方法，定义蛇的起始坐标，运动方向和得分变量。给数组添加坐标的时候注意顺序，因为有蛇头和蛇尾的区别
    public void initNewGame() {
        mSnakeTrail.clear();
        mAppleList.clear();
        //snake初始状态时的个数和位置,方向
        mSnakeTrail.add(new Coordinate(8, 7));
        mSnakeTrail.add(new Coordinate(7, 7));
        mSnakeTrail.add(new Coordinate(6, 7));
        mSnakeTrail.add(new Coordinate(5, 7));
        mSnakeTrail.add(new Coordinate(4, 7));
        mSnakeTrail.add(new Coordinate(3, 7));
        mDirection = RIGHT;
        mNextDirection = RIGHT;  // 这个变量必须初始化，不然每次游戏结束重新开始后，蛇初始的方向将不是向右，而是你游戏结束时蛇的方向，
        // 如果死的时候，蛇的方向向左，那么再次点击开始时会无法绘出蛇的图像
        addRandomApple();
        mScore=0;
    }
    // 根据各个数组中的数据，遍历地图设置各点的图片
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        Paint paint=new Paint();
        initSnakeView();
        //遍历地图绘制界面
        for (int x = 0; x < mXTileCount; x++) {
            for (int y = 0; y < mYTileCount; y++) {
                if (mTileGrid[x][y] > 0) {    // 被加了图片的点mTileGird是大于0的
                    canvas.drawBitmap(mTileArray[mTileGrid[x][y]], mXOffset + x * mTileSize, mYOffset + y * mTileSize, paint);
                }
            }
        }
    }

    //把蛇和苹果各点对应的坐标利用一个一维数组储存起来
    private int[] coordArrayListToArray(ArrayList<Coordinate> cvec) {
        int count = cvec.size();
        int[] rawArray = new int[count * 2];
        for (int index = 0; index < count; index++) {
            Coordinate c = cvec.get(index);
            rawArray[2 * index] = c.x;
            rawArray[2 * index + 1] = c.y;
        }
        return rawArray;
    }

    //将当前所有的游戏数据全部保存
    public Bundle saveState() {
        Bundle map = new Bundle();
        map.putIntArray("mAppleList", coordArrayListToArray(mAppleList));
        map.putInt("mDirection", Integer.valueOf(mDirection));
        map.putInt("mNextDirection", Integer.valueOf(mNextDirection));
        map.putInt("mMoveDelay", Integer.valueOf(mMoveDelay));
        map.putLong("mScore", Long.valueOf(mScore));
        map.putIntArray("mSnakeTrail", coordArrayListToArray(mSnakeTrail));
        return map;
    }
    //是coordArrayListToArray()的逆过程，用来读取数组中的坐标数据
    private ArrayList<Coordinate> coordArrayToArrayList(int[] rawArray) {
        ArrayList<Coordinate> coordArrayList = new ArrayList<Coordinate>();
        int coordCount = rawArray.length;
        for (int index = 0; index < coordCount; index += 2) {
            Coordinate c = new Coordinate(rawArray[index], rawArray[index + 1]);
            coordArrayList.add(c);
        }
        return coordArrayList;
    }
    //saveState()的逆过程,用于恢复游戏数据
    public void restoreState(Bundle icicle) {
        setMode(PAUSE);
        mAppleList = coordArrayToArrayList(icicle.getIntArray("mAppleList"));
        mDirection = icicle.getInt("mDirection");
        mNextDirection = icicle.getInt("mNextDirection");
        mMoveDelay = icicle.getInt("mMoveDelay");
        mScore = icicle.getLong("mScore");
        mSnakeTrail = coordArrayToArrayList(icicle.getIntArray("mSnakeTrail"));
    }
    // 设置键盘监听，在模拟器中可以使用电脑键盘控制蛇的方向
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
            if(mDirection != DOWN) {
                mNextDirection = UP;
            }
            return (true);
        }
        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
            if(mDirection != UP) {
                mNextDirection = DOWN;
            }
            return (true);
        }
        if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
            if(mDirection != LEFT) {
                mNextDirection = RIGHT;
            }
            return (true);
        }
        if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
            if(mDirection != RIGHT) {
                mNextDirection = LEFT;
            }
            return (true);
        }
        return super.onKeyDown(keyCode,event);
    }

    public void setTextView(TextView newView) {
        mStatusText = newView;
    }
    // 设置不同状态下提示文字的显示内容和可见状态
    public void setMode(int newMode) {
        this.newMode=newMode;
        int oldMode = mMode;
        mMode = newMode;
        if (newMode == RUNNING & oldMode != RUNNING) {
            mStatusText.setVisibility(View.INVISIBLE);
            update();
            return;
        }
        // 这里定义了一个空字符串，用于放入各个状态下的提醒文字
        Resources res = getContext().getResources();
        CharSequence str = "";
        if (newMode == PAUSE) {
            str = res.getText(R.string.mode_pause);
        }
        if (newMode == READY) {
            str = res.getText(R.string.mode_ready);
        }
        if (newMode == LOSE) {
            str = res.getString(R.string.mode_lose_prefix) + mScore
                    + res.getString(R.string.mode_lose_suffix);
        }
        if (newMode == QUIT){
            str = res.getText(R.string.mode_quit);
        }
        mStatusText.setText(str);
        mStatusText.setVisibility(View.VISIBLE);
    }

    //记录坐标位置
    private class Coordinate {
        public int x;
        public int y;
        public Coordinate(int newX, int newY) {
            x = newX;
            y = newY;
        }
        //触碰检测，看蛇是否吃到苹果
        public boolean equals(Coordinate other) {
            if (x == other.x && y == other.y) {
                return true;
            }
            return false;
        }
        // 这个方法没研究过起什么作用，我注释掉对程序的运行没有影响
        @Override
        public String toString() {
            return "Coordinate: [" + x + "," + y + "]";
        }
    }
}

