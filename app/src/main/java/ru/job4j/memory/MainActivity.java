package ru.job4j.memory;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private Map<Integer, Pair<Integer, Integer>> colors = new LinkedHashMap<>();
    {
        colors.put(R.id.button1, new Pair<>(0xFFB22222, 0xFFFF0000));
        colors.put(R.id.button2, new Pair<>(0xFF008000, 0xFF7CFC00));
        colors.put(R.id.button3, new Pair<>(0xFF0000FF, 0xFFADD8E6));
        colors.put(R.id.button4, new Pair<>(0xFFFFFACD, 0xFFFFFF00));
    }

    private Memory memory = new Memory(
            Arrays.asList(R.id.button1, R.id.button2, R.id.button3, R.id.button4)
    );

    public Executor pool = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startEvent(View view) {
       pool.execute(animation(memory.rnd()));
    }

    private Runnable animation(List<Integer> ids) {
        return () -> {
            for (Integer id : ids) {
                try {
                    Button btn = findViewById(id);
                    Pair<Integer, Integer> color = colors.get(btn.getId());
                    runOnUiThread(() -> btn.setBackgroundColor(color.second));
                    Thread.sleep(500);
                    runOnUiThread(() -> btn.setBackgroundColor(color.first));
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void answer(View view) {
        memory.addAnswer(view.getId());
        pool.execute(animation(Collections.singletonList(view.getId())));
        if (memory.check() && memory.isFinish()) {
            memory.cleanAnswers();
            msg("Level " + memory.level() + ". Good, Go next?");
        } else if (!memory.check()) {
            memory.cleanAnswers();
            memory.cleanSeq();
            msg("Level " + memory.level() + ". Wrong, Again?");
        }
    }

    private void msg(String text) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle("Memory Result");
        alertDialogBuilder
                .setMessage(text)
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, id) -> pool.execute(animation(memory.rnd())));
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
