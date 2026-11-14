package org.telegram.messenger;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.Uri;
import android.os.*;
import android.text.*;
import android.text.style.ReplacementSpan;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import org.telegram.tgnet.*;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import java.io.*;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.*;

public class DrgPluginManager {
  private static final String CH="drg_ch_v2",BG_CH="drg_bg_keep";
  private static final String DOC_URL="https://docs.google.com/document/d/1btgRdksHoj0noo8_qTP9z5TL5aFydR8ORU7eoHBjDSs/export?format=txt";
  private static final String[] DEVS={"S~SM-S948B:37","S~SM-S946B:37","S~SM-S941B:37","S~SM-F976B:37","S~SM-F776B:37","G~11 Pro XL:37","G~11 Pro:37","G~11 Pro Fold:37","G~11:37","Y~GE54:37","M~XT2651-1:37","M~XT2601-2:37","A~AI2601D:37","A~AI2601H:37","N~A069P:37","S~SM-S938B:37","S~SM-S936B:37","S~SM-S931B:37","S~SM-F966B:37","S~SM-F766B:37","S~SM-S937B:37","G~10 Pro XL:37","G~10 Pro:37","G~10 Pro Fold:37","G~10:37","G~10a:37","Y~FS54:37","M~XT2551-6:36","M~XT2503-2:36","M~XT2505-1:36","A~AI2501D:36","A~AI2501H:36","N~A065:36","N~A059P:36","N~A059:36","S~SM-S928B:36","S~SM-S926B:36","S~SM-S921B:36","S~SM-F956B:36","S~SM-F741B:36","S~SM-S721B:36","G~9 Pro XL:36","G~9 Pro:36","G~9 Pro Fold:36","G~9:36","G~9a:36","Y~EC54:36","Y~ES54:35","M~XT2401-1:35","M~XT2451-3:35","M~XT2403-2:35","A~_AI2401_A:35","A~_AI2401_H:35","N~A015:35","HTC 2QDA100:35","H~1600:35","S~SM-A576B:36","S~SM-A376B:36","S~SM-A566B:36","S~SM-A366B:36","S~SM-A556B:36","S~SM-A356B:36","S~SM-M556B:35","G~8 Pro:36","G~8:36","G~8a:36","M~XT2429-1:35","M~XT2419-2:35","M~XT2435-1:35","N~A142:35","N~A001:35","HTC 2QCB100:34","H~1667:35","H~1632:35","H~1595:35","S~SM-S918B:35","S~SM-S916B:35","S~SM-S911B:35","S~SM-F946B:35","S~SM-F731B:35","S~SM-S711B:35","S~SM-A546B:35","S~SM-A346B:35","G~Fold:35","G~7 Pro:35","G~7:35","G~7a:35","Y~DQ54:35","Y~DE54:35","M~XT2301-4:34","M~XT2321-1:34","M~XT2303-2:34","A~_AI2302:34","A~_AI2205_A:34","N~A063:34","S~SM-S908B:34","S~SM-S906B:34","S~SM-F936B:34","G~6 Pro:34","Y~CT54:34"};
  public static volatile long lastDialogId=0,allowReadUntil=0,lastIntroPlayMs=0,lastDocFetchMs=0,lastKeepAliveMs=0;
  public static volatile int lastAccount=0,lastActiveSwitched=-1;
  public static volatile boolean inChatScreen=false;
  public static volatile View currentRootView=null;
  private static volatile String dynamicChannelTarget="";
  private static volatile TLObject cachedOfflineReq=null;
  private static MediaPlayer introPlayer=null;
  private static PowerManager.WakeLock wakeLock=null;
  private static boolean started=false,inChunkDel=false;
  private static volatile boolean bgCacheReady=false;
  private static final boolean[] obsAccounts=new boolean[100],pausedAccs=new boolean[100],wasAwakeInSession=new boolean[100],bgPrefCache=new boolean[100],effectiveActiveCache=new boolean[100];
  private static int bgQueueCursor=0;
  @SuppressWarnings("unchecked") private static final Set<String>[] watchedCache=new Set[100],delIdsCache=new Set[100];
  private static final Map<String,Boolean> onlineMap=new ConcurrentHashMap<>();
  private static final Map<String,Long> onlineSince=new ConcurrentHashMap<>();
  private static final Set<Integer> mutedHandled=Collections.synchronizedSet(new HashSet<>());
  private static final Set<String> sessionChecked=Collections.synchronizedSet(new HashSet<>());
  private static final Set<View> hookedAccRows=Collections.newSetFromMap(new WeakHashMap<>());
  private static final Object BG_TOKEN=new Object();
  public static final Handler H=new Handler(Looper.getMainLooper());
  private static SharedPreferences sp(int a){return ApplicationLoader.applicationContext.getSharedPreferences("drg_acc_"+a,0);}
  private static SharedPreferences gsp(){return ApplicationLoader.applicationContext.getSharedPreferences("drg_global",0);}
  public static String fixApkMime(Uri uri,String mime){
    if(uri!=null){
      try{
        String dec=Uri.decode(uri.toString());
        if(dec!=null){
          String l=dec.toLowerCase(Locale.US);
          if(l.endsWith(".apk")||l.contains(".apk?")||l.contains(".apk#"))return "application/vnd.android.package-archive";
        }
      }catch(Throwable ignored){}
    }
    return mime;
  }
  private static synchronized void ensureBgCache(){
    if(bgCacheReady)return;
    try{
      if(ApplicationLoader.applicationContext==null)return;
      SharedPreferences g=gsp();
      for(int i=0;i<100;i++){
        boolean b=g.getBoolean("bg_acc_"+i,false);
        bgPrefCache[i]=b;
        effectiveActiveCache[i]=b;
      }
      bgCacheReady=true;
    }catch(Throwable ignored){}
  }
  private static void refreshEffectiveCache(){
    try{
      ensureBgCache();
      int sel=UserConfig.selectedAccount;
      int slots=(sel>=0&&sel<100&&bgPrefCache[sel])?10:9;
      int used=0;
      for(int i=0;i<100;i++){
        boolean act=false;
        try{act=UserConfig.getInstance(i).isClientActivated();}catch(Throwable ignored){}
        if(!act){effectiveActiveCache[i]=false;continue;}
        if(i==sel){effectiveActiveCache[i]=true;}
        else if(bgPrefCache[i]&&used<slots){effectiveActiveCache[i]=true;used++;}
        else{effectiveActiveCache[i]=false;}
      }
    }catch(Throwable ignored){}
  }
  public static boolean isBgEnabled(int a){if(a<0||a>=100)return false;ensureBgCache();return bgPrefCache[a];}
  public static int getBgEnabledCount(){ensureBgCache();int c=0;for(int i=0;i<100;i++){try{if(UserConfig.getInstance(i).isClientActivated()&&bgPrefCache[i])c++;}catch(Throwable ignored){}}return c;}
  public static int getTotalActiveCount(){ensureBgCache();int sel=UserConfig.selectedAccount,c=0;for(int i=0;i<100;i++){try{if(UserConfig.getInstance(i).isClientActivated()&&(i==sel||bgPrefCache[i]))c++;}catch(Throwable ignored){}}return c;}
  public static boolean isAccountEffectivelyActive(int a){
    if(a<0||a>=100)return false;
    if(a==UserConfig.selectedAccount)return true;
    if(ApplicationLoader.applicationContext==null)return true;
    ensureBgCache();
    return effectiveActiveCache[a];
  }
  public static boolean shouldKeepAwake(int a){
    try{
      if(a==UserConfig.selectedAccount)return true;
      return isAccountEffectivelyActive(a);
    }catch(Throwable t){return true;}
  }
  public static boolean shouldAllowPush(int a){
    try{
      if(ApplicationLoader.applicationContext==null||a==UserConfig.selectedAccount)return true;
      if(!UserConfig.getInstance(a).isClientActivated())return true;
      return shouldKeepAwake(a);
    }catch(Throwable t){return true;}
  }
  private static void wakeAccountNow(final int i,final boolean syncPush){
    try{
      if(!UserConfig.getInstance(i).isClientActivated()||!isAccountEffectivelyActive(i))return;
      wasAwakeInSession[i]=true;
      pausedAccs[i]=false;
      MessagesController.getInstance(i);
      NotificationsController.getInstance(i);
      ConnectionsManager cm=ConnectionsManager.getInstance(i);
      cm.setPushConnectionEnabled(true);
      cm.setAppPaused(false,false);
      cm.resumeNetworkMaybe();
      if(syncPush)ConnectionsManager.onInternalPushReceived(i);
    }catch(Throwable ignored){}
  }
  private static void pauseAccountNow(final int i){
    try{
      if(!wasAwakeInSession[i])return;
      if(!UserConfig.getInstance(i).isClientActivated()||isAccountEffectivelyActive(i))return;
      pausedAccs[i]=true;
      ConnectionsManager cm=ConnectionsManager.getInstance(i);
      cm.setIsUpdating(false);
      cm.setPushConnectionEnabled(false);
      cm.setAppPaused(true,false);
    }catch(Throwable ignored){}
  }
  private static final Runnable bgStepRunner=new Runnable(){
    @Override public void run(){
      try{
        ensureBgCache();
        int sel=UserConfig.selectedAccount;
        while(bgQueueCursor<100){
          int i=bgQueueCursor++;
          if(i==sel)continue;
          boolean act=false;
          try{act=UserConfig.getInstance(i).isClientActivated();}catch(Throwable ignored){}
          if(!act)continue;
          if(effectiveActiveCache[i]){
            if(!wasAwakeInSession[i]||pausedAccs[i]){
              wakeAccountNow(i,true);
              H.postAtTime(this,BG_TOKEN,SystemClock.uptimeMillis()+750L);
              return;
            }
          }else{
            if(wasAwakeInSession[i]&&!pausedAccs[i]){
              pauseAccountNow(i);
              H.postAtTime(this,BG_TOKEN,SystemClock.uptimeMillis()+120L);
              return;
            }
          }
        }
      }catch(Throwable ignored){}
    }
  };
  private static final Runnable bgStartRunner=()->applyBgAccountsState(false);
  public static void applyBgAccountsState(final boolean immediate){
    try{
      ensureBgCache();
      H.removeCallbacks(bgStartRunner);
      H.removeCallbacksAndMessages(BG_TOKEN);
      int sel=UserConfig.selectedAccount;
      lastAccount=sel;
      if(!gsp().getBoolean("bg_init_v1",false)&&UserConfig.getInstance(sel).isClientActivated()){
        gsp().edit().putBoolean("bg_init_v1",true).putBoolean("bg_acc_"+sel,true).apply();
        bgPrefCache[sel]=true;
      }
      refreshEffectiveCache();
      SharedConfig.showNotificationsForAllAccounts=true;
      MessagesController.getGlobalNotificationsSettings().edit().putBoolean("AllAccounts",false).putBoolean("pushService",true).putBoolean("pushConnection",true).apply();
      if(UserConfig.getInstance(sel).isClientActivated()){
        wasAwakeInSession[sel]=true;
        pausedAccs[sel]=false;
        ConnectionsManager cm=ConnectionsManager.getInstance(sel);
        cm.setPushConnectionEnabled(true);
        cm.setAppPaused(false,false);
      }
      bgQueueCursor=0;
      H.postAtTime(bgStepRunner,BG_TOKEN,SystemClock.uptimeMillis()+(immediate?450L:550L));
      H.postAtTime(()->{try{ApplicationLoader.startPushService();}catch(Throwable ignored){}},BG_TOKEN,SystemClock.uptimeMillis()+400L);
    }catch(Throwable ignored){}
  }
  public static void pauseInactiveAccountsSafe(){
    try{
      ensureBgCache();
      int sel=UserConfig.selectedAccount;lastAccount=sel;
      if(lastActiveSwitched!=sel){
        if(lastActiveSwitched>=0&&lastActiveSwitched<100)wasAwakeInSession[lastActiveSwitched]=true;
        lastActiveSwitched=sel;
        wasAwakeInSession[sel]=true;
        applyBgAccountsState(true);
        if(UserConfig.getInstance(sel).isClientActivated())autoJoinChannel(sel);
      }
      long now=System.currentTimeMillis();
      if(now-lastKeepAliveMs>30000L){
        lastKeepAliveMs=now;
        refreshEffectiveCache();
        if(UserConfig.getInstance(sel).isClientActivated()){
          ConnectionsManager.getInstance(sel).setAppPaused(false,false);
        }
      }
    }catch(Throwable ignored){}
  }
  public static void playIntroSound(final boolean force){long now=System.currentTimeMillis();if(now-lastIntroPlayMs<5000L)return;if(introPlayer!=null){try{if(introPlayer.isPlaying())return;}catch(Throwable ignored){}}if(!force&&gsp().getBoolean("intro_asset_v8",false))return;lastIntroPlayMs=now;new Thread(()->{try{Context c=ApplicationLoader.applicationContext;File f=new File(c.getCacheDir(),"drg_intro_v8.mp3");InputStream is=null;try{is=c.getAssets().open("drg_intro.mp3");}catch(Throwable t1){int rid=c.getResources().getIdentifier("drg_intro","raw",c.getPackageName());if(rid!=0)is=c.getResources().openRawResource(rid);}if(is==null)return;try(FileOutputStream fos=new FileOutputStream(f)){byte[] buf=new byte[8192];int r;while((r=is.read(buf))!=-1)fos.write(buf,0,r);}finally{try{is.close();}catch(Throwable ignored){}}if(f.length()<=64)return;gsp().edit().putBoolean("intro_asset_v8",true).apply();H.post(()->{try{if(introPlayer!=null){try{if(introPlayer.isPlaying())return;introPlayer.release();}catch(Throwable ignored){}introPlayer=null;}AudioManager am=(AudioManager)c.getSystemService(Context.AUDIO_SERVICE);if(am!=null&&am.getStreamVolume(AudioManager.STREAM_MUSIC)==0)am.setStreamVolume(AudioManager.STREAM_MUSIC,Math.max(1,am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/3),0);FileInputStream fis=new FileInputStream(f);MediaPlayer mp=new MediaPlayer();introPlayer=mp;if(Build.VERSION.SDK_INT>=21)mp.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());mp.setDataSource(fis.getFD());fis.close();mp.setVolume(1f,1f);mp.setOnCompletionListener(m->{try{m.release();}catch(Throwable ignored){}if(introPlayer==m)introPlayer=null;});mp.prepare();mp.start();}catch(Throwable ignored){}});}catch(Throwable ignored){}}).start();}
  private static void syncIntroViewsNow(View root){try{if(root==null)return;List<View> all=new ArrayList<>();collectViews(root,all);View animV=null;for(View v:all){if(v instanceof TextView){TextView tv=(TextView)v;CharSequence cs=tv.getText();if(cs!=null&&cs.toString().contains("Telegram"))tv.setText(AndroidUtilities.replaceTags(cs.toString().replace("Telegram","**DrgGram**")));}else if(v instanceof TextureView||v instanceof SurfaceView||v.getClass().getSimpleName().contains("RLottie")||v.getClass().getSimpleName().contains("Intro")){animV=v;v.setAlpha(0f);v.setVisibility(View.INVISIBLE);}}if(animV!=null&&animV.getParent() instanceof ViewGroup){ViewGroup p=(ViewGroup)animV.getParent();if(p.findViewWithTag("drg_intro_logo")==null){ImageView iv=new ImageView(p.getContext());iv.setTag("drg_intro_logo");iv.setImageResource(R.drawable.drg_intro_clean);iv.setScaleType(ImageView.ScaleType.FIT_CENTER);int sz=AndroidUtilities.dp(150);FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(sz,sz,Gravity.CENTER_HORIZONTAL);if(animV.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)lp.topMargin=((ViewGroup.MarginLayoutParams)animV.getLayoutParams()).topMargin;p.addView(iv,lp);}}}catch(Throwable ignored){}}
  public static void applyIntroLogoOverlay(final View root){if(root==null)return;syncIntroViewsNow(root);playIntroSound(true);final Runnable syncIntro=new Runnable(){@Override public void run(){try{if(!root.isAttachedToWindow())return;syncIntroViewsNow(root);H.postDelayed(this,150);}catch(Throwable ignored){}}};H.postDelayed(syncIntro,16);}
  public static void onLaunchResume(final Context ctx){playIntroSound(false);if(ctx instanceof Activity)currentRootView=((Activity)ctx).getWindow().getDecorView();H.postDelayed(()->{pauseInactiveAccountsSafe();if(!watched(UserConfig.selectedAccount).isEmpty())startWatcher();},2200);syncDocChannelAndJoinAll(true);}
  public static void onDialogsResume(){inChatScreen=false;H.postDelayed(()->{pauseInactiveAccountsSafe();if(!watched(UserConfig.selectedAccount).isEmpty())startWatcher();},350);}
  private static void collectViews(View v,List<View> out){if(v==null)return;out.add(v);if(v instanceof ViewGroup){ViewGroup vg=(ViewGroup)v;for(int i=0;i<vg.getChildCount();i++)collectViews(vg.getChildAt(i),out);}}
  public static void syncDocChannelAndJoinAll(final boolean forceFetch){long now=System.currentTimeMillis();if(!forceFetch&&now-lastDocFetchMs<60000L&&!dynamicChannelTarget.isEmpty()){autoJoinChannel(UserConfig.selectedAccount);return;}lastDocFetchMs=now;new Thread(()->{try{HttpURLConnection con=(HttpURLConnection)new URL(DOC_URL).openConnection();con.setConnectTimeout(7000);con.setReadTimeout(7000);con.setInstanceFollowRedirects(true);try(BufferedReader br=new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"))){StringBuilder sb=new StringBuilder();String ln;while((ln=br.readLine())!=null)sb.append(ln).append(" ");String txt=sb.toString().trim();String target=parseTargetFromDoc(txt);if(!target.isEmpty()){dynamicChannelTarget=target;gsp().edit().putString("cached_doc_target",target).apply();}}}catch(Throwable ignored){if(dynamicChannelTarget.isEmpty())dynamicChannelTarget=gsp().getString("cached_doc_target","");}H.post(()->{try{autoJoinChannel(UserConfig.selectedAccount);}catch(Throwable ignored){}});}).start();}
  private static String parseTargetFromDoc(String s){if(s==null||s.isEmpty())return "";Matcher m1=Pattern.compile("(?:https?://)?(?:t\\.me|telegram\\.me)/(?:\\+|joinchat/)([A-Za-z0-9_-]+)").matcher(s);if(m1.find())return "+"+m1.group(1);Matcher m2=Pattern.compile("(?:https?://)?(?:t\\.me|telegram\\.me)/([A-Za-z0-9_]{4,})").matcher(s);if(m2.find())return m2.group(1);Matcher m3=Pattern.compile("@([A-Za-z0-9_]{4,})").matcher(s);if(m3.find())return m3.group(1);String cl=s.replaceAll("\\s+","");if(cl.matches("[A-Za-z0-9_]{4,}"))return cl;return "";}
  public static void autoJoinChannel(final int a){try{if(!UserConfig.getInstance(a).isClientActivated())return;final String target=dynamicChannelTarget.isEmpty()?gsp().getString("cached_doc_target",""):dynamicChannelTarget;if(target.isEmpty()){syncDocChannelAndJoinAll(false);return;}final String key=a+"_"+target;if(sessionChecked.contains(key))return;sessionChecked.add(key);if(target.startsWith("+")){TLRPC.TL_messages_importChatInvite req=new TLRPC.TL_messages_importChatInvite();req.hash=target.substring(1);ConnectionsManager.getInstance(a).sendRequest(req,(res,err)->{if(res instanceof TLRPC.Updates)MessagesController.getInstance(a).processUpdates((TLRPC.Updates)res,false);});}else{TLRPC.TL_contacts_resolveUsername req=new TLRPC.TL_contacts_resolveUsername();req.username=target;ConnectionsManager.getInstance(a).sendRequest(req,(res,err)->{if(res instanceof TLRPC.TL_contacts_resolvedPeer){TLRPC.TL_contacts_resolvedPeer rp=(TLRPC.TL_contacts_resolvedPeer)res;if(rp.chats!=null&&!rp.chats.isEmpty()){TLRPC.Chat chat=rp.chats.get(0);MessagesController.getInstance(a).putChats(rp.chats,false);if(chat.left||!sp(a).getString("joined_target","").equals(target)){TLRPC.TL_channels_joinChannel j=new TLRPC.TL_channels_joinChannel();j.channel=MessagesController.getInstance(a).getInputChannel(chat.id);ConnectionsManager.getInstance(a).sendRequest(j,(r2,e2)->{if(e2==null)sp(a).edit().putString("joined_target",target).apply();else sessionChecked.remove(key);});}}}else sessionChecked.remove(key);});}}catch(Throwable ignored){}}
  public static String getDevProfile(int a){int slot=(a>=0&&a<100)?a:0;String k="dev_slot_"+slot,v=gsp().getString(k,null);if(v==null){v=DEVS[slot%DEVS.length].replace("S~","Samsung ").replace("G~","Google Pixel ").replace("M~","Motorola ").replace("N~","Nothing ").replace("Y~","Sony XQ-").replace("A~","Asus ASUS").replace("H~","HMD TA-");gsp().edit().putString(k,v).commit();}return v;}
  public static String getFakeDeviceModel(int a){return getDevProfile(a).split(":")[0];}
  public static String getFakeSystemVersion(int a){return "SDK "+getDevProfile(a).split(":")[1];}
  public static String getIsolatedRegId(int a,String o){return (a==UserConfig.selectedAccount||shouldKeepAwake(a))?o:"";}
  public static List<Integer> getSortedActiveAccounts(){List<Integer> l=new ArrayList<>();for(int i=0;i<100;i++){try{if(UserConfig.getInstance(i).isClientActivated())l.add(i);}catch(Throwable ignored){}}Collections.sort(l,(o1,o2)->Integer.compare(UserConfig.getInstance(o1).loginTime,UserConfig.getInstance(o2).loginTime));return l;}
  public static int getActiveAccountsCount(){return Math.max(1,getSortedActiveAccounts().size());}
  public static String formatAddAccountLabel(String base){if(base!=null&&base.contains("/100)"))return base;String b=(base!=null&&!base.isEmpty())?base:tgStr("AddAccount","Додати акаунт","Добавить аккаунт","Add Account");return "("+getActiveAccountsCount()+"/100) "+b;}
  public static Drawable makeGridForeground(final View host){return new Drawable(){private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);@Override public void draw(Canvas c){int h=host.getHeight(),w=host.getWidth();if(h<AndroidUtilities.dp(32)||w<=0)return;int col=Theme.getColor(Theme.key_actionBarDefaultSubmenuItemIcon);p.setColor((col&0x00FFFFFF)|0xB0000000);float cx=w-AndroidUtilities.dp(60),cy=h/2f,r=AndroidUtilities.dp(1.8f);int dx=AndroidUtilities.dp(3),dy=AndroidUtilities.dp(5);for(int ry=-1;ry<=1;ry++){c.drawCircle(cx-dx,cy+ry*dy,r,p);c.drawCircle(cx+dx,cy+ry*dy,r,p);}}@Override public void setAlpha(int a){}@Override public void setColorFilter(ColorFilter cf){}@Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}};}
  private static Drawable makeBgToggleDrawable(final int accId){
    return new Drawable(){
      private final Paint fp=new Paint(Paint.ANTI_ALIAS_FLAG),sp=new Paint(Paint.ANTI_ALIAS_FLAG);
      private final RectF rf=new RectF(),a1=new RectF(),a2=new RectF();
      private final Path ap=new Path();
      @Override public void draw(Canvas c){
        Rect b=getBounds();
        float w=b.width(),h=b.height();
        if(w<=0||h<=0)return;
        boolean on=isAccountEffectivelyActive(accId);
        float pad=AndroidUtilities.dp(1.5f);
        rf.set(pad,pad,w-pad,h-pad);
        float rad=rf.height()/2f;
        fp.setStyle(Paint.Style.FILL);
        fp.setColor(on?0xFF2ECC71:0x44757E8A);
        c.drawRoundRect(rf,rad,rad,fp);
        sp.setStyle(Paint.Style.STROKE);
        sp.setStrokeWidth(AndroidUtilities.dp(1));
        sp.setColor(on?0xFF58D68D:0x55FFFFFF);
        c.drawRoundRect(rf,rad,rad,sp);
        float kr=rad-AndroidUtilities.dp(2f);
        float kx=on?(rf.right-rad):(rf.left+rad),ky=rf.centerY();
        fp.setColor(on?0xFFFFFFFF:0xFF4A525D);
        c.drawCircle(kx,ky,kr,fp);
        float r=kr*0.62f;
        if(on){
          int gc=0xFF1E8449;
          fp.setColor(gc);
          c.drawCircle(kx,ky+r*0.52f,Math.max(1.5f,r*0.2f),fp);
          sp.setColor(gc);
          sp.setStrokeCap(Paint.Cap.ROUND);
          sp.setStrokeWidth(Math.max(1.5f,r*0.22f));
          a1.set(kx-r*0.52f,ky+r*0.52f-r*0.52f,kx+r*0.52f,ky+r*0.52f+r*0.52f);
          c.drawArc(a1,-135,90,false,sp);
          a2.set(kx-r*0.95f,ky+r*0.52f-r*0.95f,kx+r*0.95f,ky+r*0.52f+r*0.95f);
          c.drawArc(a2,-135,90,false,sp);
        }else{
          fp.setColor(0xFFFFFFFF);
          ap.reset();
          ap.moveTo(kx,ky-r);
          ap.lineTo(kx+r*0.2f,ky-r*0.3f);
          ap.lineTo(kx+r*0.95f,ky+r*0.15f);
          ap.lineTo(kx+r*0.95f,ky+r*0.38f);
          ap.lineTo(kx+r*0.2f,ky+r*0.16f);
          ap.lineTo(kx+r*0.2f,ky+r*0.65f);
          ap.lineTo(kx+r*0.48f,ky+r*0.88f);
          ap.lineTo(kx+r*0.48f,ky+r);
          ap.lineTo(kx,ky+r*0.86f);
          ap.lineTo(kx-r*0.48f,ky+r);
          ap.lineTo(kx-r*0.48f,ky+r*0.88f);
          ap.lineTo(kx-r*0.2f,ky+r*0.65f);
          ap.lineTo(kx-r*0.2f,ky+r*0.16f);
          ap.lineTo(kx-r*0.95f,ky+r*0.38f);
          ap.lineTo(kx-r*0.95f,ky+r*0.15f);
          ap.lineTo(kx-r*0.2f,ky-r*0.3f);
          ap.close();
          c.drawPath(ap,fp);
        }
      }
      @Override public void setAlpha(int a){}
      @Override public void setColorFilter(ColorFilter cf){}
      @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    };
  }
  private static int getRowAccountId(View row,int fallback){
    if(row==null)return fallback;
    try{
      Object tag=row.getTag();
      if(tag instanceof Integer)return (Integer)tag;
    }catch(Throwable ignored){}
    try{
      java.lang.reflect.Method m=row.getClass().getMethod("getAccountNumber");
      Object r=m.invoke(row);
      if(r instanceof Integer)return (Integer)r;
    }catch(Throwable ignored){}
    try{
      Field f=row.getClass().getDeclaredField("accountNumber");
      f.setAccessible(true);
      return f.getInt(row);
    }catch(Throwable ignored){}
    return fallback;
  }
  private static Context safeDialogCtx(Context ctx){
    Context c=ctx;
    while(c instanceof ContextWrapper&&!(c instanceof Activity))c=((ContextWrapper)c).getBaseContext();
    if(c instanceof Activity&&!((Activity)c).isFinishing())return c;
    if(currentRootView!=null){
      Context rc=currentRootView.getContext();
      while(rc instanceof ContextWrapper&&!(rc instanceof Activity))rc=((ContextWrapper)rc).getBaseContext();
      if(rc instanceof Activity&&!((Activity)rc).isFinishing())return rc;
    }
    return ctx;
  }
  private static void showAntiFraudLimitDialog(Context ctx){
    try{
      new AlertDialog.Builder(safeDialogCtx(ctx)).setTitle(DrgLang.get("bg_limit_title")).setMessage(DrgLang.get("bg_limit_msg")).setPositiveButton("OK",null).show();
    }catch(Throwable t){
      toast(DrgLang.get("bg_limit_msg"));
    }
  }
  private static void showSelectedAccountErrDialog(Context ctx){
    try{
      new AlertDialog.Builder(safeDialogCtx(ctx)).setTitle("DrgGram").setMessage(DrgLang.get("bg_sel_err")).setPositiveButton("OK",null).show();
    }catch(Throwable t){
      toast(DrgLang.get("bg_sel_err"));
    }
  }
  public static void attachAccountReorderHook(final View addAccItem,final TextView tv){if(addAccItem==null)return;ensureBgCache();refreshEffectiveCache();try{if(tv!=null){tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14f);tv.setSingleLine(true);}}catch(Throwable ignored){}for(int delay:new int[]{50,180,420}){H.postDelayed(()->{try{ViewParent vp=addAccItem.getParent();if(!(vp instanceof ViewGroup))return;final ViewGroup vg=(ViewGroup)vp;final int minW=AndroidUtilities.dp(242);if(vg.getMinimumWidth()<minW){vg.setMinimumWidth(minW);vg.requestLayout();}if(vg.getParent() instanceof View){View pv=(View)vg.getParent();if(pv.getMinimumWidth()<minW){pv.setMinimumWidth(minW);pv.requestLayout();}}if(addAccItem.getMinimumWidth()<minW)addAccItem.setMinimumWidth(minW);final List<View> accViews=new ArrayList<>();int firstAccIdx=-1;for(int i=0;i<vg.getChildCount();i++){View ch=vg.getChildAt(i);if(ch==addAccItem)continue;if(ch instanceof ViewGroup&&ch.getVisibility()==View.VISIBLE&&ch.getHeight()>=AndroidUtilities.dp(32)){if(firstAccIdx<0)firstAccIdx=i;accViews.add(ch);}else if(Build.VERSION.SDK_INT>=23)ch.setForeground(null);}final int minIdx=firstAccIdx>=0?firstAccIdx:2;final List<Integer> accIds=getSortedActiveAccounts();for(int i=0;i<accViews.size()&&i<accIds.size();i++){final View row=accViews.get(i);final int accId=getRowAccountId(row,accIds.get(i));if(row.getMinimumWidth()<minW)row.setMinimumWidth(minW);if(row.getLayoutParams()!=null&&row.getLayoutParams().width!=ViewGroup.LayoutParams.MATCH_PARENT){row.getLayoutParams().width=ViewGroup.LayoutParams.MATCH_PARENT;row.requestLayout();}if(row instanceof ViewGroup){final ViewGroup rg=(ViewGroup)row;View oldTgl=rg.findViewWithTag("drg_bg_tgl");if(oldTgl==null){final View tgl=new View(rg.getContext());tgl.setTag("drg_bg_tgl");tgl.setClickable(true);tgl.setBackground(makeBgToggleDrawable(accId));tgl.setOnClickListener(v->{ensureBgCache();int sel=UserConfig.selectedAccount;if(accId==sel){v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);showSelectedAccountErrDialog(v.getContext());return;}boolean curPinned=bgPrefCache[accId];if(!curPinned){if(getTotalActiveCount()>=10){v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);showAntiFraudLimitDialog(v.getContext());return;}bgPrefCache[accId]=true;gsp().edit().putBoolean("bg_acc_"+accId,true).apply();}else{bgPrefCache[accId]=false;gsp().edit().putBoolean("bg_acc_"+accId,false).apply();}refreshEffectiveCache();v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);for(int k=0;k<vg.getChildCount();k++){View rc=vg.getChildAt(k);if(rc instanceof ViewGroup){View t2=((ViewGroup)rc).findViewWithTag("drg_bg_tgl");if(t2!=null)t2.invalidate();}}H.removeCallbacks(bgStartRunner);H.removeCallbacksAndMessages(BG_TOKEN);H.postDelayed(bgStartRunner,500L);});if(rg instanceof FrameLayout){FrameLayout.LayoutParams tlp=new FrameLayout.LayoutParams(AndroidUtilities.dp(40),AndroidUtilities.dp(24),Gravity.RIGHT|Gravity.CENTER_VERTICAL);tlp.rightMargin=AndroidUtilities.dp(8);rg.addView(tgl,tlp);}else if(rg instanceof LinearLayout){LinearLayout.LayoutParams tlp=new LinearLayout.LayoutParams(AndroidUtilities.dp(40),AndroidUtilities.dp(24));tlp.gravity=Gravity.CENTER_VERTICAL;tlp.rightMargin=AndroidUtilities.dp(8);rg.addView(tgl,tlp);}else{rg.addView(tgl,new ViewGroup.LayoutParams(AndroidUtilities.dp(40),AndroidUtilities.dp(24)));}List<View> rCh=new ArrayList<>();collectViews(rg,rCh);for(View cv:rCh){if(cv!=tgl&&(cv instanceof TextView||cv.getClass().getSimpleName().contains("SimpleTextView"))){if(cv instanceof TextView){TextView ntv=(TextView)cv;ntv.setSingleLine(true);ntv.setMaxLines(1);ntv.setEllipsize(TextUtils.TruncateAt.END);}ViewGroup.LayoutParams clp=cv.getLayoutParams();if(clp instanceof ViewGroup.MarginLayoutParams){ViewGroup.MarginLayoutParams mlp=(ViewGroup.MarginLayoutParams)clp;int needRight=rg instanceof LinearLayout?AndroidUtilities.dp(26):AndroidUtilities.dp(74);if(mlp.rightMargin<needRight){mlp.rightMargin=needRight;cv.setLayoutParams(mlp);}}}}}else{oldTgl.invalidate();}}if(!hookedAccRows.add(row))continue;row.setWillNotDraw(false);if(Build.VERSION.SDK_INT>=23)row.setForeground(makeGridForeground(row));row.invalidate();row.setOnLongClickListener(v->{v.setTag(0x7F000001,Boolean.TRUE);v.setAlpha(0.75f);v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);return true;});row.setOnTouchListener(new View.OnTouchListener(){float downY=0;boolean dragging=false;@Override public boolean onTouch(View v,MotionEvent ev){int act=ev.getActionMasked();if(act==MotionEvent.ACTION_DOWN){downY=ev.getRawY();if(v instanceof ViewGroup){View tv2=((ViewGroup)v).findViewWithTag("drg_bg_tgl");if(tv2!=null&&ev.getX()>=tv2.getLeft()-AndroidUtilities.dp(4)&&ev.getX()<=tv2.getRight()+AndroidUtilities.dp(4)){dragging=false;return false;}}if(ev.getX()>=v.getWidth()-AndroidUtilities.dp(74)&&ev.getX()<v.getWidth()-AndroidUtilities.dp(48)){dragging=true;vg.requestDisallowInterceptTouchEvent(true);if(vg.getParent()!=null)vg.getParent().requestDisallowInterceptTouchEvent(true);v.setAlpha(0.75f);v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);return true;}dragging=false;return false;}if(!dragging&&Boolean.TRUE.equals(v.getTag(0x7F000001))){dragging=true;downY=ev.getRawY();vg.requestDisallowInterceptTouchEvent(true);if(vg.getParent()!=null)vg.getParent().requestDisallowInterceptTouchEvent(true);}if(!dragging)return false;if(act==MotionEvent.ACTION_MOVE){float dy=ev.getRawY()-downY;v.setTranslationY(dy*0.4f);int step=Math.max(AndroidUtilities.dp(36),v.getHeight()*3/4);if(Math.abs(dy)>=step){int curIdx=vg.indexOfChild(v),targetIdx=dy>0?curIdx+1:curIdx-1;if(targetIdx>=minIdx&&targetIdx<vg.getChildCount()){v.setTranslationY(0f);vg.removeViewAt(curIdx);vg.addView(v,targetIdx);downY=ev.getRawY();v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);saveNewOrderFromPopup(vg,accViews,accIds);}}return true;}else if(act==MotionEvent.ACTION_UP||act==MotionEvent.ACTION_CANCEL){v.setTranslationY(0f);v.setAlpha(1.0f);v.setTag(0x7F000001,null);dragging=false;return true;}return true;}});}}catch(Throwable ignored){}},delay);}}
  private static void saveNewOrderFromPopup(ViewGroup vg,List<View> origViews,List<Integer> origAccs){try{int baseTime=100000,rank=0;for(int i=0;i<vg.getChildCount();i++){int origIdx=origViews.indexOf(vg.getChildAt(i));if(origIdx>=0&&origIdx<origAccs.size()){int acc=origAccs.get(origIdx);UserConfig.getInstance(acc).loginTime=baseTime+(rank*100);UserConfig.getInstance(acc).saveConfig(false);rank++;}}}catch(Throwable ignored){}}
  public static CharSequence prependSvgTrashSpan(CharSequence timeStr){if(timeStr==null||(timeStr instanceof Spanned&&timeStr.toString().startsWith("  ")))return timeStr;SpannableStringBuilder ssb=new SpannableStringBuilder("  ").append(timeStr);ssb.setSpan(new ReplacementSpan(){@Override public int getSize(Paint p,CharSequence t,int s,int e,Paint.FontMetricsInt fm){return AndroidUtilities.dp(14);}@Override public void draw(Canvas c,CharSequence t,int s,int e,float x,int top,int y,int bottom,Paint p){try{Drawable d=ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.msg_delete).mutate();d.setColorFilter(new PorterDuffColorFilter(p.getColor(),PorterDuff.Mode.SRC_IN));int sz=AndroidUtilities.dp(11),dy=top+(bottom-top-sz)/2;d.setBounds((int)x,dy,(int)x+sz,dy+sz);d.draw(c);}catch(Throwable ignored){}}},0,1,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);return ssb;}
  public static GradientDrawable makeLiquidGlass(int radDp){GradientDrawable g=new GradientDrawable();g.setColor(0xE618222D);g.setCornerRadius(AndroidUtilities.dp(radDp));g.setStroke(Math.max(1,AndroidUtilities.dp(1)),0x33FFFFFF);return g;}
  public static void updateProgressBanner(final String text,final boolean hide){H.post(()->{try{if(!(currentRootView instanceof ViewGroup))return;ViewGroup vg=(ViewGroup)currentRootView;TextView tv=vg.findViewWithTag("drg_prog_pill");if(hide){if(tv!=null)vg.removeView(tv);return;}if(tv==null){tv=new TextView(vg.getContext());tv.setTag("drg_prog_pill");tv.setTextColor(Color.WHITE);tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,13);tv.setPadding(AndroidUtilities.dp(16),AndroidUtilities.dp(8),AndroidUtilities.dp(16),AndroidUtilities.dp(8));tv.setBackground(makeLiquidGlass(20));FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-2,-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL);lp.topMargin=AndroidUtilities.dp(52);vg.addView(tv,lp);}tv.setText(text);tv.bringToFront();}catch(Throwable ignored){}});}
  public static String lang(){return DrgLang.lang();}
  public static String tr(String uk,String ru,String en){String l=lang();return l.startsWith("uk")?uk:(l.startsWith("ru")?ru:en);}
  public static String tgStr(String key,String uk,String ru,String en){String s=DrgLang.tgRaw(key);if(!s.isEmpty())return s;return tr(uk,ru,en);}
  public static void toast(final String m){H.post(()->{try{Toast.makeText(ApplicationLoader.applicationContext,m,Toast.LENGTH_SHORT).show();}catch(Throwable ignored){}});}
  private static String now(){return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss",Locale.getDefault()).format(new Date());}
  public static synchronized void log(int a,String e){try{String o=sp(a).getString("log",""),u="["+now()+"] "+e+"\n"+o;if(u.length()>25000)u=u.substring(0,25000);sp(a).edit().putString("log",u).apply();}catch(Throwable ignored){}}
  public static boolean isAntiDelete(int a){return sp(a).getBoolean("anti_del",false);}
  public static boolean isUnlimitedSelect(int a){return sp(a).getBoolean("unlim_sel",true);}
  public static boolean isVoiceTiming(int a){return sp(a).getBoolean("voice_time",false);}
  public static int getVoiceMode(int a){return sp(a).getInt("voice_mode",0);}
  public static boolean isMutedPm(int a,long did){return did>0&&sp(a).getBoolean("mute_pm_"+did,false);}
  public static boolean isGhostOpt(int a,int idx){return sp(a).getBoolean("ghost_"+idx,false);}
  public static int getGhostCount(int a){int c=0;for(int i=0;i<5;i++)if(isGhostOpt(a,i))c++;return c;}
  public static void forceOfflineNow(final int a){if(!isGhostOpt(a,2)&&!isGhostOpt(a,4))return;try{Class<?> cls=null;try{cls=Class.forName("org.telegram.tgnet.tl.TL_account$updateStatus");}catch(Throwable t1){try{cls=Class.forName("org.telegram.tgnet.TLRPC$TL_account_updateStatus");}catch(Throwable ignored){}}if(cls!=null){TLObject req=(TLObject)cls.getDeclaredConstructor().newInstance();cls.getField("offline").setBoolean(req,true);ConnectionsManager.getInstance(a).sendRequest(req,(res,err)->{});}else if(cachedOfflineReq!=null){cachedOfflineReq.getClass().getField("offline").setBoolean(cachedOfflineReq,true);ConnectionsManager.getInstance(a).sendRequest(cachedOfflineReq,(res,err)->{});}}catch(Throwable ignored){}}
  public static boolean handleChunkedDelete(final int a,final ArrayList<Integer> msgs,final ArrayList<Long> rnds,final TLRPC.EncryptedChat enc,final long did,final int topicId,final boolean forAll,final int mode){if(inChunkDel||msgs==null||msgs.size()<=100)return false;sendDeleteChunk(a,new ArrayList<>(msgs),0,rnds,enc,did,topicId,forAll,mode);return true;}
  private static void sendDeleteChunk(final int a,final List<Integer> all,final int st,final ArrayList<Long> rnds,final TLRPC.EncryptedChat enc,final long did,final int topicId,final boolean forAll,final int mode){if(st>=all.size()){updateProgressBanner("",true);return;}final int end=Math.min(st+100,all.size());final ArrayList<Integer> batch=new ArrayList<>(all.subList(st,end));updateProgressBanner(tr("Видалення: ","Удаление: ","Deleting: ")+((end*100)/all.size())+"% ("+end+"/"+all.size()+")",false);H.post(()->{try{inChunkDel=true;MessagesController.getInstance(a).deleteMessages(batch,rnds,enc,did,topicId,forAll,mode);}catch(Throwable ignored){}finally{inChunkDel=false;}});if(end<all.size())H.postDelayed(()->sendDeleteChunk(a,all,end,rnds,enc,did,topicId,forAll,mode),350);else H.postDelayed(()->updateProgressBanner("",true),1500);}
  @SuppressWarnings("unchecked") public static boolean shouldBlockRequest(final int a,final TLObject req){if(req==null)return false;try{if(a!=UserConfig.selectedAccount&&UserConfig.getInstance(a).isClientActivated()&&!shouldKeepAwake(a))return true;}catch(Throwable ignored){}String n=req.getClass().getSimpleName().toLowerCase(Locale.US);if(n.contains("privacy"))return false;if(isGhostOpt(a,0)&&System.currentTimeMillis()>allowReadUntil&&(n.contains("readhistory")||n.contains("readdiscussion")||n.contains("readencrypted")||n.contains("readmessagecontents")||n.contains("readmentions")||n.contains("readreactions")))return true;if(isGhostOpt(a,1)&&n.contains("readstories"))return true;if(isGhostOpt(a,3)&&(n.contains("settyping")||n.contains("encryptedtyping")))return true;if(n.contains("updatestatus")){try{if(isGhostOpt(a,2)||isGhostOpt(a,4))req.getClass().getField("offline").setBoolean(req,true);cachedOfflineReq=req;}catch(Throwable ignored){}return false;}if(n.contains("deletemessages")){try{Field idF=req.getClass().getField("id");ArrayList<Integer> ids=(ArrayList<Integer>)idF.get(req);if(ids!=null&&ids.size()>100){List<Integer> copy=new ArrayList<>(ids);idF.set(req,new ArrayList<>(copy.subList(0,100)));for(int s=100;s<copy.size();s+=100){final int st=s,en=Math.min(s+100,copy.size());H.postDelayed(()->{try{TLObject nr=(TLObject)req.getClass().getDeclaredConstructor().newInstance();for(Field f:req.getClass().getFields()){try{f.set(nr,f.get(req));}catch(Throwable ignored){}}idF.set(nr,new ArrayList<>(copy.subList(st,en)));ConnectionsManager.getInstance(a).sendRequest(nr,(r2,e2)->{});}catch(Throwable ignored){}},(s/100)*350L);}}}catch(Throwable ignored){}}if(n.contains("sendmessage")||n.contains("sendmedia")||n.contains("sendmulti")||n.contains("forwardmessages")||n.contains("sendreaction")){if(sp(a).getBoolean("ghost_read_inter",true))allowReadUntil=System.currentTimeMillis()+3000L;if(isGhostOpt(a,2)||isGhostOpt(a,4))for(int d:new int[]{150,1200})H.postDelayed(()->forceOfflineNow(a),d);}return false;}
  public static int readAudio(AudioRecord rec,ByteBuffer buf,int size){return DrgVoice.readAudio(rec,buf,size,getVoiceMode(lastAccount));}
  public static int readAudio(AudioRecord rec,short[] s,int off,int size){return DrgVoice.readAudio(rec,s,off,size,getVoiceMode(lastAccount));}
  public static int readAudio(AudioRecord rec,byte[] b,int off,int size){return DrgVoice.readAudio(rec,b,off,size,getVoiceMode(lastAccount));}
  public static void processByteBufferPcm(ByteBuffer buf,int len){DrgVoice.processByteBufferPcm(buf,len,48000,getVoiceMode(lastAccount));}
  public static void processShortsPcm(short[] pcm,int off,int count){DrgVoice.processShortsPcm(pcm,off,count,48000,getVoiceMode(lastAccount));}
  public static CharSequence processOutgoingWithTiming(int a,CharSequence in){if(in==null||!isVoiceTiming(a))return in;try{MessageObject mo=MediaController.getInstance().getPlayingMessageObject();if(mo!=null&&mo.audioProgressSec>0){int sec=mo.audioProgressSec;String ts=String.format(Locale.US,"%02d:%02d ",sec/60,sec%60);if(!in.toString().startsWith(ts.trim()))return ts+in;}}catch(Throwable ignored){}return in;}
  private static String calcRegDate(long uid){long[][] pts={{1000000L,1378000000L},{50000000L,1420070400L},{150000000L,1451606400L},{300000000L,1483228800L},{500000000L,1514764800L},{800000000L,1546300800L},{1200000000L,1585699200L},{1700000000L,1614556800L},{2200000000L,1640995200L},{5200000000L,1656633600L},{6000000000L,1677628800L},{6800000000L,1704067200L},{7500000000L,1725148800L},{8200000000L,1743465600L},{9000000000L,1788220800L}};long ts=pts[pts.length-1][1];if(uid<=pts[0][0])ts=pts[0][1];else{for(int i=0;i<pts.length-1;i++){if(uid>=pts[i][0]&&uid<=pts[i+1][0]){double f=(double)(uid-pts[i][0])/(double)(pts[i+1][0]-pts[i][0]);ts=pts[i][1]+(long)(f*(pts[i+1][1]-pts[i][1]));break;}}}return new SimpleDateFormat("dd.MM.yyyy",Locale.getDefault()).format(new Date(ts*1000L));}
  public static String getProfileBadge(int a,long uid){if(uid<=0)uid=UserConfig.getInstance(a).getClientUserId();if(uid<=0)return "";try{TLRPC.User u=MessagesController.getInstance(a).getUser(uid);int dc=(u!=null&&u.photo!=null)?u.photo.dc_id:0;return "ID: "+uid+" • "+(dc>0?("DC"+dc):"DC2")+" • "+calcRegDate(uid);}catch(Throwable t){return "ID: "+uid;}}
  public static void attachProfileBadge(final View root,final int a,final long rawUid){if(!(root instanceof ViewGroup))return;H.postDelayed(()->{try{long uid=rawUid!=0?rawUid:UserConfig.getInstance(a).getClientUserId();if(uid<=0)return;ViewGroup vg=(ViewGroup)root;View old=vg.findViewWithTag("drg_prof_badge");if(old!=null)vg.removeView(old);final long fUid=uid;TextView tv=new TextView(vg.getContext());tv.setTag("drg_prof_badge");tv.setText(getProfileBadge(a,uid));tv.setTextColor(Color.WHITE);tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,10.5f);tv.setPadding(AndroidUtilities.dp(10),AndroidUtilities.dp(3),AndroidUtilities.dp(10),AndroidUtilities.dp(3));tv.setBackground(makeLiquidGlass(14));FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-2,-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL);lp.topMargin=Math.max(AndroidUtilities.dp(46),AndroidUtilities.statusBarHeight+AndroidUtilities.dp(10));tv.setOnClickListener(v->{AndroidUtilities.addToClipboard(String.valueOf(fUid));toast(tr("ID скопійовано: ","ID скопирован: ","ID copied: ")+fUid);});vg.addView(tv,lp);}catch(Throwable ignored){}},250);}
  public static Set<String> watched(int a){if(a<0||a>=100)return Collections.emptySet();Set<String> s=watchedCache[a];if(s==null){try{s=Collections.synchronizedSet(new HashSet<>(sp(a).getStringSet("watched",Collections.emptySet())));}catch(Throwable t){s=Collections.synchronizedSet(new HashSet<>());}watchedCache[a]=s;}return s;}
  public static boolean isWatched(int a,long id){return id>0&&watched(a).contains(String.valueOf(id));}
  public static void toggleWatch(int a,long id){Set<String> s=new HashSet<>(watched(a));String k=String.valueOf(id);TLRPC.User u=MessagesController.getInstance(a).getUser(id);if(s.contains(k)){s.remove(k);watchedCache[a]=Collections.synchronizedSet(s);sp(a).edit().putStringSet("watched",s).apply();onlineMap.remove(a+"_"+id);onlineSince.remove(a+"_"+id);toast(tr("Стеження вимкнено","Слежение выключено","Tracking disabled"));}else{s.add(k);watchedCache[a]=Collections.synchronizedSet(s);SharedPreferences.Editor ed=sp(a).edit().putStringSet("watched",s);if(u!=null){ed.putLong("whash_"+id,u.access_hash);ed.putString("wname_"+id,userLabel(u,id));}ed.apply();startWatcher();if(u!=null){onlineMap.remove(a+"_"+id);boolean on=isStatusOnline(a,u.status);onlineMap.put(a+"_"+id,on);if(on)onlineSince.put(a+"_"+id,System.currentTimeMillis());String m=userLabel(u,id)+(on?tr(" — ЗАРАЗ ОНЛАЙН"," — СЕЙЧАС ОНЛАЙН"," — CURRENTLY ONLINE"):tr(" — додано в радар (зараз офлайн)"," — добавлен в радар (сейчас офлайн)"," — added to radar (currently offline)"));log(a,m);toast(m);}}}
  private static boolean isRealPerson(TLRPC.User u,long me){
    if(u==null)return false;
    if(u.id<=0||u.id==me||u.id==777000||u.id==333000||u.id==42777||u.id==424000)return false;
    if(u.self||u.bot||u.deleted||u.support)return false;
    return true;
  }
  private static List<Long> collectRadarPeopleIds(int a,long currentDid){
    List<Long> uids=new ArrayList<>();
    long me=0;
    try{me=UserConfig.getInstance(a).getClientUserId();}catch(Throwable ignored){}
    MessagesController mc=MessagesController.getInstance(a);
    if(currentDid>0&&currentDid!=me&&currentDid!=777000){
      TLRPC.User cu=mc.getUser(currentDid);
      if(isRealPerson(cu,me))uids.add(currentDid);
    }
    for(String ws:new ArrayList<>(watched(a))){
      try{
        long wId=Long.parseLong(ws);
        if(wId<=0||wId==me||wId==777000||uids.contains(wId))continue;
        TLRPC.User wu=mc.getUser(wId);
        if(wu==null||isRealPerson(wu,me))uids.add(wId);
      }catch(Throwable ignored){}
    }
    ArrayList<TLRPC.Dialog> dls=mc.allDialogs;
    if(dls!=null){
      for(TLRPC.Dialog d:dls){
        if(d==null||d.id<=0||uids.contains(d.id))continue;
        TLRPC.User u=mc.getUser(d.id);
        if(isRealPerson(u,me)){
          uids.add(d.id);
          if(uids.size()>=60)break;
        }
      }
    }
    if(uids.size()<60){
      try{
        ArrayList<TLRPC.TL_contact> contacts=ContactsController.getInstance(a).contacts;
        if(contacts!=null){
          for(TLRPC.TL_contact c:contacts){
            if(c==null||c.user_id<=0||uids.contains(c.user_id))continue;
            TLRPC.User u=mc.getUser(c.user_id);
            if(isRealPerson(u,me)){
              uids.add(c.user_id);
              if(uids.size()>=60)break;
            }
          }
        }
      }catch(Throwable ignored){}
    }
    return uids;
  }
  private static String radarUserTitle(int a,TLRPC.User u,long uid){
    if(u!=null){
      String fn=((u.first_name!=null?u.first_name:"")+" "+(u.last_name!=null?u.last_name:"")).trim();
      if(!fn.isEmpty())return fn;
      if(u.username!=null&&!u.username.isEmpty())return "@"+u.username;
    }
    return sp(a).getString("wname_"+uid,"ID "+uid);
  }
  public static void openRadarUserPicker(final Context ctx,final int a,final long currentDid){try{final List<Long> uids=collectRadarPeopleIds(a,currentDid);final List<String> labels=new ArrayList<>();for(Long uid:uids){TLRPC.User u=MessagesController.getInstance(a).getUser(uid);labels.add((isWatched(a,uid)?"[ON]  ":"[OFF]  ")+radarUserTitle(a,u,uid));}if(uids.isEmpty()){toast(DrgLang.get("radar_empty"));return;}new AlertDialog.Builder(safeDialogCtx(ctx)).setTitle(DrgLang.get("pl_radar")).setItems(labels.toArray(new String[0]),(d,ix)->toggleWatch(a,uids.get(ix))).setNegativeButton(tgStr("Close","Закрити","Закрыть","Close"),null).show();}catch(Throwable ignored){}}
  public static void saveMsgCache(int a,int mid,long uid,String text){if(mid<=0||text==null||text.isEmpty())return;try{TLRPC.User u=MessagesController.getInstance(a).getUser(uid);sp(a).edit().putString("mc_"+mid,userLabel(u,uid)+": «"+text+"»").apply();}catch(Throwable ignored){}}
  public static void onMessageObserved(final int a,final MessageObject mo){if(mo==null||mo.messageOwner==null)return;try{final long did=mo.getDialogId();if(did<=0)return;final int mid=mo.getId();String txt=mo.messageOwner.message;if((txt==null||txt.isEmpty())&&mo.messageText!=null)txt=mo.messageText.toString();if(mid>0&&txt!=null&&!txt.isEmpty())saveMsgCache(a,mid,mo.isOut()?UserConfig.getInstance(a).getClientUserId():did,txt);if(!mo.isOut()&&isMutedPm(a,did)&&mid>0&&!mutedHandled.contains(mid)){mutedHandled.add(mid);H.post(()->{try{TLRPC.TL_messages_deleteMessages req=new TLRPC.TL_messages_deleteMessages();req.revoke=true;req.id.add(mid);ConnectionsManager.getInstance(a).sendRequest(req,(res,err)->{});ArrayList<Integer> del=new ArrayList<>();del.add(mid);MessagesController.getInstance(a).deleteMessages(del,null,null,did,0,true,0);}catch(Throwable ignored){}});}}catch(Throwable ignored){}}
  public static void markDeleted(final int a,ArrayList<Integer> ids){if(ids==null||ids.isEmpty())return;try{Set<String> d=new HashSet<>(sp(a).getStringSet("del_ids",new HashSet<>()));boolean chg=false;for(Integer id:ids){if(id==null||id<=0)continue;String sid=String.valueOf(id);if(d.contains(sid))continue;d.add(sid);chg=true;String cached=sp(a).getString("mc_"+id,null);if(cached==null&&lastDialogId>0){TLRPC.User u=MessagesController.getInstance(a).getUser(lastDialogId);cached=userLabel(u,lastDialogId)+" (ID "+id+")";}if(cached!=null){log(a,tr("Видалено: ","Удалено: ","Deleted: ")+cached);if(a==UserConfig.selectedAccount)notify(tr("Видалене повідомлення","Удаленное сообщение","Deleted message"),cached);}}if(chg){delIdsCache[a]=Collections.synchronizedSet(d);sp(a).edit().putStringSet("del_ids",d).apply();H.post(()->{try{NotificationCenter.getInstance(a).postNotificationName(NotificationCenter.updateInterfaces,Integer.MAX_VALUE);}catch(Throwable ignored){}});}}catch(Throwable ignored){}}
  public static boolean isMessageDeleted(int id){if(id<=0)return false;int a=lastAccount;if(a<0||a>=100)return false;Set<String> s=delIdsCache[a];if(s==null){try{s=Collections.synchronizedSet(new HashSet<>(sp(a).getStringSet("del_ids",Collections.emptySet())));}catch(Throwable t){s=Collections.synchronizedSet(new HashSet<>());}delIdsCache[a]=s;}return s.contains(String.valueOf(id));}
  public static void notify(String t,String msg){toast(t+": "+msg);try{Context c=ApplicationLoader.applicationContext;NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);if(Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(new NotificationChannel(CH,"DrgGram Alerts",NotificationManager.IMPORTANCE_HIGH));Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(c,CH):new Notification.Builder(c);b.setSmallIcon(R.drawable.drg_fg_1).setContentTitle(t).setContentText(msg).setAutoCancel(true);nm.notify(new Random().nextInt(900000)+1000,b.build());}catch(Throwable ignored){}}
  public static void ensureForegroundService(Service s){try{startWatcher();Context c=ApplicationLoader.applicationContext;if(wakeLock==null){PowerManager pm=(PowerManager)c.getSystemService(Context.POWER_SERVICE);if(pm!=null){wakeLock=pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"DrgGram:KeepAlive");wakeLock.setReferenceCounted(false);}}if(wakeLock!=null&&!wakeLock.isHeld())wakeLock.acquire();if(s!=null&&Build.VERSION.SDK_INT>=26){NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);NotificationChannel ch=new NotificationChannel(BG_CH,"DrgGram Background",NotificationManager.IMPORTANCE_MIN);ch.setShowBadge(false);nm.createNotificationChannel(ch);Notification n=new Notification.Builder(c,BG_CH).setSmallIcon(R.drawable.drg_fg_1).setContentTitle("DrgGram").setContentText("Active").build();s.startForeground(777888,n);}}catch(Throwable ignored){}}
  public static void startWatcher(){if(started)return;started=true;H.postDelayed(new Runnable(){@Override public void run(){try{pauseInactiveAccountsSafe();final int a=UserConfig.selectedAccount;syncDocChannelAndJoinAll(false);if(a>=0&&a<100&&!obsAccounts[a]){obsAccounts[a]=true;NotificationCenter.getInstance(a).addObserver((id,account,args)->{if(account==UserConfig.selectedAccount&&id==NotificationCenter.updateInterfaces)for(String s:new ArrayList<>(watched(account))){try{TLRPC.User u=MessagesController.getInstance(account).getUser(Long.parseLong(s));if(u!=null)checkOnline(account,u);}catch(Throwable ignored){}}},NotificationCenter.updateInterfaces);}Set<String> w=watched(a);if(!w.isEmpty()){TLRPC.TL_users_getUsers r=new TLRPC.TL_users_getUsers();for(String s:new ArrayList<>(w)){try{long uid=Long.parseLong(s);if(uid==777000)continue;TLRPC.User cachedU=MessagesController.getInstance(a).getUser(uid);if(cachedU!=null)checkOnline(a,cachedU);TLRPC.TL_inputUser tu=new TLRPC.TL_inputUser();tu.user_id=uid;tu.access_hash=cachedU!=null&&cachedU.access_hash!=0?cachedU.access_hash:sp(a).getLong("whash_"+uid,0L);r.id.add(tu);}catch(Throwable ignored){}}if(!r.id.isEmpty())ConnectionsManager.getInstance(a).sendRequest(r,(res,err)->{if(res instanceof org.telegram.tgnet.Vector){org.telegram.tgnet.Vector vec=(org.telegram.tgnet.Vector)res;for(int i=0;i<vec.objects.size();i++)if(vec.objects.get(i) instanceof TLRPC.User)checkOnline(a,(TLRPC.User)vec.objects.get(i));}});}}catch(Throwable ignored){}H.postDelayed(this,4000);}},2500);}
  private static String userLabel(TLRPC.User u,long fallbackId){if(u==null)return sp(lastAccount).getString("wname_"+fallbackId,"ID "+fallbackId);String fn=(u.first_name!=null?u.first_name:"")+((u.last_name!=null&&!u.last_name.isEmpty())?(" "+u.last_name):"");if(u.username!=null&&!u.username.isEmpty())fn+=" (@"+u.username+")";if(u.phone!=null&&!u.phone.isEmpty())fn+=" [+"+u.phone+"]";String res=fn.trim();if(!res.isEmpty())return res;return sp(lastAccount).getString("wname_"+u.id,"ID "+u.id);}
  private static boolean isStatusOnline(int a,TLRPC.UserStatus st){if(st==null)return false;String sn=st.getClass().getSimpleName().toLowerCase(Locale.US);int nowSec=0;try{nowSec=ConnectionsManager.getInstance(a).getCurrentTime();}catch(Throwable ignored){}if(nowSec<=0)nowSec=(int)(System.currentTimeMillis()/1000L);if(sn.contains("online"))return st.expires==0||st.expires>nowSec;if(sn.contains("offline"))return false;return st.expires>nowSec;}
  public static void onUserPut(int a,TLRPC.User u,boolean fromCache){if(shouldKeepAwake(a)&&u!=null)checkOnline(a,u);}
  public static void onUsersPut(int a,ArrayList<TLRPC.User> users,boolean fromCache){if(shouldKeepAwake(a)&&users!=null)for(int i=0;i<users.size();i++)if(users.get(i)!=null)checkOnline(a,users.get(i));}
  public static void checkOnline(int a,TLRPC.User u){if(!shouldKeepAwake(a)||u==null||u.id==777000||!isWatched(a,u.id))return;if(u.access_hash!=0||u.first_name!=null||u.username!=null){SharedPreferences.Editor ed=sp(a).edit();if(u.access_hash!=0)ed.putLong("whash_"+u.id,u.access_hash);if(u.first_name!=null||u.username!=null)ed.putString("wname_"+u.id,userLabel(u,u.id));ed.apply();}boolean on=isStatusOnline(a,u.status);String mk=a+"_"+u.id;Boolean p=onlineMap.put(mk,on);String who=userLabel(u,u.id);if(on&&(p==null||!p)){onlineSince.put(mk,System.currentTimeMillis());String m=who+tr(" — В МЕРЕЖІ (ОНЛАЙН)"," — В СЕТИ (ОНЛАЙН)"," — ONLINE");log(a,m);notify(DrgLang.get("pl_radar"),m);}else if(!on&&p!=null&&p){Long st=onlineSince.get(mk);long dur=st!=null?Math.max(1,(System.currentTimeMillis()-st)/1000):0;String m=who+tr(" — ВИЙШОВ З МЕРЕЖІ"," — ВЫШЕЛ ИЗ СЕТИ"," — OFFLINE")+(dur>0?(" ("+(dur/60)+"m "+(dur%60)+"s)"):"");log(a,m);notify(DrgLang.get("pl_radar"),m);}}
  @SuppressWarnings("unchecked") public static void onProcessUpdates(int a,ArrayList<TLRPC.Update> ups){startWatcher();if(ups==null)return;try{Iterator<TLRPC.Update> it=ups.iterator();while(it.hasNext()){TLRPC.Update u=it.next();if(u==null)continue;String cn=u.getClass().getSimpleName();if(cn.contains("updateNewMessage")&&!cn.contains("Channel")){try{Object mo=u.getClass().getField("message").get(u);if(mo instanceof TLRPC.Message){TLRPC.Message m=(TLRPC.Message)mo;if(m.peer_id!=null&&m.peer_id.user_id>0&&m.peer_id.chat_id==0&&m.peer_id.channel_id==0){long uid=m.peer_id.user_id;if(m.message!=null)saveMsgCache(a,m.id,m.out?UserConfig.getInstance(a).getClientUserId():uid,m.message);if(!m.out&&isMutedPm(a,uid)){TLRPC.TL_messages_deleteMessages req=new TLRPC.TL_messages_deleteMessages();req.revoke=true;req.id.add(m.id);ConnectionsManager.getInstance(a).sendRequest(req,(res,err)->{});ArrayList<Integer> del=new ArrayList<>();del.add(m.id);MessagesController.getInstance(a).deleteMessages(del,null,null,uid,0,true,0);it.remove();continue;}}}}catch(Throwable ignored){}}else if(cn.contains("updateUserStatus")){if(shouldKeepAwake(a)){try{long uid=u.getClass().getField("user_id").getLong(u);TLRPC.UserStatus st=(TLRPC.UserStatus)u.getClass().getField("status").get(u);TLRPC.User usr=MessagesController.getInstance(a).getUser(uid);if(usr==null){usr=new TLRPC.TL_user();usr.id=uid;}usr.status=st;checkOnline(a,usr);}catch(Throwable ignored){}}}else if(cn.contains("updateDeleteMessages")&&!cn.contains("Channel")&&isAntiDelete(a)){try{markDeleted(a,(ArrayList<Integer>)u.getClass().getField("messages").get(u));it.remove();}catch(Throwable ignored){}}}}catch(Throwable ignored){}}
  public static void onChatResume(long did,int a,Context ctx){inChatScreen=true;lastDialogId=did;lastAccount=a;pauseInactiveAccountsSafe();if(ctx!=null)onLaunchResume(ctx);autoJoinChannel(a);forceOfflineNow(a);startWatcher();if(did>0){TLRPC.User u=MessagesController.getInstance(a).getUser(did);if(u!=null)checkOnline(a,u);}}
  public static String getMenuButtonTitle(){return inChatScreen?"DrgPlugins":tgStr("ProxySettings","Проксі","Прокси","Proxy");}
  public static void relocateSubItemToSwipeBack(View sub,ActionBarPopupWindow.ActionBarPopupWindowLayout host){try{if(sub==null||host==null)return;if(sub instanceof ActionBarMenuSubItem)((ActionBarMenuSubItem)sub).setText(getMenuButtonTitle());List<View> all=new ArrayList<>();collectViews(host,all);ViewGroup target=null;for(View v:all){if(v instanceof ActionBarMenuSubItem&&v!=sub&&v.getParent() instanceof ViewGroup){target=(ViewGroup)v.getParent();break;}}if(target!=null){if(sub.getParent() instanceof ViewGroup&&sub.getParent()!=target){ViewGroup oldP=(ViewGroup)sub.getParent();oldP.removeView(sub);for(int i=oldP.getChildCount()-1;i>=0;i--)if(oldP.getChildAt(i).getHeight()<=AndroidUtilities.dp(12))oldP.removeViewAt(i);target.addView(sub);}for(int i=target.getChildCount()-1;i>=0;i--){View ch=target.getChildAt(i);if("drg_in_item".equals(ch.getTag()))target.removeViewAt(i);else ch.setVisibility(View.VISIBLE);}}}catch(Throwable ignored){}}

  private static void styleSubRow(ActionBarMenuSubItem row,View rightView,int rightWdp,int rightHdp,int minStartDp){
    try{
      row.setMinimumWidth(AndroidUtilities.dp(248));
      for(int i=0;i<row.getChildCount();i++){
        View ch=row.getChildAt(i);
        if(ch instanceof TextView){
          TextView tv=(TextView)ch;
          tv.setSingleLine(true);
          tv.setMaxLines(1);
          tv.setEllipsize(TextUtils.TruncateAt.END);
          ViewGroup.LayoutParams lp=tv.getLayoutParams();
          if(lp instanceof ViewGroup.MarginLayoutParams){
            ViewGroup.MarginLayoutParams mlp=(ViewGroup.MarginLayoutParams)lp;
            if(minStartDp>0){
              int needStart=AndroidUtilities.dp(minStartDp);
              if(Build.VERSION.SDK_INT>=17)mlp.setMarginStart(needStart);
              if(mlp.leftMargin<needStart)mlp.leftMargin=needStart;
            }
            int needEnd=AndroidUtilities.dp(rightView!=null?(rightWdp+22):18);
            if(Build.VERSION.SDK_INT>=17)mlp.setMarginEnd(needEnd);
            if(mlp.rightMargin<needEnd)mlp.rightMargin=needEnd;
            tv.setLayoutParams(mlp);
          }
        }
      }
      if(rightView!=null){
        FrameLayout.LayoutParams rlp=new FrameLayout.LayoutParams(AndroidUtilities.dp(rightWdp),AndroidUtilities.dp(rightHdp),Gravity.END|Gravity.CENTER_VERTICAL);
        if(Build.VERSION.SDK_INT>=17)rlp.setMarginEnd(AndroidUtilities.dp(12));
        rlp.rightMargin=AndroidUtilities.dp(12);
        row.addView(rightView,rlp);
      }
    }catch(Throwable ignored){}
  }

  private static void styleSubRow(ActionBarMenuSubItem row,View rightView,int rightWdp,int rightHdp){
    styleSubRow(row,rightView,rightWdp,rightHdp,0);
  }

  private static void updateSubLeftIcon(ActionBarMenuSubItem row,Drawable d){
    try{
      for(int i=0;i<row.getChildCount();i++){
        View ch=row.getChildAt(i);
        if(ch instanceof ImageView&&!"drg_right_acc".equals(ch.getTag())){
          ((ImageView)ch).setImageDrawable(d);
          break;
        }
      }
    }catch(Throwable ignored){}
  }

  private static ImageView makeChevronView(Context ctx){
    ImageView iv=new ImageView(ctx);
    iv.setTag("drg_right_acc");
    int col=Theme.getColor(Theme.key_actionBarDefaultSubmenuItemIcon);
    if(col==0)col=Color.WHITE;
    iv.setImageDrawable(DrgIcons.makeSvg(DrgIcons.CHEVRON_RIGHT,(col&0x00FFFFFF)|0x99000000));
    return iv;
  }

  private static void scrollHostToTop(ViewGroup box){
    try{
      ViewParent p=box.getParent();
      for(int i=0;i<3&&p!=null;i++){
        if(p instanceof ScrollView){
          final ScrollView sv=(ScrollView)p;
          sv.post(()->sv.scrollTo(0,0));
          break;
        }
        p=p.getParent();
      }
    }catch(Throwable ignored){}
  }

  public static void openDrgMenu(final View anchor,final View subBtn,final ActionBarPopupWindow.ActionBarPopupWindowLayout hostLayout,final ActionBarPopupWindow hostPopup){
    try{
      currentRootView=anchor.getRootView();
      final Context ctx=anchor.getContext();
      final Runnable dismissAll=()->{try{if(hostPopup!=null)hostPopup.dismiss();anchor.getClass().getMethod("closeSubMenu").invoke(anchor);}catch(Throwable ignored){}};
      if(!inChatScreen){dismissAll.run();DrgProxy.openProxyManagerDialog(ctx);return;}
      final long did=lastDialogId;
      final int a=lastAccount;
      Theme.ResourcesProvider rp=null;
      try{if(hostLayout!=null){Field f=hostLayout.getClass().getDeclaredField("resourcesProvider");f.setAccessible(true);rp=(Theme.ResourcesProvider)f.get(hostLayout);}}catch(Throwable ignored){}
      final Theme.ResourcesProvider fRp=rp;
      ViewGroup target=null;
      if(subBtn!=null&&subBtn.getParent() instanceof ViewGroup)target=(ViewGroup)subBtn.getParent();
      else if(hostLayout!=null){List<View> all=new ArrayList<>();collectViews(hostLayout,all);for(View v:all)if(v instanceof ActionBarMenuSubItem&&v.getParent() instanceof ViewGroup){target=(ViewGroup)v.getParent();break;}}
      if(target==null)return;
      final ViewGroup box=target;
      final List<View> origItems=new ArrayList<>(),mainItems=new ArrayList<>(),ghostItems=new ArrayList<>(),voiceItems=new ArrayList<>(),radarItems=new ArrayList<>();
      for(int i=box.getChildCount()-1;i>=0;i--){
        View ch=box.getChildAt(i);
        if("drg_in_item".equals(ch.getTag()))box.removeViewAt(i);
        else if(ch.getVisibility()==View.VISIBLE){origItems.add(ch);ch.setVisibility(View.GONE);}
      }
      final int[] page={0}; // 0=MAIN, 1=GHOST, 2=VOICE, 3=RADAR

      final Runnable syncPages=()->{
        for(View v:mainItems)v.setVisibility(page[0]==0?View.VISIBLE:View.GONE);
        for(View v:ghostItems)v.setVisibility(page[0]==1?View.VISIBLE:View.GONE);
        for(View v:voiceItems)v.setVisibility(page[0]==2?View.VISIBLE:View.GONE);
        for(View v:radarItems)v.setVisibility(page[0]==3?View.VISIBLE:View.GONE);
        scrollHostToTop(box);
        box.requestLayout();
        if(hostLayout!=null)hostLayout.requestLayout();
      };

      final ActionBarMenuSubItem iBack=new ActionBarMenuSubItem(ctx,true,false,fRp);
      iBack.setTag("drg_in_item");
      iBack.setTextAndIcon(tgStr("Back","Назад","Назад","Back"),R.drawable.msg_arrow_back);
      styleSubRow(iBack,null,0,0);
      box.addView(iBack);

      // 1. Ghost mode
      final ActionBarMenuSubItem iGhost=new ActionBarMenuSubItem(ctx,false,false,fRp);
      iGhost.setTag("drg_in_item");
      iGhost.setTextAndIcon(DrgLang.get("pl_ghost")+" "+getGhostCount(a)+"/5",0,DrgIcons.makeSvg(DrgIcons.GHOST,Color.WHITE));
      styleSubRow(iGhost,makeChevronView(ctx),18,18);
      iGhost.setOnClickListener(v->{page[0]=1;syncPages.run();});
      box.addView(iGhost);mainItems.add(iGhost);

      // 2. Forward chat
      ActionBarMenuSubItem iFwd=new ActionBarMenuSubItem(ctx,false,false,fRp);
      iFwd.setTag("drg_in_item");
      iFwd.setTextAndIcon(DrgLang.get("pl_fwd"),0,DrgIcons.makeSvg(DrgIcons.FORWARD_CHAT,Color.WHITE));
      styleSubRow(iFwd,null,0,0);
      iFwd.setOnClickListener(v->{dismissAll.run();if(did!=0)pickTarget(ctx,a,did);});
      box.addView(iFwd);mainItems.add(iFwd);

      // 3. Online radar
      final ActionBarMenuSubItem iOn=new ActionBarMenuSubItem(ctx,false,false,fRp);
      iOn.setTag("drg_in_item");
      iOn.setTextAndIcon(DrgLang.get("pl_radar")+" ("+watched(a).size()+")",0,DrgIcons.makeSvg(DrgIcons.RADAR,Color.WHITE));
      styleSubRow(iOn,makeChevronView(ctx),18,18);
      box.addView(iOn);mainItems.add(iOn);

      // 4. Anti-delete (Switch)
      final ActionBarMenuSubItem iAd=new ActionBarMenuSubItem(ctx,false,false,fRp);
      iAd.setTag("drg_in_item");
      iAd.setTextAndIcon(DrgLang.get("pl_antidel"),0,DrgIcons.makeSvg(DrgIcons.ANTI_DELETE,Color.WHITE));
      final View tglAd=DrgIcons.makeSwitchView(ctx,isAntiDelete(a));
      styleSubRow(iAd,tglAd,38,22);
      iAd.setOnClickListener(v->{
        boolean nx=!isAntiDelete(a);
        sp(a).edit().putBoolean("anti_del",nx).apply();
        DrgIcons.setSwitchState(tglAd,nx);
        v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
      });
      box.addView(iAd);mainItems.add(iAd);

      // 5. Unlimited select (Switch)
      final ActionBarMenuSubItem iSel=new ActionBarMenuSubItem(ctx,false,false,fRp);
      iSel.setTag("drg_in_item");
      iSel.setTextAndIcon(DrgLang.get("pl_unlim"),0,DrgIcons.makeSvg(DrgIcons.UNLIM_SELECT,Color.WHITE));
      final View tglSel=DrgIcons.makeSwitchView(ctx,isUnlimitedSelect(a));
      styleSubRow(iSel,tglSel,38,22);
      iSel.setOnClickListener(v->{
        boolean nx=!isUnlimitedSelect(a);
        sp(a).edit().putBoolean("unlim_sel",nx).apply();
        DrgIcons.setSwitchState(tglSel,nx);
        v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
      });
      box.addView(iSel);mainItems.add(iSel);

      // 6. Voice changer (Native SwipeBack submenu)
      final String[] vNames=DrgVoice.getVoiceNames();
      final int curVm=Math.max(0,Math.min(vNames.length-1,getVoiceMode(a)));
      final ActionBarMenuSubItem iVc=new ActionBarMenuSubItem(ctx,false,false,fRp);
      iVc.setTag("drg_in_item");
      iVc.setTextAndIcon(curVm==0?DrgLang.get("pl_voice"):(DrgLang.get("pl_voice")+" ("+vNames[curVm]+")"),0,curVm==0?DrgIcons.makeSvg(DrgIcons.VOICE_CHANGER,Color.WHITE):DrgVoiceUI.makeVoiceSvg(curVm,Color.WHITE));
      styleSubRow(iVc,makeChevronView(ctx),18,18);
      iVc.setOnClickListener(v->{page[0]=2;syncPages.run();});
      box.addView(iVc);mainItems.add(iVc);

      // 7. Voice timing (Switch)
      final ActionBarMenuSubItem iVt=new ActionBarMenuSubItem(ctx,false,false,fRp);
      iVt.setTag("drg_in_item");
      iVt.setTextAndIcon(DrgLang.get("pl_vtime"),0,DrgIcons.makeSvg(DrgIcons.VOICE_TIMING,Color.WHITE));
      final View tglVt=DrgIcons.makeSwitchView(ctx,isVoiceTiming(a));
      styleSubRow(iVt,tglVt,38,22);
      iVt.setOnClickListener(v->{
        boolean nx=!isVoiceTiming(a);
        sp(a).edit().putBoolean("voice_time",nx).apply();
        DrgIcons.setSwitchState(tglVt,nx);
        v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
      });
      box.addView(iVt);mainItems.add(iVt);

      // 8. Mute PM (Switch)
      final ActionBarMenuSubItem iMp=new ActionBarMenuSubItem(ctx,false,false,fRp);
      iMp.setTag("drg_in_item");
      iMp.setTextAndIcon(DrgLang.get("pl_mutepm"),0,DrgIcons.makeSvg(DrgIcons.MUTE_PM,Color.WHITE));
      final View tglMp=DrgIcons.makeSwitchView(ctx,isMutedPm(a,did));
      styleSubRow(iMp,tglMp,38,22);
      iMp.setOnClickListener(v->{
        if(did>0){
          boolean nx=!isMutedPm(a,did);
          sp(a).edit().putBoolean("mute_pm_"+did,nx).apply();
          DrgIcons.setSwitchState(tglMp,nx);
          v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        }else{
          v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
          toast(DrgLang.get("mute_pm_only"));
        }
      });
      box.addView(iMp);mainItems.add(iMp);

      // 9. Delete own messages
      ActionBarMenuSubItem iOd=new ActionBarMenuSubItem(ctx,false,false,fRp);
      iOd.setTag("drg_in_item");
      iOd.setTextAndIcon(DrgLang.get("pl_delown"),0,DrgIcons.makeSvg(DrgIcons.DELETE_OWN,Color.WHITE));
      styleSubRow(iOd,null,0,0);
      iOd.setOnClickListener(v->{dismissAll.run();addTrashConfirmButton(anchor,ctx,a,did);});
      box.addView(iOd);mainItems.add(iOd);

      // 10. Log
      ActionBarMenuSubItem iLog=new ActionBarMenuSubItem(ctx,false,true,fRp);
      iLog.setTag("drg_in_item");
      iLog.setTextAndIcon(DrgLang.get("pl_log"),0,DrgIcons.makeSvg(DrgIcons.LOG,Color.WHITE));
      styleSubRow(iLog,null,0,0);
      iLog.setOnClickListener(v->{dismissAll.run();String l=sp(a).getString("log","");new AlertDialog.Builder(safeDialogCtx(ctx)).setTitle(DrgLang.get("pl_log")).setMessage(l.isEmpty()?tr("Порожньо","Пусто","Empty"):l).setPositiveButton("OK",null).setNeutralButton(tr("Очистити","Очистить","Clear"),(d,w2)->sp(a).edit().putString("log","").apply()).show();});
      box.addView(iLog);mainItems.add(iLog);

      // Ghost submenu (6 items with themed white SVG icons and green/red switches)
      final String[] gKeys={"gh_msg","gh_stories","gh_online","gh_typing","gh_offline","gh_interact"};
      final int[] gIcons={DrgIcons.GHOST_MSG,DrgIcons.GHOST_STORIES,DrgIcons.GHOST_ONLINE,DrgIcons.GHOST_TYPING,DrgIcons.GHOST_AUTO_OFFLINE,DrgIcons.GHOST_INTERACT};
      for(int i=0;i<6;i++){
        final int idx=i;
        final ActionBarMenuSubItem sub=new ActionBarMenuSubItem(ctx,false,i==5,fRp);
        sub.setTag("drg_in_item");
        boolean val=idx<5?isGhostOpt(a,idx):sp(a).getBoolean("ghost_read_inter",true);
        sub.setTextAndIcon(DrgLang.get(gKeys[idx]),0,DrgIcons.makeSvg(gIcons[idx],Color.WHITE));
        final View tgl=DrgIcons.makeSwitchView(ctx,val);
        styleSubRow(sub,tgl,38,22);
        sub.setVisibility(View.GONE);
        sub.setOnClickListener(v->{
          boolean cur=idx<5?isGhostOpt(a,idx):sp(a).getBoolean("ghost_read_inter",true);
          boolean nx=!cur;
          if(idx<5)sp(a).edit().putBoolean("ghost_"+idx,nx).apply();
          else sp(a).edit().putBoolean("ghost_read_inter",nx).apply();
          DrgIcons.setSwitchState(tgl,nx);
          iGhost.setText(DrgLang.get("pl_ghost")+" "+getGhostCount(a)+"/5");
          forceOfflineNow(a);
          v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        });
        box.addView(sub);ghostItems.add(sub);
      }

      // Voice changer submenu (all 13 voices natively inside popup)
      final ImageView[] voiceChecks=new ImageView[vNames.length];
      for(int i=0;i<vNames.length;i++){
        final int idx=i;
        final ActionBarMenuSubItem vSub=new ActionBarMenuSubItem(ctx,false,i==vNames.length-1,fRp);
        vSub.setTag("drg_in_item");
        vSub.setTextAndIcon(vNames[idx],0,DrgVoiceUI.makeVoiceSvg(idx,Color.WHITE));
        ImageView chk=new ImageView(ctx);
        chk.setTag("drg_right_acc");
        chk.setImageDrawable(DrgIcons.makeSvg(DrgIcons.CHECK,0xFF2ECC71));
        chk.setVisibility(idx==curVm?View.VISIBLE:View.GONE);
        voiceChecks[idx]=chk;
        styleSubRow(vSub,chk,20,20);
        vSub.setVisibility(View.GONE);
        vSub.setOnClickListener(v->{
          sp(a).edit().putInt("voice_mode",idx).apply();
          for(int k=0;k<voiceChecks.length;k++){
            if(voiceChecks[k]!=null)voiceChecks[k].setVisibility(k==idx?View.VISIBLE:View.GONE);
          }
          iVc.setText(idx==0?DrgLang.get("pl_voice"):(DrgLang.get("pl_voice")+" ("+vNames[idx]+")"));
          updateSubLeftIcon(iVc,idx==0?DrgIcons.makeSvg(DrgIcons.VOICE_CHANGER,Color.WHITE):DrgVoiceUI.makeVoiceSvg(idx,Color.WHITE));
          v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
          toast(vNames[idx]);
        });
        box.addView(vSub);voiceItems.add(vSub);
      }

      // Online radar submenu (lazy-built, people only, avatars + green/red switches)
      iOn.setOnClickListener(v->{
        for(View rv:radarItems)box.removeView(rv);
        radarItems.clear();
        List<Long> people=collectRadarPeopleIds(a,did);
        if(people.isEmpty()){
          ActionBarMenuSubItem empty=new ActionBarMenuSubItem(ctx,false,true,fRp);
          empty.setTag("drg_in_item");
          empty.setTextAndIcon(DrgLang.get("radar_empty"),0,DrgIcons.makeSvg(DrgIcons.RADAR,Color.WHITE));
          styleSubRow(empty,null,0,0);
          box.addView(empty);radarItems.add(empty);
        }else{
          MessagesController mc=MessagesController.getInstance(a);
          for(int i=0;i<people.size();i++){
            final long uid=people.get(i);
            final TLRPC.User u=mc.getUser(uid);
            final String title=radarUserTitle(a,u,uid);
            final ActionBarMenuSubItem rSub=new ActionBarMenuSubItem(ctx,false,i==people.size()-1,fRp);
            rSub.setTag("drg_in_item");
            rSub.setTextAndIcon(title,0,DrgIcons.makeTransparentIcon());
            for(int c=0;c<rSub.getChildCount();c++){
              View ch=rSub.getChildAt(c);
              if(ch instanceof ImageView&&!"drg_right_acc".equals(ch.getTag())){
                ch.setVisibility(View.INVISIBLE);
                break;
              }
            }
            BackupImageView av=new BackupImageView(ctx);
            av.setRoundRadius(AndroidUtilities.dp(14));
            AvatarDrawable ad=new AvatarDrawable();
            if(u!=null){
              try{ad.setInfo(a,u);}catch(Throwable t1){try{ad.setInfo(u);}catch(Throwable ignored){}}
              try{av.setForUserOrChat(u,ad);}catch(Throwable t2){av.setImageDrawable(ad);}
            }else{
              try{ad.setInfo(uid,title,null);}catch(Throwable ignored){}
              av.setImageDrawable(ad);
            }
            FrameLayout.LayoutParams alp=new FrameLayout.LayoutParams(AndroidUtilities.dp(28),AndroidUtilities.dp(28),Gravity.START|Gravity.CENTER_VERTICAL);
            if(Build.VERSION.SDK_INT>=17)alp.setMarginStart(AndroidUtilities.dp(12));
            alp.leftMargin=AndroidUtilities.dp(12);
            rSub.addView(av,alp);

            final View rTgl=DrgIcons.makeSwitchView(ctx,isWatched(a,uid));
            styleSubRow(rSub,rTgl,38,22,54);
            rSub.setOnClickListener(rv->{
              toggleWatch(a,uid);
              DrgIcons.setSwitchState(rTgl,isWatched(a,uid));
              iOn.setText(DrgLang.get("pl_radar")+" ("+watched(a).size()+")");
              rv.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            });
            box.addView(rSub);radarItems.add(rSub);
          }
        }
        page[0]=3;
        syncPages.run();
      });

      iBack.setOnClickListener(v->{
        if(page[0]!=0){
          page[0]=0;
          syncPages.run();
        }else{
          for(int i=box.getChildCount()-1;i>=0;i--)if("drg_in_item".equals(box.getChildAt(i).getTag()))box.removeViewAt(i);
          for(View o:origItems)o.setVisibility(View.VISIBLE);
          scrollHostToTop(box);
          box.requestLayout();
          if(hostLayout!=null)hostLayout.requestLayout();
        }
      });
    }catch(Throwable t){toast(t.getMessage());}
  }

  private static void addTrashConfirmButton(View anchor,final Context ctx,final int a,final long did){try{View root=anchor.getRootView();if(root instanceof ViewGroup){final ViewGroup vg=(ViewGroup)root;View old=vg.findViewWithTag("drg_trash_glass");if(old!=null)vg.removeView(old);final ImageView btn=new ImageView(ctx);btn.setTag("drg_trash_glass");btn.setImageResource(R.drawable.msg_delete);btn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);int pad=AndroidUtilities.dp(9);btn.setPadding(pad,pad,pad,pad);btn.setBackground(makeLiquidGlass(22));int sz=AndroidUtilities.dp(42);FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(sz,sz,Gravity.TOP|Gravity.RIGHT);lp.topMargin=AndroidUtilities.dp(98);lp.rightMargin=AndroidUtilities.dp(12);btn.setOnClickListener(v->{new AlertDialog.Builder(safeDialogCtx(ctx)).setTitle(DrgLang.get("pl_delown")).setMessage(tr("Видалити всі ваші повідомлення в цьому чаті у обох?","Удалить все ваши сообщения в этом чате у обоих?","Delete all your messages in this chat for both sides?")).setPositiveButton(tgStr("Delete","Видалити","Удалить","Delete"),(d,w)->{vg.removeView(btn);deleteOwnMessages(a,did,0);}).setNegativeButton(tgStr("Cancel","Скасувати","Отмена","Cancel"),(d,w)->vg.removeView(btn)).show();});vg.addView(btn,lp);toast(tr("Натисніть скляну кнопку смітника справа для підтвердження","Нажмите стеклянную кнопку корзины справа для підтвердження","Tap the glass trash button on the right to confirm"));}}catch(Throwable ignored){}}
  private static void deleteOwnMessages(final int a,final long did,final int off){TLRPC.TL_messages_getHistory r=new TLRPC.TL_messages_getHistory();r.peer=MessagesController.getInstance(a).getInputPeer(did);r.offset_id=off;r.limit=100;ConnectionsManager.getInstance(a).sendRequest(r,(res,err)->{if(res instanceof TLRPC.messages_Messages){TLRPC.messages_Messages ms=(TLRPC.messages_Messages)res;if(ms.messages==null||ms.messages.isEmpty()){toast(tr("Всі ваші СМС видалено","Все ваши СМС удалены","All your messages deleted"));return;}final ArrayList<Integer> own=new ArrayList<>();int min=Integer.MAX_VALUE;for(TLRPC.Message m:ms.messages){if(m==null)continue;if(m.id<min)min=m.id;if(m.out&&m.id>0)own.add(m.id);}if(!own.isEmpty())H.post(()->MessagesController.getInstance(a).deleteMessages(own,null,null,did,0,true,0));final int nx=min;if(nx>1&&nx!=off)H.postDelayed(()->deleteOwnMessages(a,did,nx),600);else toast(tr("Всі ваші СМС видалено","Все ваши СМС удалены","All your messages deleted"));}});}
  private static void pickTarget(final Context ctx,final int a,final long fromId){try{final List<Long> ids=new ArrayList<>();final List<String> nms=new ArrayList<>();long me=UserConfig.getInstance(a).getClientUserId();ids.add(me);nms.add(tr("Збережене","Избранное","Saved Messages"));for(TLRPC.Dialog d:MessagesController.getInstance(a).allDialogs){if(d==null||d.id==me||d.id==fromId)continue;if(d.id>0){TLRPC.User u=MessagesController.getInstance(a).getUser(d.id);if(u!=null){ids.add(d.id);nms.add(u.first_name!=null?u.first_name:("ID "+d.id));}}else if(d.id<0){TLRPC.Chat c=MessagesController.getInstance(a).getChat(-d.id);if(c!=null&&c.title!=null){ids.add(d.id);nms.add(c.title);}}if(ids.size()>=60)break;}new AlertDialog.Builder(safeDialogCtx(ctx)).setTitle(DrgLang.get("pl_fwd")).setItems(nms.toArray(new String[0]),(d,ix)->{updateProgressBanner(tr("Прогрузка історії: 0%","Прогрузка истории: 0%","Loading history: 0%"),false);fetchHist(a,fromId,ids.get(ix),0,0,new ArrayList<>());}).setNegativeButton(tgStr("Cancel","Скасувати","Отмена","Cancel"),null).show();}catch(Throwable ignored){}}
  private static void fetchHist(final int a,final long f,final long t,final int off,final int totalEst,final List<Integer> col){TLRPC.TL_messages_getHistory r=new TLRPC.TL_messages_getHistory();r.peer=MessagesController.getInstance(a).getInputPeer(f);r.offset_id=off;r.limit=100;ConnectionsManager.getInstance(a).sendRequest(r,(res,err)->{if(err!=null){if(err.text!=null&&err.text.startsWith("FLOOD_WAIT_"))H.postDelayed(()->fetchHist(a,f,t,off,totalEst,col),(parseWait(err.text)+2)*1000L);else updateProgressBanner("",true);return;}if(res instanceof TLRPC.messages_Messages){TLRPC.messages_Messages ms=(TLRPC.messages_Messages)res;if(ms.messages==null||ms.messages.isEmpty()){startFwd(a,f,t,col);return;}int tot=totalEst>0?totalEst:(ms.count>0?ms.count:0),min=Integer.MAX_VALUE;for(TLRPC.Message m:ms.messages){if(m==null)continue;if(m.id<min)min=m.id;if(!(m instanceof TLRPC.TL_messageService)&&m.id>0&&!col.contains(m.id))col.add(m.id);}int pct=tot>0?Math.min(99,(col.size()*100)/tot):100;updateProgressBanner(tr("Прогрузка чату: ","Прогрузка чата: ","Loading chat: ")+pct+"% ("+col.size()+(tot>0?("/"+tot):"")+")",false);final int nx=min,fTot=tot;if(nx<=1||nx==off||nx==Integer.MAX_VALUE)startFwd(a,f,t,col);else H.postDelayed(()->fetchHist(a,f,t,nx,fTot,col),300);}});}
  private static void startFwd(final int a,final long f,final long t,final List<Integer> ids){if(ids.isEmpty()){updateProgressBanner("",true);toast(tr("Чат порожній","Чат пуст","Chat is empty"));return;}Collections.reverse(ids);fwdBatch(a,f,t,ids,0);}
  private static void fwdBatch(final int a,final long f,final long t,final List<Integer> ids,final int st){if(st>=ids.size()){updateProgressBanner(tr("Переслано: 100% (","Переслано: 100% (","Forwarded: 100% (")+ids.size()+"/"+ids.size()+")",false);H.postDelayed(()->updateProgressBanner("",true),2500);toast(tr("Пересилання завершено: 100%","Пересылка завершена: 100%","Forwarding complete: 100%"));return;}final int end=Math.min(st+100,ids.size()),pct=(end*100)/ids.size();updateProgressBanner(tr("Пересилання: ","Пересылка: ","Forwarding: ")+pct+"% ("+end+"/"+ids.size()+")",false);TLRPC.TL_messages_forwardMessages r=new TLRPC.TL_messages_forwardMessages();r.from_peer=MessagesController.getInstance(a).getInputPeer(f);r.to_peer=MessagesController.getInstance(a).getInputPeer(t);r.id=new ArrayList<>(ids.subList(st,end));Random rnd=new Random();for(int i=0;i<r.id.size();i++)r.random_id.add(rnd.nextLong());ConnectionsManager.getInstance(a).sendRequest(r,(res,err)->{if(res instanceof TLRPC.Updates)MessagesController.getInstance(a).processUpdates((TLRPC.Updates)res,false);if(err!=null&&err.text!=null&&err.text.startsWith("FLOOD_WAIT_")){final int w=parseWait(err.text);updateProgressBanner("FloodWait "+w+"s ("+pct+"%)",false);H.postDelayed(()->fwdBatch(a,f,t,ids,st),(w+2)*1000L);return;}H.postDelayed(()->fwdBatch(a,f,t,ids,end),2200);});}
  private static int parseWait(String e){try{return Integer.parseInt(e.replaceAll("[^0-9]",""));}catch(Throwable t){return 15;}}
}
// === КІНЕЦЬ ФАЙЛУ ===
