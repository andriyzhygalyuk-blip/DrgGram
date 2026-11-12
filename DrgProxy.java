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
import org.telegram.ui.ActionBar.Theme;
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
        }else if(type==3){// Plus SVG
          c.drawCircle(cx,cy,r*0.9f,p);
          c.drawLine(cx-r*0.45f,cy,cx+r*0.45f,cy,p);c.drawLine(cx,cy-r*0.45f,cx,cy+r*0.45f,p);
        }else if(type==4){// Info (i) SVG
          c.drawCircle(cx,cy,r*0.9f,p);
          c.drawLine(cx,cy-r*0.05f,cx,cy+r*0.45f,p);
          Paint fp=new Paint(Paint.ANTI_ALIAS_FLAG);fp.setColor(color);fp.setStyle(Paint.Style.FILL);
          c.drawCircle(cx,cy-r*0.4f,AndroidUtilities.dp(1.3f),fp);
        }else if(type==5){// Check SVG
          path.moveTo(cx-r*0.65f,cy+r*0.05f);path.lineTo(cx-r*0.15f,cy+r*0.55f);path.lineTo(cx+r*0.75f,cy-r*0.45f);c.drawPath(path,p);
        }
      }
      @Override public void setAlpha(int a){p.setAlpha(a);}
      @Override public void setColorFilter(ColorFilter cf){p.setColorFilter(cf);}
      @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
      @Override public int getIntrinsicWidth(){return AndroidUtilities.dp(24);}
      @Override public int getIntrinsicHeight(){return AndroidUtilities.dp(24);}
    };
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

  private static void collect(View v,List<View> out){if(v==null)return;out.add(v);if(v instanceof ViewGroup){ViewGroup vg=(ViewGroup)v;for(int i=0;i<vg.getChildCount();i++)collect(vg.getChildAt(i),out);}}

  private static Object getSettingsObj(Object p){
    if(p==null)return null;
    try{for(Field f:p.getClass().getDeclaredFields()){if(f.getType().getSimpleName().contains("ProxySettings")){f.setAccessible(true);Object v=f.get(p);if(v!=null)return v;}}}catch(Throwable ignored){}
    return p;
  }
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

  private static SharedConfig.ProxyInfo createProxyInfo(String srv,int prt,String usr,String pwd,String sec){
    try{
      for(Constructor<?> c:SharedConfig.ProxyInfo.class.getDeclaredConstructors()){
        c.setAccessible(true);Class<?>[] pt=c.getParameterTypes();
        if(pt.length==5)return (SharedConfig.ProxyInfo)c.newInstance(srv,prt,usr,pwd,sec);
        if(pt.length==1){
          Class<?> sc=pt[0];Object settings=null;
          for(Constructor<?> scCon:sc.getDeclaredConstructors()){
            scCon.setAccessible(true);
            if(scCon.getParameterTypes().length==5){settings=scCon.newInstance(srv,prt,usr,pwd,sec);break;}
            if(scCon.getParameterTypes().length==0){settings=scCon.newInstance();break;}
          }
          if(settings!=null){
            pSet(settings,srv,"address","ip","host","server");pSet(settings,Integer.valueOf(prt),"port");
            pSet(settings,usr,"username","user");pSet(settings,pwd,"password","pass");pSet(settings,sec,"secret");
            return (SharedConfig.ProxyInfo)c.newInstance(settings);
          }
        }
      }
    }catch(Throwable ignored){}
    return null;
  }

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

  private static View makeActionRow(Context ctx,int svgType,int color,String text,View.OnClickListener clk){
    LinearLayout r=new LinearLayout(ctx);r.setOrientation(LinearLayout.HORIZONTAL);r.setGravity(Gravity.CENTER_VERTICAL);
    r.setPadding(AndroidUtilities.dp(20),AndroidUtilities.dp(12),AndroidUtilities.dp(20),AndroidUtilities.dp(12));
    ImageView iv=new ImageView(ctx);iv.setImageDrawable(makeSvg(svgType,color));
    r.addView(iv,new LinearLayout.LayoutParams(AndroidUtilities.dp(24),AndroidUtilities.dp(24)));
    TextView tv=new TextView(ctx);tv.setText(text);tv.setTextColor(color);tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,15);
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1f);lp.leftMargin=AndroidUtilities.dp(16);
    r.addView(tv,lp);r.setOnClickListener(clk);return r;
  }

  public static void openProxyManagerDialog(final Context ctx){
    try{
      SharedConfig.loadProxyList();
      final boolean proxyEn=MessagesController.getGlobalMainSettings().getBoolean("proxy_enabled",false);
      ScrollView sv=new ScrollView(ctx);
      LinearLayout box=new LinearLayout(ctx);box.setOrientation(LinearLayout.VERTICAL);
      box.setPadding(0,AndroidUtilities.dp(8),0,AndroidUtilities.dp(8));sv.addView(box);
      int txtCol=Theme.getColor(Theme.key_dialogTextBlack),blueCol=Theme.getColor(Theme.key_dialogTextBlue2),grayCol=Theme.getColor(Theme.key_dialogTextGray2);
      final AlertDialog[] dlg={null};

      box.addView(makeActionRow(ctx,0,proxyEn?0xFF4CAF50:txtCol,tgStr("UseProxySettings","Використовувати проксі","Использовать прокси","Use Proxy")+": "+(proxyEn?"ON":"OFF"),v->{
        if(dlg[0]!=null)dlg[0].dismiss();
        boolean nx=!proxyEn;if(nx&&SharedConfig.currentProxy==null&&!SharedConfig.proxyList.isEmpty())SharedConfig.currentProxy=SharedConfig.proxyList.get(0);
        applyProxyNow(SharedConfig.currentProxy,nx);openProxyManagerDialog(ctx);
      }));
      box.addView(makeActionRow(ctx,1,blueCol,tgStr("Refresh","Оновити","Обновить","Refresh"),v->{
        if(dlg[0]!=null)dlg[0].dismiss();
        if(SharedConfig.proxyList.isEmpty()){openProxyManagerDialog(ctx);return;}
        DrgPluginManager.toast(tgStr("Checking","Перевірка...","Проверка...","Checking..."));
        final int[] left={SharedConfig.proxyList.size()};
        for(SharedConfig.ProxyInfo p:SharedConfig.proxyList)pingProxy(p,()->{left[0]--;if(left[0]<=0)openProxyManagerDialog(ctx);});
      }));
      box.addView(makeActionRow(ctx,2,0xFFE53935,tgStr("Delete","Видалити","Удалить","Delete")+" ("+tgStr("Unavailable","Недоступний","Недоступен","Unavailable")+")",v->{
        if(dlg[0]!=null)dlg[0].dismiss();
        int rem=0;for(int i=SharedConfig.proxyList.size()-1;i>=0;i--){SharedConfig.ProxyInfo p=SharedConfig.proxyList.get(i);if(!pBool(p,"available")&&pLong(p,"ping")<=0){SharedConfig.deleteProxy(p);rem++;}}
        DrgPluginManager.toast(tgStr("Delete","Видалити","Удалить","Delete")+": "+rem);openProxyManagerDialog(ctx);
      }));
      box.addView(makeActionRow(ctx,3,blueCol,tgStr("AddProxy","Додати проксі","Добавить прокси","Add Proxy"),v->{
        if(dlg[0]!=null)dlg[0].dismiss();openAddProxyDialog(ctx);
      }));

      for(final SharedConfig.ProxyInfo p:new ArrayList<>(SharedConfig.proxyList)){
        boolean cur=SharedConfig.currentProxy==p&&proxyEn,chk=pBool(p,"checking"),av=pBool(p,"available");long pg=pLong(p,"ping");
        LinearLayout row=new LinearLayout(ctx);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(AndroidUtilities.dp(20),AndroidUtilities.dp(10),AndroidUtilities.dp(16),AndroidUtilities.dp(10));
        ImageView leftIc=new ImageView(ctx);leftIc.setImageDrawable(makeSvg(cur?5:0,cur?0xFF4CAF50:grayCol));
        row.addView(leftIc,new LinearLayout.LayoutParams(AndroidUtilities.dp(22),AndroidUtilities.dp(22)));
        LinearLayout col=new LinearLayout(ctx);col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams clp=new LinearLayout.LayoutParams(0,-2,1f);clp.leftMargin=AndroidUtilities.dp(14);
        TextView t1=new TextView(ctx);t1.setText(pAddr(p)+":"+pPort(p));t1.setTextColor(txtCol);t1.setTextSize(TypedValue.COMPLEX_UNIT_DIP,15);col.addView(t1);
        TextView t2=new TextView(ctx);String st=chk?tgStr("Checking","Перевірка...","Проверка...","Checking..."):(av?(tgStr("Ping","Пінг","Пинг","Ping")+": "+pg+" ms"):tgStr("Unavailable","Недоступний","Недоступен","Unavailable"));
        t2.setText(st);t2.setTextColor(chk?grayCol:(av?0xFF4CAF50:0xFFE53935));t2.setTextSize(TypedValue.COMPLEX_UNIT_DIP,12.5f);col.addView(t2);
        row.addView(col,clp);
        ImageView infoIc=new ImageView(ctx);infoIc.setImageDrawable(makeSvg(4,grayCol));
        row.addView(infoIc,new LinearLayout.LayoutParams(AndroidUtilities.dp(24),AndroidUtilities.dp(24)));
        row.setOnClickListener(v->{if(dlg[0]!=null)dlg[0].dismiss();openProxyDetailDialog(ctx,p);});
        box.addView(row);
      }
      dlg[0]=new AlertDialog.Builder(ctx).setTitle(tgStr("ProxySettings","Налаштування проксі","Настройки прокси","Proxy Settings")).setView(sv).setNegativeButton(tgStr("Close","Закрити","Закрыть","Close"),null).show();
    }catch(Throwable ignored){}
  }

  private static void openAddProxyDialog(final Context ctx){try{final EditText et=new EditText(ctx);et.setHint("tg://proxy?server=... / ip:port:secret");et.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));et.setHintTextColor(Theme.getColor(Theme.key_dialogTextGray3));int pad=AndroidUtilities.dp(18);et.setPadding(pad,pad,pad,pad);new AlertDialog.Builder(ctx).setTitle(tgStr("AddProxy","Додати проксі","Добавить прокси","Add Proxy")).setView(et).setPositiveButton(tgStr("Add","Додати","Добавить","Add"),(d,w)->{try{String s=et.getText().toString().trim(),srv="",usr="",pwd="",sec="";int prt=1080;if(s.contains("server=")){Uri u=Uri.parse(s.replace("tg://","https://t.me/"));srv=u.getQueryParameter("server");String ps=u.getQueryParameter("port");if(ps!=null)prt=Integer.parseInt(ps);usr=u.getQueryParameter("user");pwd=u.getQueryParameter("pass");sec=u.getQueryParameter("secret");}else if(s.contains(":")){String[] pt=s.split(":");srv=pt[0];prt=Integer.parseInt(pt[1]);if(pt.length>2)sec=pt[2];}if(srv!=null&&!srv.isEmpty()){SharedConfig.ProxyInfo pi=createProxyInfo(srv,prt,usr!=null?usr:"",pwd!=null?pwd:"",sec!=null?sec:"");if(pi!=null){SharedConfig.addProxy(pi);pingProxy(pi,()->openProxyManagerDialog(ctx));}}}catch(Throwable ignored){}}).setNegativeButton(tgStr("Cancel","Скасувати","Отмена","Cancel"),(d,w)->openProxyManagerDialog(ctx)).show();}catch(Throwable ignored){}}

  private static void openProxyDetailDialog(final Context ctx,final SharedConfig.ProxyInfo p){try{LinearLayout box=new LinearLayout(ctx);box.setOrientation(LinearLayout.VERTICAL);int pad=AndroidUtilities.dp(20);box.setPadding(pad,AndroidUtilities.dp(12),pad,AndroidUtilities.dp(8));String sec=pSec(p),usr=pUser(p),addr=pAddr(p);boolean isMt=!sec.isEmpty(),av=pBool(p,"available"),chk=pBool(p,"checking");long pg=pLong(p,"ping");final TextView[] countryTv={null},statusTv={null};String[][] rows={{tr("Тип","Тип","Type"),isMt?"MTProto":"SOCKS5"},{tgStr("Country","Країна","Страна","Country"),"..."},{tgStr("UseProxyAddress","Сервер","Сервер","Server"),addr},{tgStr("UseProxyPort","Порт","Порт","Port"),String.valueOf(pPort(p))},{isMt?tgStr("UseProxySecret","Секрет","Секрет","Secret"):tgStr("UseProxyUsername","Логін","Логин","Username"),isMt?sec:(!usr.isEmpty()?usr:"—")},{tr("Статус","Статус","Status"),chk?tgStr("Checking","Перевірка...","Проверка...","Checking..."):(av?(pg+" ms"):tr("Перевірити","Проверить","Check"))}};
  for(int i=0;i<rows.length;i++){LinearLayout r=new LinearLayout(ctx);r.setOrientation(LinearLayout.HORIZONTAL);r.setPadding(0,AndroidUtilities.dp(8),0,AndroidUtilities.dp(8));TextView k=new TextView(ctx);k.setText(rows[i][0]);k.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));k.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14);r.addView(k,new LinearLayout.LayoutParams(AndroidUtilities.dp(95),-2));TextView v=new TextView(ctx);v.setText(rows[i][1]);v.setTextColor(i==5?(av?0xFF4CAF50:Theme.getColor(Theme.key_dialogTextBlue2)):Theme.getColor(Theme.key_dialogTextBlack));v.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14);if(i==1)countryTv[0]=v;if(i==5){statusTv[0]=v;r.setOnClickListener(clk->{statusTv[0].setText(tgStr("Checking","Перевірка...","Проверка...","Checking..."));pingProxy(p,()->{if(pBool(p,"available")){statusTv[0].setTextColor(0xFF4CAF50);statusTv[0].setText(pLong(p,"ping")+" ms");}else{statusTv[0].setTextColor(0xFFE53935);statusTv[0].setText(tgStr("Unavailable","Недоступний","Недоступен","Unavailable"));}});});}r.addView(v,new LinearLayout.LayoutParams(0,-2,1f));box.addView(r);}
  fetchCountryAsync(addr,countryTv[0]);if(!av&&!chk)pingProxy(p,()->{if(statusTv[0]!=null){if(pBool(p,"available")){statusTv[0].setTextColor(0xFF4CAF50);statusTv[0].setText(pLong(p,"ping")+" ms");}else{statusTv[0].setTextColor(0xFFE53935);statusTv[0].setText(tgStr("Unavailable","Недоступний","Недоступен","Unavailable"));}}});
  new AlertDialog.Builder(ctx).setTitle(tgStr("Proxy","Проксі-сервер","Прокси-сервер","Proxy Server")).setView(box).setPositiveButton(tgStr("ConnectToProxy","Під'єднатися","Подключиться","Connect"),(d,w)->applyProxyNow(p,true)).setNeutralButton(tgStr("Delete","Видалити","Удалить","Delete"),(d,w)->{SharedConfig.deleteProxy(p);openProxyManagerDialog(ctx);}).setNegativeButton(tgStr("Close","Закрити","Закрыть","Close"),(d,w)->openProxyManagerDialog(ctx)).show();}catch(Throwable ignored){}}
}
// === КІНЕЦЬ ФАЙЛУ ===
