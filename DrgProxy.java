package org.telegram.messenger;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestTimeDelegate;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.ProxyListActivity;
import java.io.*;
import java.lang.reflect.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.*;

public class DrgProxy {
  private static final Map<String,String> countryCache=Collections.synchronizedMap(new HashMap<>());

  public static Drawable makeSvg(final int type,final int color){
    return new Drawable(){
      private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
      {p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(AndroidUtilities.dp(1.8f));p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);}
      @Override public void draw(Canvas c){
        Rect b=getBounds();float cx=b.exactCenterX(),cy=b.exactCenterY(),r=Math.min(b.width(),b.height())*0.38f;
        Path path=new Path();
        if(type==0){// Shield SVG
          path.moveTo(cx,cy-r);path.lineTo(cx+r*0.85f,cy-r*0.55f);path.lineTo(cx+r*0.85f,cy+r*0.1f);
          path.quadTo(cx+r*0.85f,cy+r*0.8f,cx,cy+r);path.quadTo(cx-r*0.85f,cy+r*0.8f,cx-r*0.85f,cy+r*0.1f);
          path.lineTo(cx-r*0.85f,cy-r*0.55f);path.close();c.drawPath(path,p);
        }else if(type==1){// Refresh SVG
          RectF rf=new RectF(cx-r*0.85f,cy-r*0.85f,cx+r*0.85f,cy+r*0.85f);
          c.drawArc(rf,30,290,false,p);
          path.moveTo(cx+r*0.35f,cy-r*0.95f);path.lineTo(cx+r*0.85f,cy-r*0.45f);path.lineTo(cx+r*0.25f,cy-r*0.35f);c.drawPath(path,p);
        }else if(type==2){// Trash SVG
          c.drawLine(cx-r*0.8f,cy-r*0.5f,cx+r*0.8f,cy-r*0.5f,p);
          c.drawLine(cx-r*0.3f,cy-r*0.8f,cx+r*0.3f,cy-r*0.8f,p);
          RectF rf=new RectF(cx-r*0.6f,cy-r*0.5f,cx+r*0.6f,cy+r*0.85f);c.drawRoundRect(rf,AndroidUtilities.dp(2),AndroidUtilities.dp(2),p);
        }else if(type==4){// Info (i) SVG
          c.drawCircle(cx,cy,r*0.95f,p);
          c.drawLine(cx,cy-r*0.05f,cx,cy+r*0.48f,p);
          Paint fp=new Paint(Paint.ANTI_ALIAS_FLAG);fp.setColor(color);fp.setStyle(Paint.Style.FILL);
          c.drawCircle(cx,cy-r*0.42f,AndroidUtilities.dp(1.3f),fp);
        }
      }
      @Override public void setAlpha(int a){p.setAlpha(a);}
      @Override public void setColorFilter(ColorFilter cf){p.setColorFilter(cf);}
      @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
      @Override public int getIntrinsicWidth(){return AndroidUtilities.dp(24);}
      @Override public int getIntrinsicHeight(){return AndroidUtilities.dp(24);}
    };
  }

  private static GradientDrawable cardBg(int color,int radDp,int strokeCol){
    GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(AndroidUtilities.dp(radDp));
    if(strokeCol!=0)g.setStroke(Math.max(1,AndroidUtilities.dp(0.8f)),strokeCol);
    return g;
  }

  public static void onPopupShown(final ActionBarPopupWindow pw){
    if(pw==null||DrgPluginManager.inChatScreen)return;
    DrgPluginManager.H.post(()->{
      try{
        View cv=pw.getContentView();if(!(cv instanceof ViewGroup))return;
        List<View> all=new ArrayList<>();collect(cv,all);
        ViewGroup target=null;
        for(View v:all){
          if("drg_proxy_sub".equals(v.getTag()))return;
          if(v instanceof ActionBarMenuSubItem&&v.getParent() instanceof ViewGroup)target=(ViewGroup)v.getParent();
        }
        if(target==null)return;
        Context ctx=target.getContext();
        ActionBarMenuSubItem item=new ActionBarMenuSubItem(ctx,false,true);
        item.setTag("drg_proxy_sub");
        int icCol=Theme.getColor(Theme.key_actionBarDefaultSubmenuItemIcon);
        item.setTextAndIcon(tgStr("ProxySettings","Проксі","Прокси","Proxy"),0,makeSvg(0,icCol));
        item.setOnClickListener(v->{try{pw.dismiss();}catch(Throwable ignored){}openProxyManagerDialog(ctx);});
        target.addView(item);
      }catch(Throwable ignored){}
    });
  }

  public static void openProxyManagerDialog(final Context ctx){
    try{
      BaseFragment f=LaunchActivity.getLastFragment();
      if(f!=null){f.presentFragment(new ProxyListActivity());return;}
    }catch(Throwable ignored){}
  }

  public static void attachToNativeProxyList(final Object frag,final View root){
    if(!(frag instanceof BaseFragment)||!(root instanceof ViewGroup))return;
    final BaseFragment bf=(BaseFragment)frag;
    final ViewGroup vg=(ViewGroup)root;
    final Context ctx=vg.getContext();
    DrgPluginManager.H.postDelayed(new Runnable(){
      @Override public void run(){
        try{
          if(!vg.isAttachedToWindow())return;
          List<View> all=new ArrayList<>();collect(vg,all);
          String connTitle=tgStr("ProxyConnections","З'єднання","Соединения","Connections");
          for(View v:all){
            if(v instanceof HeaderCell){
              HeaderCell hc=(HeaderCell)v;
              CharSequence cs=hc.getTextView()!=null?hc.getTextView().getText():null;
              if(cs!=null&&(cs.toString().equalsIgnoreCase(connTitle)||cs.toString().contains("З'єднання")||cs.toString().contains("Соединения")||cs.toString().contains("Connections"))){
                if(hc.findViewWithTag("drg_hdr_actions")==null){
                  LinearLayout bar=new LinearLayout(ctx);bar.setTag("drg_hdr_actions");bar.setOrientation(LinearLayout.HORIZONTAL);bar.setGravity(Gravity.CENTER_VERTICAL);
                  int blue=Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader);
                  TextView btnInfo=new TextView(ctx);btnInfo.setText(tr("Країна / Інфо","Страна / Инфо","Country / Info"));
                  btnInfo.setTextColor(blue);btnInfo.setTextSize(TypedValue.COMPLEX_UNIT_DIP,13.5f);btnInfo.setPadding(AndroidUtilities.dp(8),AndroidUtilities.dp(4),AndroidUtilities.dp(8),AndroidUtilities.dp(4));
                  btnInfo.setOnClickListener(clk->openSelectProxyForInfo(ctx,bf));
                  bar.addView(btnInfo);

                  TextView btnDel=new TextView(ctx);btnDel.setText(tgStr("Delete","Видалити","Удалить","Delete"));
                  btnDel.setTextColor(0xFFE56555);btnDel.setTextSize(TypedValue.COMPLEX_UNIT_DIP,13.5f);btnDel.setPadding(AndroidUtilities.dp(8),AndroidUtilities.dp(4),AndroidUtilities.dp(8),AndroidUtilities.dp(4));
                  btnDel.setOnClickListener(clk->{
                    int rem=0;
                    for(int i=SharedConfig.proxyList.size()-1;i>=0;i--){
                      SharedConfig.ProxyInfo p=SharedConfig.proxyList.get(i);
                      if(!pBool(p,"available")&&pLong(p,"ping")<=0&&SharedConfig.currentProxy!=p){SharedConfig.deleteProxy(p);rem++;}
                    }
                    DrgPluginManager.toast(tgStr("Delete","Видалити","Удалить","Delete")+" ("+tgStr("Unavailable","Недоступний","Недоступен","Unavailable")+"): "+rem);
                    refreshNativeProxyList(bf);
                  });
                  bar.addView(btnDel);

                  TextView btnRef=new TextView(ctx);btnRef.setText(tgStr("Refresh","Оновити","Обновить","Refresh"));
                  btnRef.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));btnRef.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14);btnRef.setPadding(AndroidUtilities.dp(8),AndroidUtilities.dp(4),AndroidUtilities.dp(14),AndroidUtilities.dp(4));
                  btnRef.setOnClickListener(clk->{
                    for(SharedConfig.ProxyInfo p:SharedConfig.proxyList){
                      pSet(p,Boolean.FALSE,"available");pSet(p,Long.valueOf(0L),"ping");pSet(p,Long.valueOf(0L),"addressCheckTime");pSet(p,Integer.valueOf(0),"proxyCheckPingId");
                      pingProxy(p,()->refreshNativeProxyList(bf));
                    }
                    refreshNativeProxyList(bf);
                  });
                  bar.addView(btnRef);

                  FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-2,-2,Gravity.RIGHT|Gravity.BOTTOM);
                  lp.bottomMargin=AndroidUtilities.dp(2);
                  hc.addView(bar,lp);
                }
              }
            }
          }
          DrgPluginManager.H.postDelayed(this,600);
        }catch(Throwable ignored){}
      }
    },150);
  }

  private static void refreshNativeProxyList(BaseFragment bf){
    try{
      for(Method m:bf.getClass().getDeclaredMethods()){
        if("updateRows".equals(m.getName())||"checkProxyList".equals(m.getName())){
          m.setAccessible(true);
          if(m.getParameterTypes().length==1)m.invoke(bf,Boolean.TRUE);
          else if(m.getParameterTypes().length==0)m.invoke(bf);
        }
      }
      NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.proxySettingsChanged);
    }catch(Throwable ignored){}
  }

  private static void openSelectProxyForInfo(final Context ctx,final BaseFragment bf){
    try{
      SharedConfig.loadProxyList();
      final ArrayList<SharedConfig.ProxyInfo> list=new ArrayList<>(SharedConfig.proxyList);
      if(list.isEmpty()){DrgPluginManager.toast(tr("Список проксі порожній","Список прокси пуст","Proxy list is empty"));return;}
      if(list.size()==1){openProxyDetailSheet(ctx,list.get(0),()->refreshNativeProxyList(bf));return;}
      String[] items=new String[list.size()];
      for(int i=0;i<list.size();i++){
        SharedConfig.ProxyInfo p=list.get(i);
        boolean av=pBool(p,"available");long pg=pLong(p,"ping");
        items[i]=pAddr(p)+":"+pPort(p)+"  ("+(av?(pg+" ms"):tgStr("Unavailable","Недоступний","Недоступен","Unavailable"))+")";
      }
      new AlertDialog.Builder(ctx).setTitle(tgStr("Proxy","Проксі-сервер","Прокси-сервер","Proxy Server")).setItems(items,(d,w)->openProxyDetailSheet(ctx,list.get(w),()->refreshNativeProxyList(bf))).setNegativeButton(tgStr("Close","Закрити","Закрыть","Close"),null).show();
    }catch(Throwable ignored){}
  }

  private static void collect(View v,List<View> out){if(v==null)return;out.add(v);if(v instanceof ViewGroup){ViewGroup vg=(ViewGroup)v;for(int i=0;i<vg.getChildCount();i++)collect(vg.getChildAt(i),out);}}
  private static Object getSettingsObj(Object p){if(p==null)return null;try{for(Field f:p.getClass().getDeclaredFields()){if(f.getType().getSimpleName().contains("ProxySettings")){f.setAccessible(true);Object v=f.get(p);if(v!=null)return v;}}}catch(Throwable ignored){}return p;}
  private static String pStr(Object p,String... nms){if(p==null)return "";for(Object t:new Object[]{p,getSettingsObj(p)}){if(t==null)continue;for(String n:nms){try{Field f=t.getClass().getDeclaredField(n);f.setAccessible(true);Object v=f.get(t);if(v!=null)return v.toString();}catch(Throwable ignored){}}}return "";}
  private static int pInt(Object p,String... nms){if(p==null)return 1080;for(Object t:new Object[]{p,getSettingsObj(p)}){if(t==null)continue;for(String n:nms){try{Field f=t.getClass().getDeclaredField(n);f.setAccessible(true);return f.getInt(t);}catch(Throwable ignored){}}}return 1080;}
  private static long pLong(Object p,String... nms){if(p==null)return 0L;for(Object t:new Object[]{p,getSettingsObj(p)}){if(t==null)continue;for(String n:nms){try{Field f=t.getClass().getDeclaredField(n);f.setAccessible(true);return f.getLong(t);}catch(Throwable ignored){}}}return 0L;}
  private static boolean pBool(Object p,String... nms){if(p==null)return false;for(Object t:new Object[]{p,getSettingsObj(p)}){if(t==null)continue;for(String n:nms){try{Field f=t.getClass().getDeclaredField(n);f.setAccessible(true);return f.getBoolean(t);}catch(Throwable ignored){}}}return false;}
  private static void pSet(Object p,Object val,String... nms){if(p==null)return;for(Object t:new Object[]{p,getSettingsObj(p)}){if(t==null)continue;for(String n:nms){try{Field f=t.getClass().getDeclaredField(n);f.setAccessible(true);f.set(t,val);return;}catch(Throwable ignored){}}}}
  private static String pAddr(Object p){return pStr(p,"address","ip","host","server");}
  private static int pPort(Object p){return pInt(p,"port");}
  private static String pUser(Object p){return pStr(p,"username","user");}
  private static String pPass(Object p){return pStr(p,"password","pass");}
  private static String pSec(Object p){return pStr(p,"secret");}
  private static String tgStr(String k,String uk,String ru,String en){return DrgPluginManager.tgStr(k,uk,ru,en);}
  private static String tr(String uk,String ru,String en){return DrgPluginManager.tr(uk,ru,en);}

  public static void applyProxyNow(SharedConfig.ProxyInfo p,boolean en){
    try{
      SharedPreferences.Editor ed=MessagesController.getGlobalMainSettings().edit();
      ed.putBoolean("proxy_enabled",en);
      if(p!=null){SharedConfig.currentProxy=p;ed.putString("proxy_ip",pAddr(p)).putInt("proxy_port",pPort(p)).putString("proxy_pass",pPass(p)).putString("proxy_user",pUser(p)).putString("proxy_secret",pSec(p));}
      ed.apply();
      for(Method m:ConnectionsManager.class.getDeclaredMethods()){
        if("setProxySettings".equals(m.getName())){
          m.setAccessible(true);
          if(m.getParameterTypes().length==2){m.invoke(null,en,en&&p!=null?getSettingsObj(p):null);break;}
          else if(m.getParameterTypes().length==6){
            if(en&&p!=null)m.invoke(null,true,pAddr(p),pPort(p),pUser(p),pPass(p),pSec(p));
            else m.invoke(null,false,"",1080,"","","");
            break;
          }
        }
      }
      NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.proxySettingsChanged);
    }catch(Throwable ignored){}
  }

  public static void pingProxy(final SharedConfig.ProxyInfo p,final Runnable onDone){
    if(p==null)return;pSet(p,Boolean.TRUE,"checking");final int a=UserConfig.selectedAccount;
    final RequestTimeDelegate del=time->DrgPluginManager.H.post(()->{
      pSet(p,Boolean.FALSE,"checking");
      if(time>0){pSet(p,Boolean.TRUE,"available");pSet(p,Long.valueOf(time),"ping");}
      else{pSet(p,Boolean.FALSE,"available");pSet(p,Long.valueOf(0L),"ping");}
      if(onDone!=null)onDone.run();
    });
    try{
      ConnectionsManager cm=ConnectionsManager.getInstance(a);
      for(Method m:ConnectionsManager.class.getDeclaredMethods()){
        if("checkProxy".equals(m.getName())){
          m.setAccessible(true);
          if(m.getParameterTypes().length==2){m.invoke(cm,getSettingsObj(p),del);return;}
          if(m.getParameterTypes().length==6){m.invoke(cm,pAddr(p),pPort(p),pUser(p),pPass(p),pSec(p),del);return;}
        }
      }
    }catch(Throwable ignored){pSet(p,Boolean.FALSE,"checking");if(onDone!=null)DrgPluginManager.H.post(onDone);}
  }

  private static void fetchCountryAsync(final String addr,final TextView tv){if(addr==null||addr.isEmpty())return;String cached=countryCache.get(addr);if(cached!=null){tv.setText(cached);return;}new Thread(()->{String cName="—";try{HttpURLConnection con=(HttpURLConnection)new URL("http://ip-api.com/json/"+Uri.encode(addr)+"?fields=countryCode,country&lang="+DrgPluginManager.lang()).openConnection();con.setConnectTimeout(3500);con.setReadTimeout(3500);try(BufferedReader br=new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"))){String s=br.readLine();if(s!=null){Matcher mc=Pattern.compile("\"countryCode\"\\s*:\\s*\"([A-Z]{2})\"").matcher(s);if(mc.find()){String iso=mc.group(1);String loc=new Locale("",iso).getDisplayCountry(Locale.getDefault());if(loc!=null&&!loc.isEmpty())cName=loc;}if("—".equals(cName)){Matcher m=Pattern.compile("\"country\"\\s*:\\s*\"([^\"]+)\"").matcher(s);if(m.find()&&!m.group(1).isEmpty())cName=m.group(1);}}}}catch(Throwable ignored){}final String fc=cName;countryCache.put(addr,fc);DrgPluginManager.H.post(()->{try{tv.setText(fc);}catch(Throwable ignored){}});}).start();}

  private static void openProxyDetailSheet(final Context ctx,final SharedConfig.ProxyInfo p,final Runnable onChanged){
    try{
      final BottomSheet bs=new BottomSheet(ctx,false);
      LinearLayout root=new LinearLayout(ctx);root.setOrientation(LinearLayout.VERTICAL);
      root.setBackground(cardBg(0xFF181C22,20,0));
      int p18=AndroidUtilities.dp(18),p12=AndroidUtilities.dp(12);
      root.setPadding(p18,p18,p18,p18);

      LinearLayout hdr=new LinearLayout(ctx);hdr.setOrientation(LinearLayout.HORIZONTAL);hdr.setGravity(Gravity.CENTER_VERTICAL);
      hdr.setPadding(0,0,0,p12);
      TextView ttl=new TextView(ctx);ttl.setText(tgStr("Proxy","Проксі-сервер","Прокси-сервер","Proxy Server"));
      ttl.setTextColor(Color.WHITE);ttl.setTextSize(TypedValue.COMPLEX_UNIT_DIP,19);ttl.setTypeface(AndroidUtilities.bold());
      hdr.addView(ttl,new LinearLayout.LayoutParams(0,-2,1f));
      TextView delBtn=new TextView(ctx);delBtn.setText(tgStr("Delete","Видалити","Удалить","Delete"));
      delBtn.setTextColor(0xFFE56555);delBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14);
      delBtn.setOnClickListener(v->{SharedConfig.deleteProxy(p);bs.dismiss();if(onChanged!=null)onChanged.run();});
      hdr.addView(delBtn);root.addView(hdr);

      LinearLayout tbl=new LinearLayout(ctx);tbl.setOrientation(LinearLayout.VERTICAL);
      tbl.setBackground(cardBg(0xFF1F242C,12,0x26FFFFFF));
      String sec=pSec(p),usr=pUser(p),addr=pAddr(p);
      boolean isMt=!sec.isEmpty(),av=pBool(p,"available"),chk=pBool(p,"checking");long pg=pLong(p,"ping");
      final TextView[] countryTv={null},statusTv={null};
      String[][] rows={
        {tr("Тип","Тип","Type"),isMt?tr("Проксі MTProto","Прокси MTProto","MTProto Proxy"):"SOCKS5"},
        {tgStr("Country","Країна","Страна","Country"),"..."},
        {tgStr("UseProxyAddress","Сервер","Сервер","Server"),addr},
        {tgStr("UseProxyPort","Порт","Порт","Port"),String.valueOf(pPort(p))},
        {isMt?tgStr("UseProxySecret","Секрет","Секрет","Secret"):tgStr("UseProxyUsername","Логін","Логин","Username"),isMt?sec:(!usr.isEmpty()?usr:"—")},
        {tr("Статус","Статус","Status"),chk?tgStr("Checking","Перевірка...","Проверка...","Checking..."):(av?(tgStr("Available","Доступний","Доступен","Available")+" ("+tgStr("Ping","пінг","пинг","ping")+": "+pg+" ms)"):tr("Перевірити","Проверить","Check"))}
      };
      for(int i=0;i<rows.length;i++){
        LinearLayout r=new LinearLayout(ctx);r.setOrientation(LinearLayout.HORIZONTAL);r.setGravity(Gravity.CENTER_VERTICAL);
        TextView k=new TextView(ctx);k.setText(rows[i][0]);k.setTextColor(0xFF8A94A3);k.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14);
        k.setPadding(p12,p12,p12,p12);
        r.addView(k,new LinearLayout.LayoutParams(AndroidUtilities.dp(92),-2));
        View vLine=new View(ctx);vLine.setBackgroundColor(0x22FFFFFF);
        r.addView(vLine,new LinearLayout.LayoutParams(1,-1));
        TextView v=new TextView(ctx);v.setText(rows[i][1]);v.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14);
        v.setPadding(p12,p12,p12,p12);
        v.setTextColor(i==5?(av?0xFF4CAF50:0xFF4EA4F5):Color.WHITE);
        if(i==1)countryTv[0]=v;
        if(i==5){
          statusTv[0]=v;
          r.setOnClickListener(clk->{
            statusTv[0].setTextColor(0xFF8A94A3);statusTv[0].setText(tgStr("Checking","Перевірка...","Проверка...","Checking..."));
            pingProxy(p,()->{
              if(pBool(p,"available")){statusTv[0].setTextColor(0xFF4CAF50);statusTv[0].setText(tgStr("Available","Доступний","Доступен","Available")+" ("+tgStr("Ping","пінг","пинг","ping")+": "+pLong(p,"ping")+" ms)");}
              else{statusTv[0].setTextColor(0xFFE56555);statusTv[0].setText(tgStr("Unavailable","Недоступний","Недоступен","Unavailable"));}
              if(onChanged!=null)onChanged.run();
            });
          });
        }
        r.addView(v,new LinearLayout.LayoutParams(0,-2,1f));
        tbl.addView(r);
        if(i<rows.length-1){View hLine=new View(ctx);hLine.setBackgroundColor(0x22FFFFFF);tbl.addView(hLine,new LinearLayout.LayoutParams(-1,1));}
      }
      root.addView(tbl);
      fetchCountryAsync(addr,countryTv[0]);

      TextView note=new TextView(ctx);
      note.setText(tr("Цей сервер може показувати рекламний канал у списку чатів. Ваш трафік не розкриватиметься.","Этот сервер может показывать рекламный канал в списке чатов. Ваш трафик не раскрывается.","This server may display a sponsored channel in your chat list. Your traffic is not disclosed."));
      note.setTextColor(0xFF8A94A3);note.setTextSize(TypedValue.COMPLEX_UNIT_DIP,12);note.setGravity(Gravity.CENTER);
      note.setPadding(p12,p12,p12,p12);root.addView(note);

      TextView connBtn=new TextView(ctx);connBtn.setText(tgStr("ConnectToProxy","Під'єднатися до проксі","Подключиться к прокси","Connect to Proxy"));
      connBtn.setTextColor(Color.WHITE);connBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP,15.5f);connBtn.setTypeface(AndroidUtilities.bold());
      connBtn.setGravity(Gravity.CENTER);connBtn.setBackground(cardBg(0xFF3897F0,22,0));
      connBtn.setPadding(p18,AndroidUtilities.dp(13),p18,AndroidUtilities.dp(13));
      connBtn.setOnClickListener(v->{applyProxyNow(p,true);bs.dismiss();if(onChanged!=null)onChanged.run();});
      root.addView(connBtn);

      bs.setCustomView(root);bs.show();
    }catch(Throwable ignored){}
  }
}
// === КІНЕЦЬ ФАЙЛУ ===
