package com.example.seantz12.taptoattack;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

/**
 * Created by seantz12 on 01/08/17.
 */

// Todo implement mechanic of only spawning certain amount of enemies at a time to reduce load
// Todo consider learning how to use multiple threads to achieve that goal? Might be unneeded

public class GameView extends SurfaceView implements Runnable {

    // Arraylists for the two classes that I'll be using, makes it more "dynamic"
    private ArrayList<Background> backgrounds;
    private ArrayList<Enemy> enemies;

    // Used to point to activity
    private Context context;

    // Screen size
    private int screenW;
    private int screenH;

    // Hero's important variables
    private Bitmap hero;
    private boolean swung = false;
    private boolean charge = false;
    private int swings = 20;
    private int hp = 5;

    // Hero's position on the canvas
    private float heroX = 10;
    private float heroY = 550;

    // Storing timing of swing to calculate damage
    private long startSwingTime;
    private long timeNotSwung;
    private long totalSwingTime;

    // The amount of enemies that will be used
    private int meleeEnemy;
    private int toughEnemy;
    private int bosses;
    private int[] enemyCount;

    // Pause between enemy spawns
    private int spaceTime;

    // For the purpose of updating and saving level completion
    private int levelNum;
    private SharedPreferences saveFile;
    private SharedPreferences.Editor editor;

    // Statistical purposes, dealing with system time
    private long fps;
    private long timeAtFrame;
    private long startTime;
    private long createTime;

    // Related to actually running the game
    private Thread gameThread = null;
    private SurfaceHolder ourHolder;
    private volatile boolean play;

    // Drawing
    private Canvas canvas;
    private Paint paint;

/**************************************************************************************************/

    public GameView(Context context, int screenW, int screenH, String enemyCount, int levelNum) {
        super(context);

        // Done to keep track of when the thread started for initial enemy spawning
        startTime = System.currentTimeMillis();

        // Self explanatory, takes all passed variables and stores it in this instance
        this.context = context;
        this.screenW = screenW;
        this.screenH = screenH;
        this.enemyCount = getEnemies(enemyCount);
        this.levelNum = levelNum;

        // Getting all the enemies ready to be created
        meleeEnemy = this.enemyCount[0];
        toughEnemy = this.enemyCount[1];
        bosses = this.enemyCount[2];
        enemies = new ArrayList<>();

        createTime = System.currentTimeMillis();
        spaceTime = (int)((Math.random() * 6) + 3)*200;

        // Loads the other resources that will be used
        backgrounds = new ArrayList<>();
        backgrounds.add(new Background(
                this.context,
                screenW,
                screenH,
                "sky_clouds",
                0, 80, 50));

        backgrounds.add(new Background(
                this.context,
                screenW,
                screenH,
                "grass_foreground",
                70, 110, 200));

        hero = BitmapFactory.decodeResource(this.getResources(), R.drawable.hero);

        saveFile = this.context.getSharedPreferences("Save File", Context.MODE_PRIVATE);
        editor = saveFile.edit();

        ourHolder = getHolder();
        paint = new Paint();
    }

/**************************************************************************************************/

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

    public void update() { // Todo add animations in so they aren't a slideshow lmao
        // Updates background positions
        for(Background bg : backgrounds) {
            bg.update(fps);
        }

        if(System.currentTimeMillis() - createTime >= spaceTime && meleeEnemy + toughEnemy > 0) {
            createEnemy();
            createTime = System.currentTimeMillis();
            spaceTime = (int)((Math.random() * 6) + 3)*200;
        }

        // The "a.i" that moves the enemy and does damage to the hero
        Enemy dead = null;
        for(Enemy e : enemies) {
            if(e.xPos > heroX + 40) {
                e.xPos = e.xPos - e.speed;
            } else if(e.xPos <= heroX + 40) {
                // This condition occurs when player is touched by enemy
                // Todo possibly add different ranges for enemies
                hp--;
                dead = e;
                if(hp <= 0) {
                    // Informs player is dead and resets him back to level select
                    Intent intent = new Intent(this.context, LevelSelect.class);
                    this.context.startActivity(intent);
                }
            }
        }
        if(dead != null) enemies.remove(dead); // This removes the enemy that touches the player

        if((int)(System.currentTimeMillis() - timeNotSwung)/100 >= 7 && swings < 20 && !charge) {
            swings++;
            timeNotSwung = System.currentTimeMillis();
        }

        /* For charge attacks (when the player holds down on the screen)
        // Todo maybe leave this with the onscreen handler? why leave it in update?
        if(swung && swings > 0) {
            // Takes the total time the finger was on the screen and converts it to damage delt
            totalSwingTime = System.currentTimeMillis() - startSwingTime;
            int damage = (int)totalSwingTime / 500 + 1;
            checkAndSwing(damage);
            swung = false;
            swings--;
        // If the time between the last time the player swung and now is a second, add a swing back
        } else if((int)(System.currentTimeMillis() - timeNotSwung)/100 >= 5
                && swings < 20 && !charge) {
            swings++;
            timeNotSwung = System.currentTimeMillis();
        }*/
    }

/**************************************************************************************************/

    public void createEnemy() {
        // This will determine what type of enemy is spawned randomly
        int type = ((int) (Math.random() * 2) + 1);

        // Creates the specified type of enemy and updates enemy counter accordingly
        if((type == 1 && meleeEnemy != 0) || toughEnemy == 0) {
            enemies.add(new Enemy(
                    this.context,
                    "melee",
                    1, // HP
                    3, // Speed (needs to be worked on)
                    screenW,
                    550));
            meleeEnemy--;
        } else {
            enemies.add(new Enemy(
                    this.context,
                    "tough",
                    2, // HP
                    (float)2.2, // Speed (needs to be worked on)
                    screenW,
                    550));
            toughEnemy--;
        }
    }

    // Todo possibly only remove multiple enemies on a charge attack
    private void checkAndSwing(int damage) {
        // Records and deals damage to all enemies hit by attack
        ArrayList<Enemy> deadEnemies = new ArrayList<>();
        for(Enemy e : enemies) {
            if (e.xPos <= heroX + 100) {
                if (e.hp - damage <= 0) {
                    deadEnemies.add(e);
                } else {
                    e.hp -= damage;
                    // Todo make animation smoother
                    e.xPos += 100;
                }
            }
        }


        // If any enemies drop < 0, they're removed here
        if(deadEnemies.size() > 0) {
            for(int i = 0; i < deadEnemies.size(); i++) {
                enemies.remove(deadEnemies.get(i));
            }
        }

        // If there are no remaining enemies, the player is moved back to level select screen
        if(enemies.size() == 0) {
            // Note: Math.max is there to ensure that player progress does not get reset.
            // Ex. player beats level 5, goes back to play level 3, progress will remain level 5
            editor.putInt("levelsDone", Math.max(levelNum, saveFile.getInt("levelsDone", 0)));
            editor.commit(); // for purposes of updating player progress
            Intent intent = new Intent(this.context, LevelSelect.class);
            this.context.startActivity(intent);
        }
    }

/**************************************************************************************************/

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                // Saves timing for damage calculation
                charge = true;
                startSwingTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                // Actually swings the sword
                if(swings > 0) {
                    totalSwingTime = System.currentTimeMillis() - startSwingTime;
                    int damage = (int) totalSwingTime / 250 + 1;
                    checkAndSwing(damage);
                    swings--;
                }
                charge = false;
                timeNotSwung = System.currentTimeMillis();
                break;
        }
        return true;
    }

/**************************************************************************************************/

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
            canvas.drawText("Swings: " + swings, 600, 40, paint);
            canvas.drawText("HP: " + hp, 600, 80, paint);
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

/**************************************************************************************************/

    private int[] getEnemies(String enemies) {
        int[] allEnemies = new int[3];

        // Saves typing
        int tIndex = enemies.indexOf('T');
        int bIndex = enemies.indexOf('B');

        // Essentially takes the string and separate the numbers from the string
        allEnemies[0] = Integer.parseInt(enemies.substring(1, tIndex));
        allEnemies[1] = Integer.parseInt(enemies.substring(tIndex+1, bIndex));
        allEnemies[2] = Integer.parseInt(enemies.substring(bIndex+1, enemies.length()));

        return allEnemies;
    }

/**************************************************************************************************/

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

/**************************************************************************************************/

}