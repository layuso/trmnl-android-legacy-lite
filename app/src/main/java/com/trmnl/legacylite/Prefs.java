package com.trmnl.legacylite;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
  public static final String MODE_BYOD = "BYOD";
  public static final String MODE_BYOS = "BYOS";
  public static final String TRMNL_BASE = "https://trmnl.com";
  public static final String ORIENTATION_PORTRAIT = "PORTRAIT";
  public static final String ORIENTATION_LANDSCAPE = "LANDSCAPE";

  private final SharedPreferences sp;
  public Prefs(Context c){ sp=c.getSharedPreferences("trmnl_lite", Context.MODE_PRIVATE); }

  public void save(String mode, String baseUrl, String token){
    sp.edit()
      .putString("mode", mode)
      .putString("base", baseUrl)
      .putString("token", token)
      .putBoolean("configured", true)
      .apply();
  }

  public void clearConfigured(){ sp.edit().putBoolean("configured", false).apply(); }

  public void setOrientation(String orientation){
    String o = ORIENTATION_LANDSCAPE.equals(orientation) ? ORIENTATION_LANDSCAPE : ORIENTATION_PORTRAIT;
    sp.edit().putString("orientation", o).apply();
  }

  public String orientation(){ return sp.getString("orientation", ORIENTATION_LANDSCAPE); }

  public String mode(){ return sp.getString("mode", MODE_BYOD); }
  public String base(){ return sp.getString("base", ""); }
  public String token(){ return sp.getString("token", ""); }

  public boolean configured(){
    boolean flag = sp.getBoolean("configured", false);
    return flag && !token().trim().isEmpty() && (MODE_BYOD.equals(mode()) || !base().trim().isEmpty());
  }

  public String effectiveBase(){ return MODE_BYOD.equals(mode()) ? TRMNL_BASE : base().trim(); }
}
