package com.example.seantz12.taptoattack;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by seantz12 on 03/08/17.
 */

public class Enemy {
    Bitmap enemy;
    int hp;
    int speed;
    int xPos;
    int yPos;
    // boolean range (to be implemented later)

    public Enemy(Context context, String enemy, int hp, int speed, int xPos, int yPos) {
        // Makes enemy name into something that can access the resource needed
        int id = context.getResources().getIdentifier(enemy, "drawable",
                context.getPackageName());

        this.enemy= BitmapFactory.decodeResource(context.getResources(), id);
        this.hp = hp;
        this.speed = speed;
        this.xPos = xPos;
        this.yPos = yPos;
    }


}
