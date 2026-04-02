package com.trmnl.legacylite;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
  private static final String FALLBACK_IMAGE_URL = "https://trmnl.com/images/system_screens/setup_logo/og_plus.png";

  private Prefs prefs; private ApiClient api;
  private ImageView image; private TextView status;
  private final Handler h = new Handler(Looper.getMainLooper());
  private int refreshSec = 60;
  private int consecutiveImageFailures = 0;

  private final ActivityResultLauncher<Intent> configLauncher = registerForActivityResult(
      new ActivityResultContracts.StartActivityForResult(), r -> {
        if(prefs.configured()) loadNext(); else openConfig();
      });

  @Override protected void onCreate(Bundle b){
    super.onCreate(b); setContentView(R.layout.activity_main);
    prefs=new Prefs(this); api=new ApiClient();
    image=findViewById(R.id.fullImage); status=findViewById(R.id.statusText);
    immersive();

    image.setOnClickListener(v -> showMenu());
  }

  @Override protected void onResume(){
    super.onResume();
    if(!prefs.configured()) openConfig();
    else loadNext();
  }

  @Override protected void onPause(){
    super.onPause();
    h.removeCallbacksAndMessages(null);
  }

  private void openConfig(){ configLauncher.launch(new Intent(this, ConfigActivity.class)); }

  private void loadNext(){
    status.setVisibility(View.GONE);
    api.getDisplay(prefs.effectiveBase(), prefs.token(), r -> runOnUiThread(() -> applyResult(r)));
  }

  private void loadCurrent(){
    status.setVisibility(View.GONE);
    api.getCurrent(prefs.effectiveBase(), prefs.token(), r -> runOnUiThread(() -> applyResult(r)));
  }

  private void applyResult(ApiClient.Result r){
    if(r.ok && r.bitmap!=null){
      image.setImageBitmap(r.bitmap);
      refreshSec = r.refreshRate>0 ? r.refreshRate : 60;
      status.setText("");
      status.setVisibility(View.GONE);
      consecutiveImageFailures = 0;
      scheduleNext(Math.max(15, refreshSec));
      return;
    }

    if(r.networkError){
      status.setText(r.message==null?"Network connectivity issue":r.message);
      status.setVisibility(View.VISIBLE);
      scheduleNext(30);
      return;
    }

    consecutiveImageFailures++;
    status.setText(r.message==null?"Image unavailable from server":r.message);
    status.setVisibility(View.VISIBLE);
    showFallbackThenHandlePersistence();
  }

  private void showFallbackThenHandlePersistence(){
    api.fetchImageByUrl(FALLBACK_IMAGE_URL, fr -> runOnUiThread(() -> {
      if(fr.ok && fr.bitmap!=null){ image.setImageBitmap(fr.bitmap); }
      h.removeCallbacksAndMessages(null);
      h.postDelayed(() -> {
        if(consecutiveImageFailures >= 2){
          prefs.clearConfigured();
          openConfig();
        } else {
          loadCurrent();
        }
      }, 30000);
    }));
  }

  private void scheduleNext(int seconds){
    h.removeCallbacksAndMessages(null);
    h.postDelayed(this::loadNext, seconds*1000L);
  }

  private void showMenu(){
    String[] options = new String[]{
        "Configure Device",
        "Refresh Current Image",
        "Load Next Playlist Image"
    };

    new AlertDialog.Builder(this)
        .setTitle("Device Options")
        .setItems(options, (dialog, which) -> {
          if(which == 0){
            openConfig();
          } else if(which == 1){
            loadCurrent();
          } else if(which == 2){
            loadNext();
          }
        })
        .show();
  }

  private void immersive(){
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    View d=getWindow().getDecorView();
    d.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
  }
}
