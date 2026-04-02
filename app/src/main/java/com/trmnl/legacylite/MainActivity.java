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
  private Prefs prefs; private ApiClient api;
  private ImageView image; private TextView status;
  private final Handler h = new Handler(Looper.getMainLooper());
  private int refreshSec = 60;

  private final ActivityResultLauncher<Intent> configLauncher = registerForActivityResult(
      new ActivityResultContracts.StartActivityForResult(), r -> { if(prefs.configured()) loadNext(); else openConfig(); });

  @Override protected void onCreate(Bundle b){
    super.onCreate(b); setContentView(R.layout.activity_main);
    prefs=new Prefs(this); api=new ApiClient();
    image=findViewById(R.id.fullImage); status=findViewById(R.id.statusText);
    immersive();
    image.setOnClickListener(v -> showMenu());
  }

  @Override protected void onResume(){ super.onResume(); if(!prefs.configured()) openConfig(); else loadNext(); }
  @Override protected void onPause(){ super.onPause(); h.removeCallbacksAndMessages(null); }

  private void openConfig(){ configLauncher.launch(new Intent(this, ConfigActivity.class)); }

  private void loadNext(){
    status.setText("Loading next playlist image...");
    api.getDisplay(prefs.effectiveBase(), prefs.token(), r -> runOnUiThread(() -> applyResult(r,true)));
  }

  private void loadCurrent(){
    status.setText("Refreshing current image...");
    api.getCurrent(prefs.effectiveBase(), prefs.token(), r -> runOnUiThread(() -> applyResult(r,false)));
  }

  private void applyResult(ApiClient.Result r, boolean schedule){
    if(r.ok && r.bitmap!=null){ image.setImageBitmap(r.bitmap); refreshSec = r.refreshRate>0 ? r.refreshRate : 60; status.setText(""); }
    else { status.setText(r.message==null?"Failed to load image":r.message); }
    if(schedule){ h.removeCallbacksAndMessages(null); h.postDelayed(this::loadNext, Math.max(15,refreshSec)*1000L); }
  }

  private void showMenu(){
    String[] opts = new String[]{"Configure Device","Refresh Current Image","Load Next Playlist Image"};
    new AlertDialog.Builder(this)
      .setTitle("Options")
      .setItems(opts,(d,w)->{ if(w==0) openConfig(); else if(w==1) loadCurrent(); else loadNext(); })
      .show();
  }

  private void immersive(){
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    View d=getWindow().getDecorView();
    d.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
  }
}
