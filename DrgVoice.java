package org.telegram.messenger;
import android.media.AudioRecord;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DrgVoice {
  private static final String[] LANGS={"en","uk","ru","ar","fa","tr","de","fr","es","it","pt","pl","id","hi","zh","ja","ko","uz","kk","be","az","vi","th","nl","cs","ro","he"};
  private static final String[][] V_NAMES={
    {"OFF","ВИМК","ВЫКЛ","إيقاف","خاموش","KAPALI","AUS","DÉSACTIVÉ","NO","OFF","DESLIGADO","WYŁ","MATI","बंद","关闭","オフ","끄기","O'CHIQ","ӨШІРУЛІ","ВЫКЛ","SÖNÜLÜ","TẮT","ปิด","UIT","VYP","OPRIT","כבוי"},
    {"Soft Female","Ніжний жіночий","Нежный женский","أنثوي ناعم","زنانه لطیف","Yumuşak Kadın","Sanfte Frau","Femme douce","Mujer suave","Donna dolce","Feminina suave","Delikatna kobieta","Wanita Lembut","सॉफ्ट फीमेल","温柔女声","優しい女性","부드러운 여성","Mayin ayol","Нәзік әйел","Пяшчотны жаночы","Zərif qadın","Nữ nhẹ nhàng","หญิงเสียงนุ่ม","Zachte vrouw","Jemný ženský","Femeie blândă","אישה רכה"},
    {"Deep Female","Оксамитовий жіночий","Бархатный женский","أنثوي عميق","زنانه بم","Kalın Kadın","Tiefe Frau","Femme grave","Mujer grave","Donna profonda","Feminina grave","Głęboka kobieta","Wanita Berat","डीप फीमेल","御姐女声","落ち着いた女性","중저음 여성","Yo'g'on ayol","Қоңыр әйел","Аксамітны жаночы","Qalın qadın","Nữ trầm","หญิงเสียงทุ้ม","Diepe vrouw","Hluboký ženský","Femeie gravă","אישה עמוקה"},
    {"Soft Male","Ніжний чоловічий","Нежный мужской","ذكري ناعم","مردانه ملایم","Yumuşak Erkek","Sanfter Mann","Homme doux","Hombre suave","Uomo dolce","Masculina suave","Łagodny mężczyzna","Pria Lembut","सॉफ्ट मेल","温柔男声","優しい男性","부드러운 남성","Mayin erkak","Нәзік ер","Пяшчотны мужчынскі","Zərif kişi","Nam nhẹ nhàng","ชายเสียงนุ่ม","Zachte man","Jemný mužský","Bărbat blând","גבר רך"},
    {"Deep Male","Грубий чоловічий","Грубый мужской","ذكري عميق","مردانه بم","Kalın Erkek","Tiefer Mann","Homme grave","Hombre grave","Uomo profondo","Masculina grave","Niski mężczyzna","Pria Berat","डीप मेल","低沉男声","低い男性","중저음 남성","Yo'g'on erkak","Жуан ер","Грубый мужчынскі","Qalın kişi","Nam trầm","ชายเสียงทุ้ม","Diepe man","Hluboký mužský","Bărbat grav","גבר עמוק"},
    {"Robotic","Роботизований","Роботизированный","آلي","رباتیک","Robotik","Robotisch","Robotique","Robótico","Robotico","Robótico","Robotyczny","Robotik","रोबोटिक","机械音","ロボット","로봇","Robot","Робот","Рабатызаваны","Robot","Robot","หุ่นยนต์","Robotachtig","Robotický","Robotic","רובוטי"},
    {"Child","Дитина","Ребёнок","طفل","کودک","Çocuk","Kind","Enfant","Niño","Bambino","Criança","Dziecko","Anak-anak","बच्चा","童声","子供","어린이","Bola","Бала","Дзіця","Uşaq","Trẻ em","เด็ก","Kind","Dítě","Copil","ילד"},
    {"Studio Radio","Студійний / Радіо","Студийный / Радио","استوديو / راديو","استودیو رادیو","Stüdyo Radyo","Studio Radio","Studio Radio","Estudio Radio","Studio Radio","Estúdio Rádio","Studio Radio","Radio Studio","स्टूडियो रेडियो","电台播音","スタジオラジオ","스튜디오 라디오","Studiya radio","Студия радио","Студыйны / Радыё","Studiya radio","Phòng thu Radio","สตูดิโอเรดิโอ","Studio Radio","Studiový / Rádio","Studio Radio","אולפן רדיו"},
    {"Mickey Mouse","Міккі Маус","Микки Маус","ميكي ماوس","میکی ماوس","Miki Fare","Micky Maus","Mickey Mouse","Mickey Mouse","Topolino","Mickey Mouse","Myszka Miki","Mickey Mouse","मिकी माउस","米老鼠","ミッキーマウス","미키 마우스","Mikki Maus","Микки Маус","Мікі Маўс","Mikki Maus","Chuột Mickey","มิกกี้เมาส์","Mickey Mouse","Myšák Mickey","Mickey Mouse","מיקי מאוס"},
    {"Demon","Демон / Монстр","Демон / Монстр","شيطان / وحش","هیولا","İblis / Canavar","Dämon","Démon","Demonio","Demone","Demônio","Demon","Iblis","राक्षस","恶魔","悪魔","악마","Iblis","Жын / Монстр","Дэман / Монстар","İblis","Ác quỷ","ปีศาจ","Demon","Démon","Demon","שד"},
    {"Walkie-Talkie","Рація / Мегафон","Рация / Мегафон","لاسلكي","بی‌سیم","Telsiz","Funkgerät","Talkie-walkie","Walkie-talkie","Walkie-talkie","Rádio comunicador","Krótkofalówka","Walkie-Talkie","वॉकी-टॉकी","对讲机","トランシーバー","무전기","Ratsiya","Рация","Рацыя / Мегафон","Ratsiya","Bộ đàm","วิทยุสื่อสาร","Portofoon","Vysílačka","Walkie-Talkie","מכשיר קשר"},
    {"Anime Cute","Аніме / Милий","Аниме / Милый","أنمي لطيف","انیمه ناز","Anime Tatlı","Anime Süß","Anime Mignon","Anime Lindo","Anime Dolce","Anime Fofo","Anime Słodki","Anime Imut","एनीमे क्यूट","二次元萌音","アニメ風","애니메 큐트","Anime yoqimli","Аниме сүйкімді","Анімэ / Мілы","Anime şirin","Anime dễ thương","อนิเมะน่ารัก","Anime Schattig","Anime Roztomilý","Anime Drăguț","אנימה חמוד"}
  };

  private static final int IN_SIZE=16384,IN_MASK=16383,OUT_SIZE=16384,OUT_MASK=16383;
  private static final float[] inBuf=new float[IN_SIZE],outBuf=new float[OUT_SIZE];
  private static float[] hann=new float[0],prevTail=new float[0];
  private static int sr=0,hopOut=160,winLen=320,search=80,lastMode=-1;
  private static long inWrite=0,anchor=0,outWrite=0,outRead=0,lastCallMs=0;
  private static boolean hasPrevTail=false;
  private static double robP1=0,robP2=0;
  private static final Biquad bq1=new Biquad(),bq2=new Biquad(),bq3=new Biquad(),bq4=new Biquad();
  private static final Object LOCK=new Object();

  private static class Biquad {
    double b0=1,b1=0,b2=0,a1=0,a2=0,x1=0,x2=0,y1=0,y2=0;
    void reset(){x1=x2=y1=y2=0;}
    void setIdentity(){b0=1;b1=b2=a1=a2=0;}
    void setHPF(double fs,double fc,double q){
      double w0=2.0*Math.PI*Math.min(fc,fs*0.42)/fs,cs=Math.cos(w0),al=Math.sin(w0)/(2.0*q),a0=1.0+al;
      b0=((1.0+cs)/2.0)/a0;b1=(-(1.0+cs))/a0;b2=b0;a1=(-2.0*cs)/a0;a2=(1.0-al)/a0;
    }
    void setLPF(double fs,double fc,double q){
      double w0=2.0*Math.PI*Math.min(fc,fs*0.45)/fs,cs=Math.cos(w0),al=Math.sin(w0)/(2.0*q),a0=1.0+al;
      b0=((1.0-cs)/2.0)/a0;b1=(1.0-cs)/a0;b2=b0;a1=(-2.0*cs)/a0;a2=(1.0-al)/a0;
    }
    void setPeak(double fs,double fc,double q,double db){
      double A=Math.pow(10.0,db/40.0),w0=2.0*Math.PI*Math.min(fc,fs*0.44)/fs,cs=Math.cos(w0),al=Math.sin(w0)/(2.0*q),a0=1.0+al/A;
      b0=(1.0+al*A)/a0;b1=(-2.0*cs)/a0;b2=(1.0-al*A)/a0;a1=b1;a2=(1.0-al/A)/a0;
    }
    void setHighShelf(double fs,double fc,double db){
      double A=Math.pow(10.0,db/40.0),w0=2.0*Math.PI*Math.min(fc,fs*0.44)/fs,cs=Math.cos(w0),sn=Math.sin(w0),bt=2.0*Math.sqrt(A)*sn/Math.sqrt(2.0);
      double a0=(A+1.0)-(A-1.0)*cs+bt;
      b0=(A*((A+1.0)+(A-1.0)*cs+bt))/a0;b1=(-2.0*A*((A-1.0)+(A+1.0)*cs))/a0;b2=(A*((A+1.0)+(A-1.0)*cs-bt))/a0;
      a1=(2.0*((A-1.0)-(A+1.0)*cs))/a0;a2=((A+1.0)-(A-1.0)*cs-bt)/a0;
    }
    float run(float x){
      double y=b0*x+b1*x1+b2*x2-a1*y1-a2*y2;
      x2=x1;x1=x;y2=y1;y1=y;
      return (float)y;
    }
  }

  public static String[] getVoiceNames(){
    String l=DrgLang.lang();
    int li=0;
    if("ua".equals(l))li=1;
    else for(int i=0;i<LANGS.length;i++)if(LANGS[i].equals(l)){li=i;break;}
    String[] out=new String[V_NAMES.length];
    for(int i=0;i<V_NAMES.length;i++)out[i]=li<V_NAMES[i].length?V_NAMES[i][li]:V_NAMES[i][0];
    return out;
  }

  private static void setup(int sampleRate,int mode){
    if(sampleRate<=0)sampleRate=16000;
    boolean srChg=(sr!=sampleRate);
    if(srChg){
      sr=sampleRate;
      hopOut=Math.max(80,sr/100);
      winLen=hopOut*2;
      search=Math.max(32,sr/200);
      hann=new float[winLen];
      for(int k=0;k<winLen;k++)hann[k]=0.5f*(1f-(float)Math.cos(2.0*Math.PI*k/winLen));
      prevTail=new float[hopOut];
    }
    if(srChg||lastMode!=mode){
      lastMode=mode;
      bq1.reset();bq2.reset();bq3.reset();bq4.reset();
      bq1.setIdentity();bq2.setIdentity();bq3.setIdentity();bq4.setIdentity();
      if(mode==1){// Soft Female (Anti-chipmunk)
        bq1.setHPF(sr,145,0.72);bq2.setPeak(sr,420,1.0,3.5);bq3.setPeak(sr,2850,1.3,-4.5);bq4.setHighShelf(sr,5600,-2.5);
      }else if(mode==2){// Deep Female
        bq1.setHPF(sr,125,0.72);bq2.setPeak(sr,360,1.0,4.0);bq3.setPeak(sr,2600,1.3,-3.5);bq4.setLPF(sr,6400,0.7);
      }else if(mode==3){// Soft Male
        bq1.setHPF(sr,85,0.7);bq2.setPeak(sr,180,1.0,2.2);bq3.setPeak(sr,2600,1.2,1.5);
      }else if(mode==4){// Deep Male
        bq1.setHPF(sr,65,0.7);bq2.setPeak(sr,120,0.9,3.8);bq3.setLPF(sr,4800,0.7);
      }else if(mode==5){// Robotic
        bq1.setHPF(sr,240,0.8);bq2.setLPF(sr,3600,0.8);
      }else if(mode==6){// Child
        bq1.setHPF(sr,210,0.72);bq2.setPeak(sr,650,1.1,2.5);bq3.setPeak(sr,3400,1.3,-3.0);
      }else if(mode==7){// Studio Radio
        bq1.setHPF(sr,75,0.75);bq2.setPeak(sr,115,0.9,4.5);bq3.setPeak(sr,3100,1.1,3.0);
      }else if(mode==8){// Mickey Mouse
        bq1.setHPF(sr,260,0.75);bq2.setPeak(sr,950,1.1,3.5);bq3.setPeak(sr,2400,1.2,2.5);bq4.setLPF(sr,6500,0.7);
      }else if(mode==9){// Demon
        bq1.setHPF(sr,50,0.7);bq2.setPeak(sr,95,0.9,4.5);bq3.setLPF(sr,3500,0.75);
      }else if(mode==10){// Walkie-Talkie
        bq1.setHPF(sr,520,1.1);bq2.setPeak(sr,1400,1.2,4.0);bq3.setLPF(sr,2300,1.1);
      }else if(mode==11){// Anime Cute
        bq1.setHPF(sr,190,0.72);bq2.setPeak(sr,520,1.0,3.2);bq3.setPeak(sr,3100,1.3,-3.5);bq4.setHighShelf(sr,5000,2.0);
      }
    }
    if(srChg||(System.currentTimeMillis()-lastCallMs>600L)){
      java.util.Arrays.fill(inBuf,0f);
      java.util.Arrays.fill(outBuf,0f);
      inWrite=anchor=outWrite=outRead=0;
      hasPrevTail=false;robP1=robP2=0;
    }
    lastCallMs=System.currentTimeMillis();
  }

  private static double pitchForMode(int m){
    switch(m){
      case 1:return 1.54;
      case 2:return 1.34;
      case 3:return 0.90;
      case 4:return 0.76;
      case 5:return 0.96;
      case 6:return 1.72;
      case 7:return 0.93;
      case 8:return 2.05;
      case 9:return 0.66;
      case 10:return 1.00;
      case 11:return 1.66;
      default:return 1.00;
    }
  }

  private static float interp(double pos){
    int i=(int)pos;
    float f=(float)(pos-i);
    return inBuf[i&IN_MASK]*(1f-f)+inBuf[(i+1)&IN_MASK]*f;
  }

  public static int readAudio(AudioRecord rec,ByteBuffer buf,int size,int mode){
    int len=rec.read(buf,size);
    if(len>0&&mode>0)processByteBufferPcm(buf,len,rec.getSampleRate(),mode);
    return len;
  }
  public static int readAudio(AudioRecord rec,short[] s,int off,int size,int mode){
    int len=rec.read(s,off,size);
    if(len>0&&mode>0)processShortsPcm(s,off,len,rec.getSampleRate(),mode);
    return len;
  }
  public static int readAudio(AudioRecord rec,byte[] b,int off,int size,int mode){
    int len=rec.read(b,off,size);
    if(len>1&&mode>0){
      int cnt=len/2;short[] s=new short[cnt];
      for(int i=0;i<cnt;i++)s[i]=(short)((b[off+i*2]&0xFF)|(b[off+i*2+1]<<8));
      processShortsPcm(s,0,cnt,rec.getSampleRate(),mode);
      for(int i=0;i<cnt;i++){b[off+i*2]=(byte)(s[i]&0xFF);b[off+i*2+1]=(byte)((s[i]>>8)&0xFF);}
    }
    return len;
  }

  public static void processByteBufferPcm(ByteBuffer buf,int len,int sampleRate,int mode){
    if(mode<=0||buf==null||len<2)return;
    try{
      buf.order(ByteOrder.LITTLE_ENDIAN);
      int cnt=len/2;short[] s=new short[cnt];
      for(int i=0;i<cnt;i++)s[i]=buf.getShort(i*2);
      processShortsPcm(s,0,cnt,sampleRate,mode);
      for(int i=0;i<cnt;i++)buf.putShort(i*2,s[i]);
    }catch(Throwable ignored){}
  }

  public static void processShortsPcm(short[] pcm,int off,int count,int sampleRate,int mode){
    if(mode<=0||pcm==null||count<=0)return;
    synchronized(LOCK){
      setup(sampleRate,mode);
      double pitch=pitchForMode(mode);
      for(int i=0;i<count;i++){
        inBuf[(int)(inWrite&IN_MASK)]=pcm[off+i]/32768.0f;
        inWrite++;
      }
      if(Math.abs(pitch-1.0)>0.01){
        int need=(int)Math.ceil(winLen*pitch)+search+8;
        int stepCorr=Math.max(1,hopOut/28);
        int coarseStep=Math.max(2,search/24);
        while((inWrite-anchor)>=need){
          long bestStart=anchor;
          if(hasPrevTail){
            double bestScore=-1e18;
            int bestD=0;
            for(int d=-search;d<=search;d+=coarseStep){
              long cand=anchor+d;
              double num=0,den=1e-9;
              for(int k=0;k<hopOut;k+=stepCorr){
                float a=prevTail[k],b=interp(cand+k*pitch);
                num+=a*b;den+=b*b;
              }
              double sc=num/Math.sqrt(den);
              if(sc>bestScore){bestScore=sc;bestD=d;}
            }
            for(int d=Math.max(-search,bestD-coarseStep+1);d<=Math.min(search,bestD+coarseStep-1);d++){
              long cand=anchor+d;
              double num=0,den=1e-9;
              for(int k=0;k<hopOut;k+=stepCorr){
                float a=prevTail[k],b=interp(cand+k*pitch);
                num+=a*b;den+=b*b;
              }
              double sc=num/Math.sqrt(den);
              if(sc>bestScore){bestScore=sc;bestD=d;}
            }
            bestStart=anchor+bestD;
          }
          for(int k=0;k<winLen;k++){
            float s=interp(bestStart+k*pitch);
            if(k>=hopOut)prevTail[k-hopOut]=s;
            outBuf[(int)((outWrite+k)&OUT_MASK)]+=s*hann[k];
          }
          hasPrevTail=true;
          outWrite+=hopOut;
          anchor+=hopOut;
        }
      }else{
        while(anchor<inWrite){
          outBuf[(int)(outWrite&OUT_MASK)]=inBuf[(int)(anchor&IN_MASK)];
          outWrite++;anchor++;
        }
      }
      for(int i=0;i<count;i++){
        float y=0f;
        if(outRead<outWrite){
          int idx=(int)(outRead&OUT_MASK);
          y=outBuf[idx];
          outBuf[idx]=0f;
          outRead++;
        }
        if(mode==5){
          robP1+=2.0*Math.PI*55.0/sr;if(robP1>2.0*Math.PI)robP1-=2.0*Math.PI;
          robP2+=2.0*Math.PI*110.0/sr;if(robP2>2.0*Math.PI)robP2-=2.0*Math.PI;
          y=y*(0.65f+0.45f*(float)Math.sin(robP1)+0.2f*(float)Math.cos(robP2));
        }else if(mode==9){
          robP1+=2.0*Math.PI*32.0/sr;if(robP1>2.0*Math.PI)robP1-=2.0*Math.PI;
          y=y*(0.85f+0.25f*(float)Math.sin(robP1))*1.18f;
        }else if(mode==10){
          y=(float)Math.tanh(y*2.6f)*0.65f;
        }else if(mode==7){
          y=(float)Math.tanh(y*1.45f)*0.9f;
        }
        y=bq1.run(y);y=bq2.run(y);y=bq3.run(y);
        if(mode==1||mode==2||mode==8||mode==11){y=bq4.run(y);y*=1.12f;}
        y=y/(1.0f+Math.abs(y)*0.18f);
        int out=Math.round(y*32767.0f);
        pcm[off+i]=(short)Math.max(-32768,Math.min(32767,out));
      }
    }
  }
}
// === КІНЕЦЬ ФАЙЛУ ===
