package com.trmnl.legacylite;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;

public class ConfigActivity extends AppCompatActivity {
  private Spinner modeSpinner; private EditText baseEdit, tokenEdit; private Button validateBtn, saveBtn;
  private ImageView preview; private TextView message; private LinearLayout baseWrap;
  private BitmapHolder holder = new BitmapHolder();
  private Prefs prefs; private ApiClient api;

  @Override protected void onCreate(Bundle b){
    super.onCreate(b); setContentView(R.layout.activity_config);
    prefs=new Prefs(this); api=new ApiClient();
    modeSpinner=findViewById(R.id.modeSpinner); baseEdit=findViewById(R.id.baseUrlEdit); tokenEdit=findViewById(R.id.tokenEdit);
    validateBtn=findViewById(R.id.validateBtn); saveBtn=findViewById(R.id.saveBtn); preview=findViewById(R.id.previewImage);
    message=findViewById(R.id.previewMessage); baseWrap=findViewById(R.id.baseWrap);
    message.setMovementMethod(new ScrollingMovementMethod());

    ArrayAdapter<CharSequence> ma = ArrayAdapter.createFromResource(this,R.array.modes,android.R.layout.simple_spinner_item);
    ma.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); modeSpinner.setAdapter(ma);

    String mode=prefs.mode(); modeSpinner.setSelection(Prefs.MODE_BYOS.equals(mode)?1:0);
    tokenEdit.setText(prefs.token()); baseEdit.setText(prefs.base());
    onModeChanged();

    modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { onModeChanged(); }
      @Override public void onNothingSelected(AdapterView<?> p) {}
    });

    validateBtn.setOnClickListener(v -> validate());
    saveBtn.setOnClickListener(v -> save());
    saveBtn.setEnabled(false);
  }

  private void onModeChanged(){ baseWrap.setVisibility(selectedMode().equals(Prefs.MODE_BYOS)?View.VISIBLE:View.GONE); }
  private String selectedMode(){ return modeSpinner.getSelectedItemPosition()==1?Prefs.MODE_BYOS:Prefs.MODE_BYOD; }
  private String selectedBase(){ return selectedMode().equals(Prefs.MODE_BYOD)?Prefs.TRMNL_BASE:baseEdit.getText().toString().trim(); }

  private void validate(){
    saveBtn.setEnabled(false); holder.bmp=null; preview.setImageDrawable(null);
    message.setText("Validating...");
    String token=tokenEdit.getText().toString().trim(); if(token.isEmpty()){ message.setText("Token is required."); return; }
    String base=selectedBase(); if(selectedMode().equals(Prefs.MODE_BYOS)&&base.isEmpty()){ message.setText("Base URL is required for BYOS."); return; }
    api.getDisplay(base, token, r -> runOnUiThread(() -> {
      if(r.ok && r.bitmap!=null){
        holder.bmp=r.bitmap;
        preview.setImageBitmap(r.bitmap);
        message.setText("Preview loaded. Refresh rate: "+r.refreshRate+"s");
        saveBtn.setEnabled(true);
      } else {
        preview.setImageDrawable(null);
        String header = r.message==null?"Device not found or no image returned.":r.message;
        String json = prettyJson(r.rawBody);
        if(json==null || json.trim().isEmpty()) {
          message.setText(header);
        } else {
          message.setText(header + "\n\n" + json);
        }
        saveBtn.setEnabled(false);
      }
    }));
  }

  private void save(){
    if(holder.bmp==null){ Toast.makeText(this,"Cannot save without preview image",Toast.LENGTH_SHORT).show(); return; }
    prefs.save(selectedMode(), selectedMode().equals(Prefs.MODE_BYOD)?Prefs.TRMNL_BASE:baseEdit.getText().toString().trim(), tokenEdit.getText().toString().trim());
    setResult(RESULT_OK); finish();
  }

  private String prettyJson(String raw){
    if(raw == null || raw.trim().isEmpty()) return null;
    String t = raw.trim();
    try {
      if(t.startsWith("{")) return new JSONObject(t).toString(2);
      if(t.startsWith("[")) return new JSONArray(t).toString(2);
      return t;
    } catch (Exception ignored){
      return t;
    }
  }

  private static class BitmapHolder { android.graphics.Bitmap bmp; }
}
