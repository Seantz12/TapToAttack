package com.example.seantz12.taptoattack;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Created by seantz12 on 01/08/17.
 */

public class GameView extends SurfaceView implements Runnable {

    ArrayList<Background> backgrounds;
    ArrayList<Enemy> enemies;

    // Declaring a whole bunch of variables(?) that will be used
    private Thread gameThread = null;
    private SurfaceHolder ourHolder;
    private volatile boolean play;

    private Canvas canvas;
    private Paint paint;

    long fps;
    long timeAtFrame;

    long startSwingTime;
    long totalSwingTime;

    Bitmap hero;
    boolean swung = false;
    boolean charge = false;

    // Numerical values storing well, what they are explicitly storing
    float heroX = 10;
    float heroY = 550;

    int screenWidth;
    int screenHeight;

    // Used to point to activity
    private Context context;



    public GameView(Context context, int screenWidth, int screenHeight) {
        super(context);

        this.context = context;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        ourHolder = getHolder();
        paint = new Paint();

        // Loads the backgrounds
        backgrounds = new ArrayList<>();
        backgrounds.add(new Background(
                this.context,
                screenWidth,
                screenHeight,
                "sky_clouds",
                0, 80, 50));

        backgrounds.add(new Background(
                this.context,
                screenWidth,
                screenHeight,
                "grass_foreground",
                70, 110, 200));


        /*
         * Ok writing this down so i dont forget
         * In level select screen, once starting a level
         * pass an integer value like 24 35 01 (without spaces)
         * First two digits is how many melee
         * Second two are ranged. last one is bosses (or something similar i dunno)
         * Then, it will record those two numbers down, and in a for loop will randomize the order of the enemies
         */
        enemies = new ArrayList<>();
        for(int i = 0; i < 40; i++) {
            int distance = ((int)(Math.random()*5)+1)*100;
            enemies.add(new Enemy(
                    this.context,
                    "hero",
                    1,
                    1,
                    screenWidth + (i+1)*100 + distance,
                    550
            ));
        }

        hero = BitmapFactory.decodeResource(this.getResources(), R.drawable.hero);
    }

    // Da loop
    public void run() {
        while(play) {
            // This will be used to calculate fps
            long startFrameTime = System.currentTimeMillis();

            update();
            draw();

            // This part calculates fps
            timeAtFrame = System.currentTimeMillis() - startFrameTime;
            if(timeAtFrame > 0) {
                fps = 1000 / timeAtFrame;
            }
        }
    }

    public void update() {
        // Updates background positions
        for(Background bg : backgrounds) {
            bg.update(fps);
        }

        for(Enemy e : enemies) {
            // Todo, collision checking with other enemies
            if(e.xPos > heroX + 50 && !collisionCheck(e)) {
                e.xPos = e.xPos - (e.speed);
            } else if(e.xPos <= heroX + 30){
                System.out.println("you got hit bitch");
            }
        }

        if(swung) {
            totalSwingTime = System.currentTimeMillis() - startSwingTime;
            int damage = (int)totalSwingTime / 1000 + 1;
            checkAndSwing(damage);
            swung = false;
        }
    }

    public void draw() {
        // Double check to make sure there is a surface value
        if(ourHolder.getSurface().isValid()) {
            // Locks the canvas
            // Make drawing surface the canvas
            canvas = ourHolder.lockCanvas();

            // Starts drawing stuff
            canvas.drawColor(Color.argb(255, 25, 230, 180));
            paint.setColor(Color.argb(255, 0, 0, 0));
            paint.setTextSize(48);

            // draws them
            drawBackground(0);
            drawBackground(1);

            canvas.drawText("FPS: " + fps, 20, 40, paint);
            canvas.drawBitmap(hero, heroX, heroY, paint);

            for(Enemy e: enemies) {
                canvas.drawBitmap(e.enemy, e.xPos, e.yPos, paint);
            }

            // Put everything on, unlock surface
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    // Method used a lot so... it's its own method
    private void drawBackground(int position) {
        // Takes a copy of the background currently in use
        Background bg = backgrounds.get(position);

        // Getting coords of where to draw images, reg edition
        Rect startRectReg = new Rect(0, 0, bg.width - bg.xClip, bg.height);
        Rect endRectReg = new Rect(bg.xClip, bg.startY, bg.width, bg.endY);

        // For flipped edition
        Rect startRectFlip = new Rect(bg.width - bg.xClip, 0, bg.width, bg.height);
        Rect endRectFlip = new Rect(0, bg.startY, bg.xClip, bg.endY);

        if(!bg.flipped) {
            canvas.drawBitmap(bg.bgImage, startRectReg, endRectReg, paint);
            canvas.drawBitmap(bg.bgImageFlip, startRectFlip, endRectFlip, paint);
        } else {
            canvas.drawBitmap(bg.bgImage, startRectFlip, endRectFlip, paint);
            canvas.drawBitmap(bg.bgImageFlip, startRectReg, endRectReg, paint);
        }
    }

    // Takes damage from charged swing and applies it to the enemy in front, if it kills them,
    // it also removes them from the list
    private void checkAndSwing(int damage) {
        Enemy dead = null;
        for(Enemy e : enemies) {
            if(e.xPos <= heroX + 80) {
                if(e.hp - damage <= 0) {
                    dead = e;
                    break;
                } else {
                    e.hp -= damage;
                    // Need to make this smoother, add a boolean for direction and flip it?
                    e.xPos += 100;
                }
            }
        }
        enemies.remove(dead);
    }

    // Checks to make sure that there's no one in front of the enemy
    private boolean collisionCheck(Enemy e) {
        int index = enemies.indexOf(e);
        if(index != 0 && e.xPos <= enemies.get(index-1).xPos + 35 ) {
            return true;
        } else {
            return false;
        }
    }

    // shutdown thread if activity stops
    public void pause() {
        play = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }
    }

    // take a guess what this one does
    // hint, it may resume the thread
    public void resume() {
        play = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            // If player touched screen
            // Need to implement length of swinging
            case MotionEvent.ACTION_DOWN:
                if(!swung && !charge) {
                    startSwingTime = System.currentTimeMillis();
                    charge = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                swung = true;
                charge = false;
                break;
        }
        return true;
    }
}