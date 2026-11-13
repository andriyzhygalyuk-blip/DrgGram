package org.telegram.messenger;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;

public class DrgVoiceUI {
  public interface OnPick { void onPick(int mode); }

  public static Drawable makeVoiceSvg(final int mode,final int color){
    return new Drawable(){
      private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
      {p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(AndroidUtilities.dp(1.7f));p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);}
      @Override public void draw(Canvas c){
        Rect b=getBounds();float cx=b.exactCenterX(),cy=b.exactCenterY(),r=Math.min(b.width(),b.height())*0.37f;
        Path path=new Path();
        if(mode==-1){// Checkmark
          path.moveTo(cx-r*0.6f,cy);path.lineTo(cx-r*0.15f,cy+r*0.48f);path.lineTo(cx+r*0.65f,cy-r*0.45f);
          c.drawPath(path,p);
        }else if(mode==0){// OFF: Muted mic
          RectF rf=new RectF(cx-r*0.26f,cy-r*0.75f,cx+r*0.26f,cy+r*0.2f);
          c.drawRoundRect(rf,r*0.26f,r*0.26f,p);
          c.drawArc(new RectF(cx-r*0.52f,cy-r*0.35f,cx+r*0.52f,cy+r*0.48f),0,180,false,p);
          c.drawLine(cx,cy+r*0.48f,cx,cy+r*0.85f,p);
          c.drawLine(cx-r*0.82f,cy-r*0.72f,cx+r*0.82f,cy+r*0.72f,p);
        }else if(mode==1){// Soft Female
          c.drawCircle(cx,cy-r*0.26f,r*0.46f,p);
          c.drawLine(cx,cy+r*0.2f,cx,cy+r*0.92f,p);
          c.drawLine(cx-r*0.34f,cy+r*0.58f,cx+r*0.34f,cy+r*0.58f,p);
        }else if(mode==2){// Velvet Female (Lips)
          path.moveTo(cx-r*0.85f,cy);
          path.quadTo(cx-r*0.36f,cy-r*0.52f,cx,cy-r*0.22f);
          path.quadTo(cx+r*0.36f,cy-r*0.52f,cx+r*0.85f,cy);
          path.quadTo(cx,cy+r*0.72f,cx-r*0.85f,cy);
          c.drawPath(path,p);
          c.drawLine(cx-r*0.65f,cy,cx+r*0.65f,cy,p);
        }else if(mode==3){// Soft Male
          c.drawCircle(cx-r*0.18f,cy+r*0.18f,r*0.46f,p);
          c.drawLine(cx+r*0.15f,cy-r*0.15f,cx+r*0.74f,cy-r*0.74f,p);
          c.drawLine(cx+r*0.3f,cy-r*0.74f,cx+r*0.74f,cy-r*0.74f,p);
          c.drawLine(cx+r*0.74f,cy-r*0.3f,cx+r*0.74f,cy-r*0.74f,p);
        }else if(mode==4){// Deep Male (Mustache)
          path.moveTo(cx,cy-r*0.08f);
          path.cubicTo(cx-r*0.35f,cy-r*0.5f,cx-r*0.72f,cy+r*0.22f,cx-r*0.95f,cy-r*0.05f);
          path.cubicTo(cx-r*0.7f,cy+r*0.52f,cx-r*0.2f,cy+r*0.22f,cx,cy+r*0.06f);
          path.cubicTo(cx+r*0.2f,cy+r*0.22f,cx+r*0.7f,cy+r*0.52f,cx+r*0.95f,cy-r*0.05f);
          path.cubicTo(cx+r*0.72f,cy+r*0.22f,cx+r*0.35f,cy-r*0.5f,cx,cy-r*0.08f);
          c.drawPath(path,p);
        }else if(mode==5){// Robotic
          c.drawLine(cx,cy-r*0.38f,cx,cy-r*0.76f,p);
          c.drawCircle(cx,cy-r*0.84f,r*0.1f,p);
          c.drawRoundRect(new RectF(cx-r*0.72f,cy-r*0.38f,cx+r*0.72f,cy+r*0.72f),r*0.2f,r*0.2f,p);
          c.drawCircle(cx-r*0.28f,cy+r*0.05f,r*0.12f,p);
          c.drawCircle(cx+r*0.28f,cy+r*0.05f,r*0.12f,p);
          c.drawLine(cx-r*0.32f,cy+r*0.42f,cx+r*0.32f,cy+r*0.42f,p);
        }else if(mode==6){// Child (Balloon)
          c.drawOval(new RectF(cx-r*0.55f,cy-r*0.88f,cx+r*0.55f,cy+r*0.28f),p);
          path.moveTo(cx-r*0.12f,cy+r*0.28f);path.lineTo(cx,cy+r*0.42f);path.lineTo(cx+r*0.12f,cy+r*0.28f);
          path.moveTo(cx,cy+r*0.42f);path.quadTo(cx-r*0.22f,cy+r*0.68f,cx+r*0.1f,cy+r*0.95f);
          c.drawPath(path,p);
        }else if(mode==7){// Studio Radio (Condenser Mic)
          c.drawRoundRect(new RectF(cx-r*0.28f,cy-r*0.86f,cx+r*0.28f,cy+r*0.14f),r*0.28f,r*0.28f,p);
          c.drawArc(new RectF(cx-r*0.52f,cy-r*0.38f,cx+r*0.52f,cy+r*0.42f),0,180,false,p);
          c.drawLine(cx,cy+r*0.42f,cx,cy+r*0.86f,p);
          c.drawLine(cx-r*0.38f,cy+r*0.86f,cx+r*0.38f,cy+r*0.86f,p);
        }else if(mode==8){// Mickey Mouse
          c.drawCircle(cx,cy+r*0.2f,r*0.52f,p);
          c.drawCircle(cx-r*0.54f,cy-r*0.44f,r*0.32f,p);
          c.drawCircle(cx+r*0.54f,cy-r*0.44f,r*0.32f,p);
        }else if(mode==9){// Demon
          c.drawCircle(cx,cy+r*0.18f,r*0.55f,p);
          path.moveTo(cx-r*0.35f,cy-r*0.28f);path.quadTo(cx-r*0.72f,cy-r*0.55f,cx-r*0.58f,cy-r*0.92f);
          path.quadTo(cx-r*0.32f,cy-r*0.65f,cx-r*0.15f,cy-r*0.35f);
          path.moveTo(cx+r*0.35f,cy-r*0.28f);path.quadTo(cx+r*0.72f,cy-r*0.55f,cx+r*0.58f,cy-r*0.92f);
          path.quadTo(cx+r*0.32f,cy-r*0.65f,cx+r*0.15f,cy-r*0.35f);
          c.drawPath(path,p);
          c.drawLine(cx-r*0.32f,cy+r*0.05f,cx-r*0.12f,cy+r*0.18f,p);
          c.drawLine(cx+r*0.32f,cy+r*0.05f,cx+r*0.12f,cy+r*0.18f,p);
        }else if(mode==10){// Walkie-Talkie
          c.drawLine(cx-r*0.28f,cy-r*0.42f,cx-r*0.28f,cy-r*0.92f,p);
          c.drawRoundRect(new RectF(cx-r*0.48f,cy-r*0.42f,cx+r*0.48f,cy+r*0.86f),r*0.16f,r*0.16f,p);
          c.drawRect(new RectF(cx-r*0.28f,cy-r*0.22f,cx+r*0.28f,cy+r*0.18f),p);
          c.drawLine(cx-r*0.24f,cy+r*0.42f,cx+r*0.24f,cy+r*0.42f,p);
          c.drawLine(cx-r*0.24f,cy+r*0.62f,cx+r*0.24f,cy+r*0.62f,p);
        }else if(mode==11){// Anime (Cat-ears)
          c.drawArc(new RectF(cx-r*0.56f,cy-r*0.28f,cx+r*0.56f,cy+r*0.78f),-35,250,false,p);
          path.moveTo(cx-r*0.46f,cy-r*0.02f);path.lineTo(cx-r*0.56f,cy-r*0.72f);path.lineTo(cx-r*0.12f,cy-r*0.26f);
          path.moveTo(cx+r*0.12f,cy-r*0.26f);path.lineTo(cx+r*0.56f,cy-r*0.72f);path.lineTo(cx+r*0.46f,cy-r*0.02f);
          c.drawPath(path,p);
          c.drawLine(cx-r*0.78f,cy+r*0.2f,cx-r*0.48f,cy+r*0.24f,p);
          c.drawLine(cx+r*0.48f,cy+r*0.24f,cx+r*0.78f,cy+r*0.2f,p);
        }else{// mode==12 Armor Pierced (Tank)
          c.drawRoundRect(new RectF(cx-r*0.86f,cy+r*0.24f,cx+r*0.86f,cy+r*0.72f),r*0.24f,r*0.24f,p);
          c.drawCircle(cx-r*0.46f,cy+r*0.48f,r*0.09f,p);
          c.drawCircle(cx,cy+r*0.48f,r*0.09f,p);
          c.drawCircle(cx+r*0.46f,cy+r*0.48f,r*0.09f,p);
          path.moveTo(cx-r*0.64f,cy+r*0.24f);path.lineTo(cx-r*0.48f,cy-r*0.04f);
          path.lineTo(cx+r*0.48f,cy-r*0.04f);path.lineTo(cx+r*0.64f,cy+r*0.24f);
          c.drawPath(path,p);
          c.drawRoundRect(new RectF(cx-r*0.36f,cy-r*0.46f,cx+r*0.24f,cy-r*0.04f),r*0.12f,r*0.12f,p);
          c.drawLine(cx+r*0.24f,cy-r*0.26f,cx+r*0.95f,cy-r*0.26f,p);
        }
      }
      @Override public void setAlpha(int a){p.setAlpha(a);}
      @Override public void setColorFilter(ColorFilter cf){p.setColorFilter(cf);}
      @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
      @Override public int getIntrinsicWidth(){return AndroidUtilities.dp(24);}
      @Override public int getIntrinsicHeight(){return AndroidUtilities.dp(24);}
    };
  }

  public static void openVoicePicker(final Context ctx,final String title,final int curMode,final OnPick cb){
    final String[] names=DrgVoice.getVoiceNames();
    final AlertDialog[] dlgRef={null};
    ScrollView sv=new ScrollView(ctx);
    LinearLayout list=new LinearLayout(ctx);
    list.setOrientation(LinearLayout.VERTICAL);
    list.setPadding(0,AndroidUtilities.dp(6),0,AndroidUtilities.dp(6));
    int txtCol=Theme.getColor(Theme.key_dialogTextBlack);
    if(txtCol==0)txtCol=Color.WHITE;
    int blueCol=Theme.getColor(Theme.key_dialogTextBlue2);
    if(blueCol==0)blueCol=0xFF3390EC;
    int icCol=Theme.getColor(Theme.key_dialogIcon);
    if(icCol==0)icCol=txtCol;
    int selBg=Theme.getColor(Theme.key_listSelector);

    for(int i=0;i<names.length;i++){
      final int idx=i;
      boolean isSel=(i==curMode);
      LinearLayout row=new LinearLayout(ctx);
      row.setOrientation(LinearLayout.HORIZONTAL);
      row.setGravity(Gravity.CENTER_VERTICAL);
      row.setMinimumHeight(AndroidUtilities.dp(48));
      row.setPadding(AndroidUtilities.dp(20),AndroidUtilities.dp(8),AndroidUtilities.dp(20),AndroidUtilities.dp(8));
      try{row.setBackground(Theme.createSelectorDrawable(selBg,2));}catch(Throwable ignored){}

      ImageView iv=new ImageView(ctx);
      iv.setImageDrawable(makeVoiceSvg(i,isSel?blueCol:icCol));
      LinearLayout.LayoutParams ilp=new LinearLayout.LayoutParams(AndroidUtilities.dp(24),AndroidUtilities.dp(24));
      ilp.setMarginEnd(AndroidUtilities.dp(16));
      row.addView(iv,ilp);

      TextView tv=new TextView(ctx);
      tv.setText(names[i]);
      tv.setTextColor(isSel?blueCol:txtCol);
      tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,15.5f);
      if(isSel){
        try{tv.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));}catch(Throwable ignored){}
      }
      row.addView(tv,new LinearLayout.LayoutParams(0,-2,1f));

      if(isSel){
        ImageView chk=new ImageView(ctx);
        chk.setImageDrawable(makeVoiceSvg(-1,blueCol));
        row.addView(chk,new LinearLayout.LayoutParams(AndroidUtilities.dp(20),AndroidUtilities.dp(20)));
      }
      row.setOnClickListener(v->{
        try{if(dlgRef[0]!=null)dlgRef[0].dismiss();}catch(Throwable ignored){}
        if(cb!=null)cb.onPick(idx);
      });
      list.addView(row,new LinearLayout.LayoutParams(-1,-2));
    }
    sv.addView(list);
    AlertDialog.Builder b=new AlertDialog.Builder(ctx);
    b.setTitle(title);
    b.setView(sv);
    b.setNegativeButton(DrgPluginManager.tgStr("Close","Закрити","Закрыть","Close"),null);
    AlertDialog d=b.create();
    dlgRef[0]=d;
    d.show();
  }
}
// === КІНЕЦЬ ФАЙЛУ ===
