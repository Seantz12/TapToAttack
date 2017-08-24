package com.example.seantz12.taptoattack;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class LevelSelect extends Activity {

    private SharedPreferences saveFile;

    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);

        // Makes all levels past the past level beat invisible
        layout = findViewById(R.id.linearLayout);

        saveFile = getSharedPreferences("Save File", Context.MODE_PRIVATE);
        int levelsDone = saveFile.getInt("levelsDone", 0);

        if(levelsDone < layout.getChildCount()) {
            for(int i = levelsDone + 1; i < layout.getChildCount(); i++) {
                Button level = (Button) layout.getChildAt(i);
                level.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void onClick(View v) {
        Intent intent = new Intent(this, GameScreen.class);
        switch(v.getId()) {
            case R.id.level1:
                // The number is the enemy "code", see GameView constructor for details
                intent.putExtra("enemy", "M10T5B6");
                intent.putExtra("levelNum", 1);
                break;
            case R.id.level2:
                intent.putExtra("enemy", "M10T10B30");
                intent.putExtra("levelNum", 2);
                break;
            case R.id.level3:
                intent.putExtra("enemy", "M0T15B30");
                intent.putExtra("levelNum", 3);
                break;
            case R.id.endlessMode:
                // Currently broken, will think of something to make it actually work
                intent.putExtra("enemy", "M1T1B1");
                break;
        }
        startActivity(intent);
    }
}
