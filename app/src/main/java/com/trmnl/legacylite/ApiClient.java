package com.trmnl.legacylite;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.json.JSONObject;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

public class ApiClient {
  public static class Result {
    public boolean ok;
    public String message;
    public Bitmap bitmap;
    public int refreshRate;
    public boolean networkError;
    public boolean imageUnavailable;
    public String rawBody;

    public Result(boolean ok, String message, Bitmap bitmap, int refreshRate, boolean networkError, boolean imageUnavailable, String rawBody) {
      this.ok = ok;
      this.message = message;
      this.bitmap = bitmap;
      this.refreshRate = refreshRate;
      this.networkError = networkError;
      this.imageUnavailable = imageUnavailable;
      this.rawBody = rawBody;
    }
  }

  public interface Callback { void onResult(Result r); }

  public void getDisplay(String base, String token, Callback cb){ fetch(base, token, "/api/display", cb); }
  public void getCurrent(String base, String token, Callback cb){ fetch(base, token, "/api/display/current", cb); }

  public void fetchImageByUrl(String imageUrl, Callback cb){
    new Thread(() -> {
      HttpURLConnection ic=null; InputStream is=null;
      try {
        ic = (HttpURLConnection)new URL(imageUrl).openConnection();
        ic.setRequestMethod("GET"); ic.setConnectTimeout(10000); ic.setReadTimeout(10000);
        int code = ic.getResponseCode();
        if(code<200 || code>=300){ cb.onResult(new Result(false,"Fallback image fetch failed",null,30,false,true,null)); return; }
        is = ic.getInputStream();
        Bitmap bmp = BitmapFactory.decodeStream(is);
        if(bmp==null){ cb.onResult(new Result(false,"Fallback image decode failed",null,30,false,true,null)); return; }
        cb.onResult(new Result(true,"OK",bmp,30,false,false,null));
      } catch (Exception e){
        cb.onResult(new Result(false,e.getMessage(),null,30,isNetworkException(e),true,null));
      } finally { try{ if(is!=null)is.close(); }catch(Exception ignored){} if(ic!=null)ic.disconnect(); }
    }).start();
  }

  private void fetch(String base, String token, String path, Callback cb){
    new Thread(() -> {
      HttpURLConnection c=null, ic=null; InputStream is=null;
      try{
        String b = normalize(base);
        URL u = new URL(b + path);
        c = (HttpURLConnection)u.openConnection();
        c.setRequestMethod("GET"); c.setConnectTimeout(10000); c.setReadTimeout(10000);
        c.setRequestProperty("accept","application/json");
        c.setRequestProperty("Access-Token", token.trim());
        int code = c.getResponseCode();

        if(code<200 || code>=300){
          String errBody = tryReadError(c);
          String debug = "endpoint=" + path + "\nhttp_status=" + code + (errBody==null?"":"\n"+errBody);
          cb.onResult(new Result(false,"HTTP "+code,null,60,false,true,debug));
          return;
        }

        String body = read(c.getInputStream());
        JSONObject j = new JSONObject(body);
        String image = first(j.optString("image_url",null), j.optString("imageUrl",null), j.optString("url",null));
        int rr = j.optInt("refresh_rate", j.optInt("refreshRate", 60));
        if(rr<=0) rr=60;

        if(image==null || image.trim().isEmpty()){
          String msg = first(j.optString("error", null), j.optString("message", null), "No image URL in response");
          String debug = "endpoint=" + path + "\nhttp_status=200\n" + body;
          cb.onResult(new Result(false,msg,null,rr,false,true,debug));
          return;
        }

        ic = (HttpURLConnection)new URL(image).openConnection();
        ic.setRequestMethod("GET"); ic.setConnectTimeout(10000); ic.setReadTimeout(10000);
        if(ic.getResponseCode()<200 || ic.getResponseCode()>=300){ cb.onResult(new Result(false,"Image fetch failed",null,rr,false,true,body)); return; }

        is = ic.getInputStream();
        Bitmap bmp = BitmapFactory.decodeStream(is);
        if(bmp==null){ cb.onResult(new Result(false,"Image decode failed",null,rr,false,true,body)); return; }

        String debug = "endpoint=" + path + "\nhttp_status=200\n" + body;
        cb.onResult(new Result(true,"OK",bmp,rr,false,false,debug));
      } catch(Exception e){
        boolean network = isNetworkException(e);
        cb.onResult(new Result(false, network ? "Network connectivity issue: " + e.getMessage() : e.getMessage(), null, 60, network, !network, null));
      } finally{ try{ if(is!=null)is.close(); }catch(Exception ignored){} if(c!=null)c.disconnect(); if(ic!=null)ic.disconnect(); }
    }).start();
  }

  private boolean isNetworkException(Exception e){
    return e instanceof UnknownHostException || e instanceof SocketTimeoutException || e instanceof ConnectException;
  }

  private String tryReadError(HttpURLConnection c){
    try {
      InputStream es = c.getErrorStream();
      if(es == null) return null;
      String data = read(es);
      es.close();
      return data;
    } catch (Exception ignored){
      return null;
    }
  }

  private String normalize(String base){
    String b=base==null?"":base.trim();
    if(!b.startsWith("http://") && !b.startsWith("https://")) b="https://"+b;
    while(b.endsWith("/")) b=b.substring(0,b.length()-1);
    return b;
  }
  private String first(String... vals){ for(String s: vals){ if(s!=null && !s.trim().isEmpty()) return s; } return null; }
  private String read(InputStream is) throws Exception { byte[] buf=new byte[4096]; int n; StringBuilder sb=new StringBuilder(); while((n=is.read(buf))!=-1){ sb.append(new String(buf,0,n)); } return sb.toString(); }
}
