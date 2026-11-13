package org.telegram.messenger;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
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
import java.net.InetAddress;
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

  private static String getViewText(View v){
    if(v==null)return "";
    if(v instanceof TextView){
      CharSequence cs=((TextView)v).getText();
      return cs!=null?cs.toString().trim():"";
    }
    try{
      Method m=v.getClass().getMethod("getText");
      Object r=m.invoke(v);
      return r!=null?r.toString().trim():"";
    }catch(Throwable ignored){}
    return "";
  }

  public static void onBottomSheetShown(final BottomSheet bs){
    if(bs==null)return;
    for(int delay:new int[]{40,140,320}){
      DrgPluginManager.H.postDelayed(()->{
        try{
          if(bs.getWindow()==null)return;
          View decor=bs.getWindow().getDecorView();if(!(decor instanceof ViewGroup))return;
          List<View> all=new ArrayList<>();collect(decor,all);
          for(View v:all)if("drg_bs_country".equals(v.getTag()))return;

          View srvLabelView=null;
          ViewGroup table=null,srvRow=null;
          String srvAddress="",secVal="";
          String srvKey1=tgStr("UseProxyAddress","Сервер","Сервер","Server");
          String secKey1=tgStr("UseProxySecret","Секрет","Секрет","Secret");

          for(View v:all){
            String s=getViewText(v);
            if(s.isEmpty())continue;
            if(srvLabelView==null&&(s.equalsIgnoreCase(srvKey1)||s.equalsIgnoreCase("Сервер")||s.equalsIgnoreCase("Server"))){
              srvLabelView=v;
              View p=v;
              while(p.getParent() instanceof ViewGroup){
                ViewGroup pg=(ViewGroup)p.getParent();
                if(pg.getClass().getSimpleName().contains("TableView")||pg.getChildCount()>=3){
                  table=pg;srvRow=(ViewGroup)p;break;
                }
                p=pg;
              }
              if(srvRow!=null){
                List<View> rvs=new ArrayList<>();collect(srvRow,rvs);
                for(View rv:rvs){
                  if(rv!=v){
                    String rt=getViewText(rv);
                    if(!rt.isEmpty()&&!rt.equalsIgnoreCase(s)){srvAddress=rt;break;}
                  }
                }
              }
            }else if(s.equalsIgnoreCase(secKey1)||s.equalsIgnoreCase("Секрет")||s.equalsIgnoreCase("Secret")){
              if(v.getParent() instanceof ViewGroup){
                List<View> rvs=new ArrayList<>();collect((ViewGroup)v.getParent(),rvs);
                for(View rv:rvs){
                  if(rv!=v){
                    String rt=getViewText(rv);
                    if(!rt.isEmpty()&&!rt.equalsIgnoreCase(s)){secVal=rt;break;}
                  }
                }
              }
            }
          }
          if(table==null||srvRow==null||srvAddress.isEmpty())return;
          boolean isMt=!secVal.isEmpty();
          String typeTitle=tr("Тип","Тип","Type");
          String typeVal=isMt?tr("Проксі MTProto","Прокси MTProto","MTProto Proxy"):"SOCKS5";
          String countryTitle=tgStr("Country","Країна","Страна","Country");

          View addedType=addNativeTableRow(table,srvRow,typeTitle,typeVal,null);
          TextView[] countryOut={null};
          View addedCountry=addNativeTableRow(table,srvRow,countryTitle,"...",countryOut);
          if(addedType!=null){
            addedType.setTag("drg_bs_country");
            int idx=table.indexOfChild(srvRow);if(idx<0)idx=0;
            if(addedCountry!=null&&addedCountry.getParent()==table){
              table.removeView(addedCountry);
              table.addView(addedCountry,idx);
            }
            if(addedType.getParent()==table){
              table.removeView(addedType);
              table.addView(addedType,idx);
            }
          }
          if(countryOut[0]!=null)fetchCountryAsync(srvAddress,countryOut[0]);
        }catch(Throwable ignored){}
      },delay);
    }
  }

  private static View addNativeTableRow(ViewGroup table,ViewGroup sampleRow,String key,String val,TextView[] outTv){
    try{
      for(Method m:table.getClass().getDeclaredMethods()){
        if("addRow".equals(m.getName())){
          m.setAccessible(true);
          Class<?>[] pt=m.getParameterTypes();
          if(pt.length==3&&pt[0]==CharSequence.class&&pt[1]==CharSequence.class){
            Object rowObj=m.invoke(table,key,val,null);
            if(rowObj instanceof View){
              View rv=(View)rowObj;
              if(outTv!=null){
                List<View> ch=new ArrayList<>();collect(rv,ch);
                for(int i=ch.size()-1;i>=0;i--){
                  if(ch.get(i) instanceof TextView){outTv[0]=(TextView)ch.get(i);break;}
                }
              }
              return rv;
            }
          }
        }
      }
    }catch(Throwable ignored){}
    Context ctx=table.getContext();
    LinearLayout r=new LinearLayout(ctx);r.setOrientation(LinearLayout.HORIZONTAL);r.setGravity(Gravity.CENTER_VERTICAL);
    int kCol=0xFF8A94A3,vCol=Color.WHITE,kW=AndroidUtilities.dp(92),pL=AndroidUtilities.dp(14),pT=AndroidUtilities.dp(11);
    try{
      List<View> sc=new ArrayList<>();collect(sampleRow,sc);
      TextView firstTv=null,secondTv=null;
      for(View v:sc){
        if(v instanceof TextView){
          if(firstTv==null)firstTv=(TextView)v;
          else{secondTv=(TextView)v;break;}
        }
      }
      if(firstTv!=null){
        kCol=firstTv.getCurrentTextColor();
        if(firstTv.getWidth()>0)kW=firstTv.getWidth();
        if(firstTv.getPaddingLeft()>0)pL=firstTv.getPaddingLeft();
        if(firstTv.getPaddingTop()>0)pT=firstTv.getPaddingTop();
      }
      if(secondTv!=null)vCol=secondTv.getCurrentTextColor();
    }catch(Throwable ignored){}
    TextView k=new TextView(ctx);k.setText(key);k.setTextColor(kCol);k.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14);
    k.setPadding(pL,pT,pL,pT);
    r.addView(k,new LinearLayout.LayoutParams(kW,-2));
    View vLine=new View(ctx);vLine.setBackgroundColor(0x22FFFFFF);
    r.addView(vLine,new LinearLayout.LayoutParams(1,-1));
    TextView v=new TextView(ctx);v.setText(val);v.setTextColor(vCol);v.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14);
    v.setPadding(pL,pT,pL,pT);
    if(outTv!=null)outTv[0]=v;
    r.addView(v,new LinearLayout.LayoutParams(0,-2,1f));
    table.addView(r);
    return r;
  }

  public static void openProxyManagerDialog(final Context ctx){
    try{
      BaseFragment f=LaunchActivity.getLastFragment();
      if(f!=null){f.presentFragment(new ProxyListActivity());return;}
    }catch(Throwable ignored){}
  }

  private static void doRefreshProxies(BaseFragment bf){
    SharedConfig.loadProxyList();
    for(SharedConfig.ProxyInfo p:SharedConfig.proxyList){
      pSet(p,Boolean.FALSE,"checking");
      pSet(p,Long.valueOf(0L),"availableCheckTime");
    }
    refreshNativeProxyList(bf,true);
  }

  private static void doDeleteUnavailableProxies(BaseFragment bf){
    SharedConfig.loadProxyList();
    for(int i=SharedConfig.proxyList.size()-1;i>=0;i--){
      SharedConfig.ProxyInfo p=SharedConfig.proxyList.get(i);
      if(p==SharedConfig.currentProxy)continue;
      boolean chk=pBool(p,"checking");
      long chkTime=pLong(p,"availableCheckTime");
      boolean av=pBool(p,"available");
      long pg=pLong(p,"ping");
      if(!chk&&chkTime>0&&(!av||pg<=0)){
        SharedConfig.deleteProxy(p);
      }
    }
    refreshNativeProxyList(bf,false);
  }

  public static void attachToNativeProxyList(final Object frag,final View root){
    if(!(frag instanceof BaseFragment)||!(root instanceof ViewGroup))return;
    final BaseFragment bf=(BaseFragment)frag;
    final ViewGroup vg=(ViewGroup)root;
    final Context ctx=vg.getContext();

    try{
      ActionBar ab=bf.getActionBar();
      if(ab!=null&&ab.findViewWithTag("drg_ab_proxy_btns")==null){
        ActionBarMenu menu=ab.createMenu();
        if(menu!=null){
          LinearLayout abBox=new LinearLayout(ctx);
          abBox.setTag("drg_ab_proxy_btns");
          abBox.setOrientation(LinearLayout.HORIZONTAL);
          abBox.setGravity(Gravity.CENTER_VERTICAL);
          int icCol=Theme.getColor(Theme.key_actionBarDefaultIcon);

          ImageView bRef=new ImageView(ctx);
          bRef.setImageDrawable(makeSvg(1,icCol));
          bRef.setScaleType(ImageView.ScaleType.CENTER);
          bRef.setOnClickListener(v->doRefreshProxies(bf));
          abBox.addView(bRef,new LinearLayout.LayoutParams(AndroidUtilities.dp(44),AndroidUtilities.dp(44)));

          ImageView bDel=new ImageView(ctx);
          bDel.setImageDrawable(makeSvg(2,0xFFE56555));
          bDel.setScaleType(ImageView.ScaleType.CENTER);
          bDel.setOnClickListener(v->doDeleteUnavailableProxies(bf));
          abBox.addView(bDel,new LinearLayout.LayoutParams(AndroidUtilities.dp(44),AndroidUtilities.dp(44)));

          menu.addView(abBox,new FrameLayout.LayoutParams(-2,-1,Gravity.RIGHT|Gravity.CENTER_VERTICAL));
        }
      }
    }catch(Throwable ignored){}

    DrgPluginManager.H.postDelayed(new Runnable(){
      @Override public void run(){
        try{
          if(!vg.isAttachedToWindow())return;
          List<View> all=new ArrayList<>();collect(vg,all);
          String connTitle=tgStr("ProxyConnections","З'єднання","Соединения","Connections");
          for(View v:all){
            String s=getViewText(v);
            if(s.isEmpty())continue;
            if((s.equalsIgnoreCase(connTitle)||s.equalsIgnoreCase("З'єднання")||s.equalsIgnoreCase("Соединения")||s.equalsIgnoreCase("Connections"))&&v.getParent() instanceof ViewGroup){
              final ViewGroup hc=(ViewGroup)v.getParent();
              if(hc.findViewWithTag("drg_hdr_bar")==null){
                final LinearLayout bar=new LinearLayout(ctx);
                bar.setTag("drg_hdr_bar");
                bar.setOrientation(LinearLayout.HORIZONTAL);
                bar.setGravity(Gravity.CENTER_VERTICAL);

                TextView tvDel=new TextView(ctx);
                tvDel.setText(tgStr("Delete","Видалити","Удалить","Delete")+" ("+tgStr("Unavailable","Недоступні","Недоступные","Unavailable")+")");
                tvDel.setTextColor(0xFFE56555);
                tvDel.setTextSize(TypedValue.COMPLEX_UNIT_DIP,13.5f);
                tvDel.setPadding(AndroidUtilities.dp(8),AndroidUtilities.dp(4),AndroidUtilities.dp(8),AndroidUtilities.dp(4));
                tvDel.setOnClickListener(clk->doDeleteUnavailableProxies(bf));
                bar.addView(tvDel);

                TextView tvRef=new TextView(ctx);
                tvRef.setText(tgStr("Refresh","Оновити","Обновить","Refresh"));
                tvRef.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
                tvRef.setTextSize(TypedValue.COMPLEX_UNIT_DIP,13.5f);
                tvRef.setPadding(AndroidUtilities.dp(8),AndroidUtilities.dp(4),AndroidUtilities.dp(6),AndroidUtilities.dp(4));
                tvRef.setOnClickListener(clk->doRefreshProxies(bf));
                bar.addView(tvRef);

                hc.addView(bar);
                hc.getViewTreeObserver().addOnPreDrawListener(()->{
                  try{
                    int w=hc.getWidth(),h=hc.getHeight();
                    if(w>0&&h>0&&bar.getParent()==hc){
                      bar.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.AT_MOST),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.AT_MOST));
                      int bw=bar.getMeasuredWidth(),bh=bar.getMeasuredHeight();
                      int rightPad=AndroidUtilities.dp(14);
                      int topPos=Math.max(0,(h-bh)/2+AndroidUtilities.dp(2));
                      bar.layout(w-bw-rightPad,topPos,w-rightPad,topPos+bh);
                    }
                  }catch(Throwable ignored){}
                  return true;
                });
                hc.invalidate();
              }
            }
          }
          DrgPluginManager.H.postDelayed(this,450);
        }catch(Throwable ignored){}
      }
    },80);
  }

  private static void refreshNativeProxyList(BaseFragment bf,boolean recheck){
    try{
      for(Method m:bf.getClass().getDeclaredMethods()){
        if(recheck&&"checkProxyList".equals(m.getName())){
          m.setAccessible(true);
          if(m.getParameterTypes().length==0)m.invoke(bf);
        }else if("updateRows".equals(m.getName())){
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
  private static long pLong(Object p,String... nms){if(p==null)return 0L;for(Object t:new Object[]{p,getSettingsObj(p)}){if(t==null)continue;for(String n:nms){try{Field f=t.getClass().getDeclaredField(n);f.setAccessible(true);return f.getLong(t);}catch(Throwable ignored){}}}return 0L;}
  private static boolean pBool(Object p,String... nms){if(p==null)return false;for(Object t:new Object[]{p,getSettingsObj(p)}){if(t==null)continue;for(String n:nms){try{Field f=t.getClass().getDeclaredField(n);f.setAccessible(true);return f.getBoolean(t);}catch(Throwable ignored){}}}return false;}
  private static void pSet(Object p,Object val,String... nms){if(p==null)return;for(Object t:new Object[]{p,getSettingsObj(p)}){if(t==null)continue;for(String n:nms){try{Field f=t.getClass().getDeclaredField(n);f.setAccessible(true);f.set(t,val);return;}catch(Throwable ignored){}}}}
  private static String tgStr(String k,String uk,String ru,String en){return DrgPluginManager.tgStr(k,uk,ru,en);}
  private static String tr(String uk,String ru,String en){return DrgPluginManager.tr(uk,ru,en);}

  private static String readHttps(String urlStr){
    try{
      HttpURLConnection con=(HttpURLConnection)new URL(urlStr).openConnection();
      con.setConnectTimeout(3500);con.setReadTimeout(3500);
      try(BufferedReader br=new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"))){
        StringBuilder sb=new StringBuilder();String ln;
        while((ln=br.readLine())!=null)sb.append(ln);
        return sb.toString();
      }
    }catch(Throwable ignored){return "";}
  }

  private static void fetchCountryAsync(final String addr,final TextView tv){
    if(addr==null||addr.isEmpty()||tv==null)return;
    String cached=countryCache.get(addr);
    if(cached!=null){tv.setText(cached);return;}
    new Thread(()->{
      String cName="—";
      try{
        String ip=addr.trim();
        try{InetAddress ia=InetAddress.getByName(ip);if(ia!=null&&ia.getHostAddress()!=null)ip=ia.getHostAddress();}catch(Throwable ignored){}
        String iso="";
        String r1=readHttps("https://api.country.is/"+Uri.encode(ip));
        Matcher m1=Pattern.compile("\"country\"\\s*:\\s*\"([A-Z]{2})\"").matcher(r1);
        if(m1.find())iso=m1.group(1);
        if(iso.isEmpty()){
          String r2=readHttps("https://ipwho.is/"+Uri.encode(ip)+"?lang="+DrgPluginManager.lang());
          Matcher m2=Pattern.compile("\"country_code\"\\s*:\\s*\"([A-Z]{2})\"").matcher(r2);
          if(m2.find())iso=m2.group(1);
          Matcher m2c=Pattern.compile("\"country\"\\s*:\\s*\"([^\"]+)\"").matcher(r2);
          if(m2c.find()&&!m2c.group(1).isEmpty())cName=m2c.group(1);
        }
        if(iso.isEmpty()){
          String r3=readHttps("https://ipinfo.io/"+Uri.encode(ip)+"/country");
          if(r3!=null&&r3.trim().matches("[A-Z]{2}"))iso=r3.trim();
        }
        if(!iso.isEmpty()){
          Locale userLoc=new Locale(DrgPluginManager.lang());
          String disp=new Locale("",iso).getDisplayCountry(userLoc);
          if(disp!=null&&!disp.isEmpty()&&!disp.equalsIgnoreCase(iso))cName=disp;
        }
      }catch(Throwable ignored){}
      final String fc=cName;
      if(!"—".equals(fc))countryCache.put(addr,fc);
      DrgPluginManager.H.post(()->{try{tv.setText(fc);}catch(Throwable ignored){}});
    }).start();}
}
// === КІНЕЦЬ ФАЙЛУ ===
