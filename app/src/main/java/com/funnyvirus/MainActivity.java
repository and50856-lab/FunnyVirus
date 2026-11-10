package com.funnyvirus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class MainActivity extends Activity {

    private MediaPlayer player;
    private AudioManager audioManager;
    private boolean isExiting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Прячем системные панели наглухо
        hideSystemUI();

        // Выкручиваем громкость на максимум
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolumeLoop();

        // Создаем интерфейс с картинкой prank.jpg на весь экран
        FrameLayout root = new FrameLayout(this);

        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.FIT_XY);

        try {
            // ВАЖНО: здесь указан prank.jpg, чтобы файл успешно нашёлся в assets!
            java.io.InputStream stream = getAssets().open("prank.jpg");
            Drawable drawable = Drawable.createFromStream(stream, null);
            image.setImageDrawable(drawable);
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        root.addView(image, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));

        // Секретная невидимая кнопка выхода в левом нижнем углу (размер 80x80dp)
        View secretButton = new View(this);
        secretButton.setOnClickListener(v -> stopPrankAndExit());

        int size = (int) (80 * getResources().getDisplayMetrics().density + 0.5f);
        FrameLayout.LayoutParams secretParams = new FrameLayout.LayoutParams(size, size);
        secretParams.gravity = Gravity.BOTTOM | Gravity.START;
        root.addView(secretButton, secretParams);

        setContentView(root);

        // Запускаем звук prank.mp3 из assets в бесконечном цикле
        startSound();

        // Включаем системный режим киоска (Lock Task), чтобы заблокировать жесты и кнопку Home
        try {
            startLockTask();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startSound() {
        try {
            AssetFileDescriptor afd = getAssets().openFd("prank.mp3");
            player = new MediaPlayer();
            player.setAudioAttributes(
                new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            );
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            player.setLooping(true);
            player.prepare();
            player.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void maxVolumeLoop() {
        if (audioManager != null) {
            int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0);
        }
    }

    // Наглухо глушим качельку громкости
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            maxVolumeLoop();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        // Игнорируем кнопку назад
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }

    // Главная ловушка: если пытаются смахнуть шторку или свернуть — мгновенно возвращаем приложение поверх всего
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus && !isExiting) {
            getWindow().getDecorView().postDelayed(() -> {
                if (!isExiting) {
                    try {
                        Intent intent = new Intent(MainActivity.this, MainActivity.this.getClass());
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 10);
        } else if (hasFocus) {
            hideSystemUI();
        }
    }

    private void stopPrankAndExit() {
        isExiting = true;
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
            player = null;
        }

        try {
            stopLockTask();
        } catch (Exception e) {
            e.printStackTrace();
        }

        finishAffinity();
    }

    @Override
    protected void onDestroy() {
        if (player != null) {
            player.release();
            player = null;
        }
        super.onDestroy();
    }
}
