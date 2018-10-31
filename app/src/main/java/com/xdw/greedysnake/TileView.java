package com.xdw.greedysnake;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class TileView extends View {
    public static int mTileSize =32;
    public static int mXTileCount;  //地图上所能容纳的格数
    public static int mYTileCount;
    public static int mXOffset;     //偏移量
    public static int mYOffset;
    Bitmap[] mTileArray;            //放置图片的数组
    int[][] mTileGrid;              //存放各坐标对应的图片

    public TileView(Context context, AttributeSet attrs,int defStyle){
        super(context,attrs,defStyle);
    }
    public TileView(Context context, AttributeSet attrs){
        super(context,attrs);
    }
    public TileView(Context context){
        super(context);
    }
    //加载三幅小图片
    public void loadTile(int key, Drawable tile) {
        Bitmap bitmap = Bitmap.createBitmap(mTileSize, mTileSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        tile.setBounds(0, 0, mTileSize, mTileSize);
        tile.draw(canvas);
        mTileArray[key] = bitmap;
    }
    //给地图数组赋值
    public void setTile(int tileindex, int x, int y) {
        mTileGrid[x][y] = tileindex;
    }
    public void resetTiles(int tilecount) {
        mTileArray = new Bitmap[tilecount];
    }
   //我的游戏界面不是占满整个屏幕，所以地图遍历的时候，y不是从0开始
    public void clearTiles() {
        for (int x = 0; x < mXTileCount; x++) {
            for (int y = 2; y < mYTileCount-8; y++) {
                setTile(0, x, y);
            }
        }
    }
   @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh){
        //地图数组初始化
        mXTileCount = (int) Math.floor(w / mTileSize);
        mYTileCount = (int) Math.floor(h / mTileSize);
//        System.out.println("-------"+mXTileCount+"----------");
//        System.out.println("-------"+mYTileCount+"----------");
        //可能屏幕的长宽不能整除，所以够分成一格的分成一格, 剩下不够一格的分成两份,左边一份,右边一份
        mXOffset = ((w - (mTileSize * mXTileCount)) / 2);
        mYOffset = ((h - (mTileSize * mYTileCount)) / 2);
//        System.out.println("-------"+mXOffset+"----------");
//        System.out.println("-------"+mYOffset+"----------");
        mTileGrid = new int[mXTileCount][mYTileCount];
        clearTiles();
    }
    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
    }
}