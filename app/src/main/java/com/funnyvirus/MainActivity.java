package com.funnyvirus;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class MainActivity extends Activity {
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Жесткий полноэкранный режим поверх блокировки и вырезов экрана
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        // Убираем системные панели (строку состояния и навигацию) навсегда
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(uiOptions);

        // Создаем корневой контейнер
        FrameLayout frameLayout = new FrameLayout(this);

        // Картинка яйца на весь экран из res/drawable/prank.jpg
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        
        int imageId = getResources().getIdentifier("prank", "drawable", getPackageName());
        if (imageId != 0) {
            imageView.setImageResource(imageId);
        } else {
            imageView.setBackgroundColor(0xFFFF0000); // На всякий случай красный фон, если картинка потеряется
        }

        FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        frameLayout.addView(imageView, imageParams);

        // Невидимая кнопка выхода в правом верхнем углу (200x200 пикселей)
        Button exitButton = new Button(this);
        exitButton.setAlpha(0f); // Абсолютно прозрачная
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPrank();
                finish();
            }
        });

        FrameLayout.LayoutParams btnParams = new FrameLayout.LayoutParams(200, 200);
        btnParams.gravity = Gravity.TOP | Gravity.END;
        frameLayout.addView(exitButton, btnParams);

        setContentView(frameLayout);

        // Выкручиваем системную громкость на абсолютный максимум
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_SHOW_UI);
        }

        // Запускаем зацикленную новогоднюю песню из assets/prank.mp3
        try {
            mediaPlayer = new MediaPlayer();
            AssetFileDescriptor afd = getAssets().openFd("prank.mp3");
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Возвращаем режим обратно, если пользователь случайно смахнул шторку
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
    }

    // Блокируем системную кнопку «Назад»
    @Override
    public void onBackPressed() {
        // Ничего не делает — ловушка глухая
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPrank();
    }

    private void stopPrank() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaPlayer = null;
        }
    }
}
