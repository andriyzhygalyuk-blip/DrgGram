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
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
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
        }
      }
      @Override public void setAlpha(int a){p.setAlpha(a);}
      @Override public void setColorFilter(ColorFilter cf){p.setColorFilter(cf);}
      @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
      @Override public int getIntrinsicWidth(){return AndroidUtilities.dp(22);}
      @Override public int getIntrinsicHeight(){return AndroidUtilities.dp(22);}
    };
  }

  private static GradientDrawable cardBg(int color,int radDp,int strokeCol){
    GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(AndroidUtilities.dp(radDp));
    if(strokeCol!=0)g.setStroke(Math.max(1,AndroidUtilities.dp(0.8f)),strokeCol);
    return g;
  }

  public static void onPopupShown(final ActionBarPopupWindow pw){
    if(pw==null)return;
    DrgPluginManager.H.postDelayed(()->{
      try{
        View cv=pw.getContentView();if(!(cv instanceof ViewGroup))return;
        List<View> all=new ArrayList<>();collect(cv,all);
        ViewGroup target=null;
        String proxyLbl=tgStr("ProxySettings","Проксі","Прокси","Proxy").toLowerCase(Locale.ROOT);
        boolean isDialogsMenu=false,hasNativeProxy=false;
        for(View v:all){
          if("drg_proxy_sub".equals(v.getTag()))return;
          if(v instanceof ActionBarMenuSubItem&&v.getVisibility()==View.VISIBLE){
            ActionBarMenuSubItem si=(ActionBarMenuSubItem)v;
            if(si.getParent() instanceof ViewGroup)target=(ViewGroup)si.getParent();
            String t=si.getTextView()!=null&&si.getTextView().getText()!=null?si.getTextView().getText().toString().toLowerCase(Locale.ROOT):"";
            if(t.contains("drgplugins")||t.contains("пошук")||t.contains("поиск")||t.contains("search"))return;
            if(t.contains("проксі")||t.contains("прокси")||t.contains("proxy")||t.contains(proxyLbl))hasNativeProxy=true;
            if(t.contains("група")||t.contains("группа")||t.contains("group")||t.contains("архів")||t.contains("архив")||t.contains("archive")||t.contains("збережене")||t.contains("избранное")||t.contains("saved")||t.contains("гаманець")||t.contains("кошелек")||t.contains("wallet"))isDialogsMenu=true;
          }
        }
        if(target==null||hasNativeProxy||!isDialogsMenu)return;
        Context ctx=target.getContext();
        ActionBarMenuSubItem item=new ActionBarMenuSubItem(ctx,false,true);
        item.setTag("drg_proxy_sub");
        int icCol=Theme.getColor(Theme.key_actionBarDefaultSubmenuItemIcon);
        item.setTextAndIcon(tgStr("ProxySettings","Проксі","Прокси","Proxy"),0,makeSvg(0,icCol));
        item.setOnClickListener(v->{try{pw.dismiss();}catch(Throwable ignored){}openProxyManagerDialog(ctx);});
        target.addView(item);
      }catch(Throwable ignored){}
    },40);
  }

  public static void onBottomSheetShown(final BottomSheet bs){
    if(bs==null)return;
    for(int delay:new int[]{50,180}){
      DrgPluginManager.H.postDelayed(()->{
        try{
          if(bs.getWindow()==null)return;
          View decor=bs.getWindow().getDecorView();if(!(decor instanceof ViewGroup))return;
          List<View> all=new ArrayList<>();collect(decor,all);
          ViewGroup srvRow=null,table=null;
          String srvAddress="",secVal="";
          String srvKey1=tgStr("UseProxyAddress","Сервер","Сервер","Server");
          String secKey1=tgStr("UseProxySecret","Секрет","Секрет","Secret");
          for(View v:all){
            if("drg_bs_country".equals(v.getTag()))return;
            if(v instanceof TextView){
              CharSequence cs=((TextView)v).getText();
              String s=cs!=null?cs.toString().trim():"";
              if((s.equalsIgnoreCase(srvKey1)||s.equalsIgnoreCase("Сервер")||s.equalsIgnoreCase("Server"))&&v.getParent() instanceof ViewGroup){
                ViewGroup row=(ViewGroup)v.getParent();
                if(row.getParent() instanceof ViewGroup&&row.getChildCount()>=2){
                  srvRow=row;table=(ViewGroup)row.getParent();
                  for(int i=0;i<row.getChildCount();i++){
                    View ch=row.getChildAt(i);
                    if(ch instanceof TextView&&ch!=v){srvAddress=((TextView)ch).getText().toString().trim();}
                  }
                }
              }else if((s.equalsIgnoreCase(secKey1)||s.equalsIgnoreCase("Секрет")||s.equalsIgnoreCase("Secret"))&&v.getParent() instanceof ViewGroup){
                ViewGroup row=(ViewGroup)v.getParent();
                for(int i=0;i<row.getChildCount();i++){
                  View ch=row.getChildAt(i);
                  if(ch instanceof TextView&&ch!=v){secVal=((TextView)ch).getText().toString().trim();}
                }
              }
            }
          }
          if(srvRow==null||table==null||srvAddress.isEmpty())return;
          Context ctx=table.getContext();
          int srvIdx=table.indexOfChild(srvRow);if(srvIdx<0)srvIdx=0;
          boolean isMt=!secVal.isEmpty();
          String typeStr=isMt?tr("Проксі MTProto","Прокси MTProto","MTProto Proxy"):"SOCKS5";
          TextView[] cTv={null};
          View rType=makeTableCloneRow(ctx,srvRow,tr("Тип","Тип","Type"),typeStr,null);
          rType.setTag("drg_bs_country");
          View div1=new View(ctx);div1.setBackgroundColor(0x22FFFFFF);
          View rCountry=makeTableCloneRow(ctx,srvRow,tgStr("Country","Країна","Страна","Country"),"...",cTv);
          View div2=new View(ctx);div2.setBackgroundColor(0x22FFFFFF);
          table.addView(div2,srvIdx);
          table.addView(rCountry,srvIdx);
          table.addView(div1,srvIdx);
          table.addView(rType,srvIdx);
          if(cTv[0]!=null)fetchCountryAsync(srvAddress,cTv[0]);
        }catch(Throwable ignored){}
      },delay);
    }
  }

  private static View makeTableCloneRow(Context ctx,ViewGroup sampleRow,String key,String val,TextView[] outVal){
    LinearLayout r=new LinearLayout(ctx);r.setOrientation(LinearLayout.HORIZONTAL);r.setGravity(Gravity.CENTER_VERTICAL);
    int kCol=0xFF8A94A3,vCol=Color.WHITE,kW=AndroidUtilities.dp(92),pL=AndroidUtilities.dp(12),pT=AndroidUtilities.dp(11);
    float tSz=14f;
    try{
      if(sampleRow.getChildAt(0) instanceof TextView){
        TextView sk=(TextView)sampleRow.getChildAt(0);
        kCol=sk.getCurrentTextColor();
        if(sk.getLayoutParams()!=null&&sk.getLayoutParams().width>0)kW=sk.getLayoutParams().width;
        pL=sk.getPaddingLeft()>0?sk.getPaddingLeft():pL;
        pT=sk.getPaddingTop()>0?sk.getPaddingTop():pT;
      }
      for(int i=1;i<sampleRow.getChildCount();i++){
        if(sampleRow.getChildAt(i) instanceof TextView){vCol=((TextView)sampleRow.getChildAt(i)).getCurrentTextColor();break;}
      }
    }catch(Throwable ignored){}
    TextView k=new TextView(ctx);k.setText(key);k.setTextColor(kCol);k.setTextSize(TypedValue.COMPLEX_UNIT_DIP,tSz);
    k.setPadding(pL,pT,pL,pT);
    r.addView(k,new LinearLayout.LayoutParams(kW,-2));
    View vLine=new View(ctx);vLine.setBackgroundColor(0x22FFFFFF);
    r.addView(vLine,new LinearLayout.LayoutParams(1,-1));
    TextView v=new TextView(ctx);v.setText(val);v.setTextColor(vCol);v.setTextSize(TypedValue.COMPLEX_UNIT_DIP,tSz);
    v.setPadding(pL,pT,pL,pT);
    if(outVal!=null)outVal[0]=v;
    r.addView(v,new LinearLayout.LayoutParams(0,-2,1f));
    return r;
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
    DrgPluginManager.H.postDelayed(()->{
      try{
        if(vg.findViewWithTag("drg_proxy_floating_bar")!=null)return;
        LinearLayout bar=new LinearLayout(ctx);bar.setTag("drg_proxy_floating_bar");
        bar.setOrientation(LinearLayout.HORIZONTAL);bar.setGravity(Gravity.CENTER);
        bar.setBackground(cardBg(0xF21C222B,24,0x33FFFFFF));
        int p14=AndroidUtilities.dp(14),p10=AndroidUtilities.dp(10);
        bar.setPadding(p14,AndroidUtilities.dp(6),p14,AndroidUtilities.dp(6));

        LinearLayout bRef=new LinearLayout(ctx);bRef.setOrientation(LinearLayout.HORIZONTAL);bRef.setGravity(Gravity.CENTER_VERTICAL);
        bRef.setPadding(p10,AndroidUtilities.dp(6),p10,AndroidUtilities.dp(6));
        int blue=0xFF4EA4F5;
        ImageView icRef=new ImageView(ctx);icRef.setImageDrawable(makeSvg(1,blue));
        bRef.addView(icRef,new LinearLayout.LayoutParams(AndroidUtilities.dp(20),AndroidUtilities.dp(20)));
        TextView tvRef=new TextView(ctx);tvRef.setText(tgStr("Refresh","Оновити","Обновить","Refresh"));
        tvRef.setTextColor(blue);tvRef.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14);tvRef.setTypeface(AndroidUtilities.bold());
        tvRef.setPadding(AndroidUtilities.dp(6),0,0,0);bRef.addView(tvRef);
        bRef.setOnClickListener(v->{
          SharedConfig.loadProxyList();
          if(SharedConfig.proxyList.isEmpty())return;
          DrgPluginManager.toast(tgStr("Checking","Перевірка...","Проверка...","Checking..."));
          for(SharedConfig.ProxyInfo p:SharedConfig.proxyList){
            pSet(p,Boolean.FALSE,"available");pSet(p,Long.valueOf(0L),"ping");pSet(p,Long.valueOf(0L),"addressCheckTime");pSet(p,Integer.valueOf(0),"proxyCheckPingId");
            pingProxy(p,()->refreshNativeProxyList(bf));
          }
          refreshNativeProxyList(bf);
        });
        bar.addView(bRef);

        View sep=new View(ctx);sep.setBackgroundColor(0x33FFFFFF);
        LinearLayout.LayoutParams sepLp=new LinearLayout.LayoutParams(1,AndroidUtilities.dp(20));
        sepLp.leftMargin=AndroidUtilities.dp(6);sepLp.rightMargin=AndroidUtilities.dp(6);
        bar.addView(sep,sepLp);

        LinearLayout bDel=new LinearLayout(ctx);bDel.setOrientation(LinearLayout.HORIZONTAL);bDel.setGravity(Gravity.CENTER_VERTICAL);
        bDel.setPadding(p10,AndroidUtilities.dp(6),p10,AndroidUtilities.dp(6));
        int red=0xFFE56555;
        ImageView icDel=new ImageView(ctx);icDel.setImageDrawable(makeSvg(2,red));
        bDel.addView(icDel,new LinearLayout.LayoutParams(AndroidUtilities.dp(20),AndroidUtilities.dp(20)));
        TextView tvDel=new TextView(ctx);tvDel.setText(tgStr("Delete","Видалити","Удалить","Delete")+" ("+tgStr("Unavailable","Недоступні","Недоступные","Unavailable")+")");
        tvDel.setTextColor(red);tvDel.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14);tvDel.setTypeface(AndroidUtilities.bold());
        tvDel.setPadding(AndroidUtilities.dp(6),0,0,0);bDel.addView(tvDel);
        bDel.setOnClickListener(v->{
          SharedConfig.loadProxyList();
          int rem=0;
          for(int i=SharedConfig.proxyList.size()-1;i>=0;i--){
            SharedConfig.ProxyInfo p=SharedConfig.proxyList.get(i);
            if(!pBool(p,"available")&&pLong(p,"ping")<=0&&SharedConfig.currentProxy!=p){SharedConfig.deleteProxy(p);rem++;}
          }
          DrgPluginManager.toast(tgStr("Delete","Видалити","Удалить","Delete")+": "+rem);
          refreshNativeProxyList(bf);
        });
        bar.addView(bDel);

        FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-2,-2,Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
        lp.bottomMargin=AndroidUtilities.dp(24);
        vg.addView(bar,lp);
        bar.bringToFront();
      }catch(Throwable ignored){}
    },120);
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
}
// === КІНЕЦЬ ФАЙЛУ ===
