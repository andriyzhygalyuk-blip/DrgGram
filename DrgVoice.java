package org.telegram.messenger;

import android.media.AudioRecord;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Random;

public class DrgVoice {
  private static final String[] LANGS = {"en","uk","ru","ar","fa","tr","de","fr","es","it","pt","pl","id","hi","zh","ja","ko","uz","kk","be","az","vi","th","nl","cs","ro","he"};
  private static final String[][] V_NAMES = {
    {"OFF","ВИМК","ВЫКЛ","إيقاف","خاموش","KAPALI","AUS","DÉSACTIVÉ","NO","OFF","DESLIGADO","WYŁ","MATI","बंद","关闭","オフ","끄기","O'CHIQ","ӨШІРУЛІ","ВЫКЛ","SÖNÜLÜ","TẮT","ปิด","UIT","VYP","OPRIT","כבוי"},
    {"Soft Female","Жіночий ніжний","Женский нежный","أنثوي ناعم","زنانه لطیف","Yumuşak Kadın","Sanfte Frau","Femme douce","Mujer suave","Donna dolce","Feminina suave","Delikatna kobieta","Wanita Lembut","सॉफ्ट फीमेल","温柔女声","優しい女性","부드러운 여성","Mayin ayol","Нәзік әйел","Пяшчотны жаночы","Zərif qadın","Nữ nhẹ nhàng","หญิงเสียงนุ่ม","Zachte vrouw","Jemný ženský","Femeie blândă","אישה רכה"},
    {"Velvet Female","Жіночий оксамитовий","Женский бархатный","أنثوي مخملي","زنانه مخملی","Kadife Kadın","Samtige Frau","Femme velours","Mujer terciopelo","Donna velluto","Feminina aveludada","Aksamitna kobieta","Wanita Lembut Dalam","वेलवेट फीमेल","御姐女声","落ち着いた女性","중저음 여성","Baxmal ayol","Қоңыр әйел","Аксамітны жаночы","Məxməri qadın","Nữ trầm ấm","หญิงเสียงทุ้มนุ่ม","Fluwelen vrouw","Sametový ženský","Femeie catifelată","אישה קטיפתית"},
    {"Soft Male","Чоловічий ніжний","Мужской нежный","ذكري ناعم","مردانه ملایم","Yumuşak Erkek","Sanfter Mann","Homme doux","Hombre suave","Uomo dolce","Masculina suave","Łagodny mężczyzna","Pria Lembut","सॉफ्ट मेल","温柔男声","優しい男性","부드러운 남성","Mayin erkak","Нәзік ер","Пяшчотны мужчынскі","Zərif kişi","Nam nhẹ nhàng","ชายเสียงนุ่ม","Zachte man","Jemný mužský","Bărbat blând","גבר רך"},
    {"Deep Male","Чоловічий грубий","Мужской грубый","ذكري عميق","مردانه بم","Kalın Erkek","Tiefer Mann","Homme grave","Hombre grave","Uomo profondo","Masculina grave","Niski mężczyzna","Pria Berat","डीप मेल","低沉男声","低い男性","중저음 남성","Yo'g'on erkak","Жуан ер","Грубый мужчынскі","Qalın kişi","Nam trầm","ชายเสียงทุ้ม","Diepe man","Hluboký mužský","Bărbat grav","גבר עמוק"},
    {"Robotic","Роботизований","Роботизированный","آلي","رباتیک","Robotik","Robotisch","Robotique","Robótico","Robotico","Robótico","Robotyczny","Robotik","रोबोटिक","机械音","ロボット","로봇","Robot","Робот","Рабатызаваны","Robot","Robot","หุ่นยนต์","Robotachtig","Robotický","Robotic","רובוטי"},
    {"Child","Дитячий","Детский","طفل","کودک","Çocuk","Kind","Enfant","Niño","Bambino","Criança","Dziecko","Anak-anak","बच्चा","童声","子供","어린이","Bola","Бала","Дзіцячы","Uşaq","Trẻ em","เด็ก","Kind","Dítě","Copil","ילד"},
    {"Studio Radio","Студійне радіо","Студийное радио","راديو استوديو","استودیو رادیو","Stüdyo Radyo","Studio-Radio","Radio Studio","Estudio Radio","Studio Radio","Estúdio Rádio","Radio Studio","Radio Studio","स्टूडियो रेडियो","电台播音","スタジオラジオ","스튜디오 라디오","Studiya radio","Студия радио","Студыйнае радыё","Studiya radio","Phòng thu Radio","สตูดิโอเรดิโอ","Studio Radio","Studiové rádio","Radio de studio","רדיו אולפן"},
    {"Mickey Mouse","Міккі Маус","Микки Маус","ميكي ماوس","میکی ماوس","Miki Fare","Micky Maus","Mickey Mouse","Mickey Mouse","Topolino","Mickey Mouse","Myszka Miki","Mickey Mouse","मिकी माउस","米老鼠","ミッキーマウス","미키 마우스","Mikki Maus","Микки Маус","Мікі Маўс","Mikki Maus","Chuột Mickey","มิกกี้เมาส์","Mickey Mouse","Myšák Mickey","Mickey Mouse","מיקי מאוס"},
    {"Demon","Демон","Демон","شيطان","اهریمن","İblis","Dämon","Démon","Demonio","Demone","Demônio","Demon","Iblis","राक्षस","恶魔","悪魔","악마","Iblis","Жын","Дэман","İblis","Ác quỷ","ปีศาจ","Demon","Démon","Demon","שד"},
    {"Walkie-Talkie","Рація","Рация","لاسلكي","بی‌سیم","Telsiz","Funkgerät","Talkie-walkie","Walkie-talkie","Walkie-talkie","Rádio comunicador","Krótkofalówka","Walkie-Talkie","वॉकी-टॉकी","对讲机","トランシーバー","무전기","Ratsiya","Рация","Рацыя","Ratsiya","Bộ đàm","วิทยุสื่อสาร","Portofoon","Vysílačka","Stație radio","מכשיר קשר"},
    {"Anime","Аніме","Аниме","أنمي","انیمه","Anime","Anime","Anime","Anime","Anime","Anime","Anime","Anime","एनीमे","二次元","アニメ","애니메","Anime","Аниме","Анімэ","Anime","Anime","อนิเมะ","Anime","Anime","Anime","אנימה"},
    {"Armor Pierced","Є пробиття","Есть пробитие","تم الاختراق","نفوذ انجام شد","Zırh Delindi","Panzerung durchschlagen","Blindage percé","Blindaje perforado","Corazza perforata","Blindagem perfurada","Pancerz przebity","Tembus Baja","कवच भेदा गया","击穿装甲","装甲貫通","장갑 관통","Zirh teshildi","Сауыт тесілді","Ёсць прабіццё","Zireh deşildi","Xuyên giáp","เจาะเกราะสำเร็จ","Pantser doorboord","Průraz pancíře","Blindaj străpuns","השריון נפרץ"}
  };

  private static final int BUF_SIZE = 32768;
  private static final int BUF_MASK = BUF_SIZE - 1;
  private static final float[] inBuf = new float[BUF_SIZE];
  private static final float[] inLpfBuf = new float[BUF_SIZE];
  private static final float[] f0LpBuf = new float[BUF_SIZE];
  private static final float[] outBuf = new float[BUF_SIZE];
  private static final float[] outWgt = new float[BUF_SIZE];
  private static float[] yinDiff = new float[1024];

  private static int sr = 0;
  private static int lastMode = -1;
  private static int minPeriod = 38, maxPeriod = 246, lookAhead = 500, ioDelay = 746, yinWin = 213, yinStep = 1;
  private static long inWrite = 0, synPos = 0, markPos = 0, outRead = 0, lastCallMs = 0;

  private static float dcX1 = 0f, dcY1 = 0f;
  private static float noiseFloor = 0.0025f, gateEnv = 0f, gateGain = 1f;
  private static float lastT0 = 0f, smoothedF0 = 0f, compEnv = 0f, tankPhase = 0f;
  private static float subPhase = 0f;
  private static int combDelay = 88;
  private static float[] combBuf = new float[512];
  private static int combIdx = 0;

  private static final Biquad preHpf = new Biquad();
  private static final Biquad aaLpf = new Biquad();
  private static final Biquad f0Lpf = new Biquad();
  private static final Biquad bq1 = new Biquad(), bq2 = new Biquad(), bq3 = new Biquad(), bq4 = new Biquad();
  private static final Random rnd = new Random();
  private static final Object LOCK = new Object();

  private static class Biquad {
    double b0 = 1, b1 = 0, b2 = 0, a1 = 0, a2 = 0, x1 = 0, x2 = 0, y1 = 0, y2 = 0;
    void reset() { x1 = x2 = y1 = y2 = 0; }
    void setIdentity() { b0 = 1; b1 = b2 = a1 = a2 = 0; }
    void setHPF(double fs, double fc, double q) {
      double w0 = 2 * Math.PI * Math.min(fc, fs * 0.42) / fs;
      double cs = Math.cos(w0), al = Math.sin(w0) / (2 * q), a0 = 1 + al;
      b0 = ((1 + cs) / 2) / a0; b1 = (-(1 + cs)) / a0; b2 = b0; a1 = (-2 * cs) / a0; a2 = (1 - al) / a0;
    }
    void setLPF(double fs, double fc, double q) {
      double w0 = 2 * Math.PI * Math.min(fc, fs * 0.45) / fs;
      double cs = Math.cos(w0), al = Math.sin(w0) / (2 * q), a0 = 1 + al;
      b0 = ((1 - cs) / 2) / a0; b1 = (1 - cs) / a0; b2 = b0; a1 = (-2 * cs) / a0; a2 = (1 - al) / a0;
    }
    void setPeak(double fs, double fc, double q, double db) {
      double A = Math.pow(10, db / 40), w0 = 2 * Math.PI * Math.min(fc, fs * 0.44) / fs;
      double cs = Math.cos(w0), al = Math.sin(w0) / (2 * q), a0 = 1 + al / A;
      b0 = (1 + al * A) / a0; b1 = (-2 * cs) / a0; b2 = (1 - al * A) / a0; a1 = b1; a2 = (1 - al / A) / a0;
    }
    void setHighShelf(double fs, double fc, double db) {
      double A = Math.pow(10, db / 40), w0 = 2 * Math.PI * Math.min(fc, fs * 0.44) / fs;
      double cs = Math.cos(w0), sn = Math.sin(w0), bt = 2 * Math.sqrt(A) * sn / Math.sqrt(2);
      double a0 = (A + 1) - (A - 1) * cs + bt;
      b0 = (A * ((A + 1) + (A - 1) * cs + bt)) / a0;
      b1 = (-2 * A * ((A - 1) + (A + 1) * cs)) / a0;
      b2 = (A * ((A + 1) + (A - 1) * cs - bt)) / a0;
      a1 = (2 * ((A - 1) - (A + 1) * cs)) / a0;
      a2 = ((A + 1) - (A - 1) * cs - bt) / a0;
    }
    float run(float x) {
      double y = b0 * x + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2;
      x2 = x1; x1 = x; y2 = y1; y1 = y;
      return (float) y;
    }
  }

  public static String[] getVoiceNames() {
    String l = DrgLang.lang();
    int li = 0;
    if ("ua".equals(l)) li = 1;
    else for (int i = 0; i < LANGS.length; i++) if (LANGS[i].equals(l)) { li = i; break; }
    String[] out = new String[V_NAMES.length];
    for (int i = 0; i < V_NAMES.length; i++) out[i] = li < V_NAMES[i].length ? V_NAMES[i][li] : V_NAMES[i][0];
    return out;
  }

  private static double formantForMode(int m) {
    switch (m) {
      case 1: return 1.16;  // Soft Female
      case 2: return 1.11;  // Velvet Female
      case 3: return 0.96;  // Soft Male
      case 4: return 0.88;  // Deep Male
      case 5: return 1.00;  // Robotic
      case 6: return 1.24;  // Child
      case 7: return 0.97;  // Studio Radio
      case 8: return 1.38;  // Mickey Mouse
      case 9: return 0.82;  // Demon
      case 10: return 1.00; // Walkie-Talkie
      case 11: return 1.21; // Anime
      case 12: return 0.95; // Armor Pierced
      default: return 1.00;
    }
  }

  private static double targetF0ForMode(int m) {
    switch (m) {
      case 1: return 205.0;
      case 2: return 176.0;
      case 3: return 112.0;
      case 4: return 84.0;
      case 5: return 122.0;
      case 6: return 260.0;
      case 7: return 0.0;   // subtle fixed ratio 0.96
      case 8: return 305.0;
      case 9: return 68.0;
      case 10: return 0.0;  // natural pitch
      case 11: return 242.0;
      case 12: return 0.0;  // subtle fixed ratio 0.94
      default: return 0.0;
    }
  }

  private static double fallbackPitchForMode(int m) {
    switch (m) {
      case 1: return 1.68;
      case 2: return 1.46;
      case 3: return 0.90;
      case 4: return 0.72;
      case 5: return 1.00;
      case 6: return 2.05;
      case 7: return 0.96;
      case 8: return 2.35;
      case 9: return 0.62;
      case 10: return 1.00;
      case 11: return 1.92;
      case 12: return 0.94;
      default: return 1.00;
    }
  }

  private static void setup(int sampleRate, int mode) {
    if (sampleRate <= 0) sampleRate = 16000;
    boolean srChg = (sr != sampleRate);
    boolean modeChg = (lastMode != mode);
    if (srChg) {
      sr = sampleRate;
      yinStep = Math.max(1, sr / 16000);
      minPeriod = Math.max(20, sr / 420);
      maxPeriod = Math.min(900, sr / 65);
      yinWin = Math.max(120, Math.min(maxPeriod, sr / 75));
      lookAhead = maxPeriod * 2 + 16;
      ioDelay = lookAhead + maxPeriod + 16;
      int maxLagIdx = (maxPeriod / yinStep) + 4;
      if (yinDiff.length < maxLagIdx) yinDiff = new float[maxLagIdx];
      combDelay = Math.max(32, Math.min(480, (int) (sr * 0.0055)));
      if (combBuf.length < combDelay + 16) combBuf = new float[combDelay + 16];
      preHpf.setHPF(sr, 75, 0.707);
      f0Lpf.setLPF(sr, 750, 0.707);
    }
    if (srChg || modeChg) {
      lastMode = mode;
      preHpf.reset();
      f0Lpf.reset();
      aaLpf.reset();
      double fRatio = formantForMode(mode);
      if (fRatio > 1.03) aaLpf.setLPF(sr, (sr * 0.43) / fRatio, 0.75);
      else aaLpf.setIdentity();

      bq1.reset(); bq2.reset(); bq3.reset(); bq4.reset();
      bq1.setIdentity(); bq2.setIdentity(); bq3.setIdentity(); bq4.setIdentity();
      if (mode == 1) { bq1.setHPF(sr, 115, 0.72); bq2.setPeak(sr, 320, 1.0, 1.8); bq3.setPeak(sr, 3200, 1.2, -2.0); bq4.setHighShelf(sr, 5800, 1.5); }
      else if (mode == 2) { bq1.setHPF(sr, 100, 0.72); bq2.setPeak(sr, 260, 0.9, 3.0); bq3.setPeak(sr, 2900, 1.2, -2.2); bq4.setLPF(sr, 7000, 0.7); }
      else if (mode == 3) { bq1.setHPF(sr, 80, 0.7); bq2.setPeak(sr, 190, 1.0, 2.0); bq3.setPeak(sr, 2800, 1.2, 1.2); }
      else if (mode == 4) { bq1.setHPF(sr, 60, 0.7); bq2.setPeak(sr, 115, 0.9, 3.8); bq3.setPeak(sr, 2200, 1.1, 1.5); bq4.setLPF(sr, 5600, 0.7); }
      else if (mode == 5) { bq1.setHPF(sr, 220, 0.8); bq2.setPeak(sr, 1200, 1.2, 2.5); bq3.setLPF(sr, 4200, 0.8); }
      else if (mode == 6) { bq1.setHPF(sr, 160, 0.72); bq2.setPeak(sr, 600, 1.1, 2.0); bq3.setLPF(sr, 6500, 0.72); }
      else if (mode == 7) { bq1.setHPF(sr, 75, 0.75); bq2.setPeak(sr, 115, 0.9, 4.2); bq3.setPeak(sr, 3400, 1.1, 3.2); }
      else if (mode == 8) { bq1.setHPF(sr, 220, 0.75); bq2.setPeak(sr, 1100, 1.1, 3.0); bq3.setLPF(sr, 6800, 0.72); }
      else if (mode == 9) { bq1.setHPF(sr, 45, 0.7); bq2.setPeak(sr, 90, 0.9, 4.5); bq3.setLPF(sr, 3800, 0.75); }
      else if (mode == 10) { bq1.setHPF(sr, 480, 1.05); bq2.setPeak(sr, 1450, 1.25, 4.5); bq3.setLPF(sr, 2350, 1.05); }
      else if (mode == 11) { bq1.setHPF(sr, 150, 0.72); bq2.setPeak(sr, 3200, 1.2, 1.5); bq3.setHighShelf(sr, 5500, 2.5); }
      else if (mode == 12) { bq1.setHPF(sr, 360, 1.05); bq2.setPeak(sr, 1180, 1.5, 5.2); bq3.setLPF(sr, 2450, 1.1); }
    }
    long now = System.currentTimeMillis();
    if (srChg || (now - lastCallMs > 600L)) {
      Arrays.fill(inBuf, 0f);
      Arrays.fill(inLpfBuf, 0f);
      Arrays.fill(f0LpBuf, 0f);
      Arrays.fill(outBuf, 0f);
      Arrays.fill(outWgt, 0f);
      Arrays.fill(combBuf, 0f);
      inWrite = 0;
      synPos = 0;
      markPos = 0;
      outRead = -ioDelay;
      dcX1 = dcY1 = 0f;
      noiseFloor = 0.0025f;
      gateEnv = 0f;
      gateGain = 1f;
      lastT0 = 0f;
      smoothedF0 = 0f;
      compEnv = 0f;
      tankPhase = 0f;
      subPhase = 0f;
      combIdx = 0;
    }
    lastCallMs = now;
  }

  private static float interpCubic(float[] buf, double pos) {
    int i1 = (int) Math.floor(pos);
    float t = (float) (pos - i1);
    float y0 = buf[(i1 - 1) & BUF_MASK];
    float y1 = buf[i1 & BUF_MASK];
    float y2 = buf[(i1 + 1) & BUF_MASK];
    float y3 = buf[(i1 + 2) & BUF_MASK];
    float c0 = y1;
    float c1 = 0.5f * (y2 - y0);
    float c2 = y0 - 2.5f * y1 + 2.0f * y2 - 0.5f * y3;
    float c3 = 0.5f * (y3 - y0) + 1.5f * (y1 - y2);
    return ((c3 * t + c2) * t + c1) * t + c0;
  }

  private static final float[] yinRes = new float[2]; // [0]=T0, [1]=voiced (1 or 0)

  private static void detectPitchYin(long centerPos) {
    int halfW = yinWin / 2;
    long base = centerPos - halfW;
    if (base < 0) {
      yinRes[0] = Math.max(32f, sr / 100f);
      yinRes[1] = 0f;
      return;
    }
    float energy = 0f;
    int zc = 0;
    float prev = inBuf[(int) (base & BUF_MASK)];
    int checkCnt = 0;
    for (int j = 0; j < yinWin; j += yinStep) {
      float cur = inBuf[(int) ((base + j) & BUF_MASK)];
      energy += cur * cur;
      if ((prev >= 0f && cur < 0f) || (prev < 0f && cur >= 0f)) zc++;
      prev = cur;
      checkCnt++;
    }
    float rms = (float) Math.sqrt(energy / Math.max(1, checkCnt));
    float zcr = (float) zc / Math.max(1, checkCnt);
    if (rms < Math.max(0.004f, noiseFloor * 1.4f) || zcr > 0.36f) {
      yinRes[0] = Math.max(32f, sr / 100f);
      yinRes[1] = 0f;
      return;
    }

    int minLagIdx = Math.max(1, minPeriod / yinStep);
    int maxLagIdx = Math.max(minLagIdx + 2, maxPeriod / yinStep);
    double runningSum = 0.0;
    yinDiff[0] = 1f;
    int bestIdx = -1;
    float bestVal = 10f;
    int globalIdx = minLagIdx;
    float globalMin = 10f;

    for (int ti = 1; ti <= maxLagIdx; ti++) {
      int lag = ti * yinStep;
      double sum = 0.0;
      for (int j = 0; j < yinWin; j += yinStep) {
        float d = inBuf[(int) ((base + j) & BUF_MASK)] - inBuf[(int) ((base + j + lag) & BUF_MASK)];
        sum += d * d;
      }
      runningSum += sum;
      float cmnd = runningSum > 1e-12 ? (float) (sum * ti / runningSum) : 1f;
      yinDiff[ti] = cmnd;

      if (ti >= minLagIdx) {
        if (cmnd < globalMin) { globalMin = cmnd; globalIdx = ti; }
        if (bestIdx < 0 && cmnd < 0.24f) {
          bestIdx = ti;
          bestVal = cmnd;
        } else if (bestIdx >= 0 && cmnd < bestVal) {
          bestIdx = ti;
          bestVal = cmnd;
        } else if (bestIdx >= 0 && cmnd > bestVal + 0.04f) {
          break;
        }
      }
    }

    if (bestIdx < 0) {
      if (globalMin < 0.38f) {
        bestIdx = globalIdx;
        bestVal = globalMin;
      } else {
        yinRes[0] = Math.max(32f, sr / 100f);
        yinRes[1] = 0f;
        return;
      }
    }

    float betterIdx = bestIdx;
    if (bestIdx > minLagIdx && bestIdx < maxLagIdx) {
      float s0 = yinDiff[bestIdx - 1];
      float s1 = yinDiff[bestIdx];
      float s2 = yinDiff[bestIdx + 1];
      float denom = 2f * (2f * s1 - s2 - s0);
      if (Math.abs(denom) > 1e-6f) {
        float shift = (s0 - s2) / denom;
        if (shift > -1f && shift < 1f) betterIdx += shift;
      }
    }

    float t0 = betterIdx * yinStep;
    if (lastT0 > 0f) {
      if (Math.abs(t0 - lastT0 * 2f) < lastT0 * 0.18f && bestVal > 0.10f) t0 = lastT0;
      else if (Math.abs(t0 - lastT0 * 0.5f) < lastT0 * 0.12f && bestVal > 0.10f) t0 = lastT0;
      else if (Math.abs(t0 - lastT0) < lastT0 * 0.35f) t0 = lastT0 * 0.35f + t0 * 0.65f;
    }
    t0 = Math.max(minPeriod, Math.min(maxPeriod, t0));
    lastT0 = t0;
    float f0 = sr / t0;
    if (smoothedF0 <= 0f) smoothedF0 = f0;
    else smoothedF0 = smoothedF0 * 0.965f + f0 * 0.035f;

    yinRes[0] = t0;
    yinRes[1] = 1f;
  }

  private static long alignPeak(long approxPos, float t0) {
    int radius = Math.max(8, (int) (t0 * 0.25f));
    long best = approxPos;
    float maxVal = -1e9f;
    for (int d = -radius; d <= radius; d++) {
      long p = approxPos + d;
      if (p < 0 || p >= inWrite) continue;
      float v = f0LpBuf[(int) (p & BUF_MASK)];
      if (v > maxVal) {
        maxVal = v;
        best = p;
      }
    }
    return best;
  }

  public static int readAudio(AudioRecord rec, ByteBuffer buf, int size, int mode) {
    int len = rec.read(buf, size);
    if (len > 0 && mode > 0) processByteBufferPcm(buf, len, rec.getSampleRate(), mode);
    return len;
  }

  public static int readAudio(AudioRecord rec, short[] s, int off, int size, int mode) {
    int len = rec.read(s, off, size);
    if (len > 0 && mode > 0) processShortsPcm(s, off, len, rec.getSampleRate(), mode);
    return len;
  }

  public static int readAudio(AudioRecord rec, byte[] b, int off, int size, int mode) {
    int len = rec.read(b, off, size);
    if (len > 1 && mode > 0) {
      int cnt = len / 2;
      short[] s = new short[cnt];
      for (int i = 0; i < cnt; i++) s[i] = (short) ((b[off + i * 2] & 0xFF) | (b[off + i * 2 + 1] << 8));
      processShortsPcm(s, 0, cnt, rec.getSampleRate(), mode);
      for (int i = 0; i < cnt; i++) {
        b[off + i * 2] = (byte) (s[i] & 0xFF);
        b[off + i * 2 + 1] = (byte) ((s[i] >> 8) & 0xFF);
      }
    }
    return len;
  }

  public static void processByteBufferPcm(ByteBuffer buf, int len, int sampleRate, int mode) {
    if (mode <= 0 || buf == null || len < 2) return;
    try {
      buf.order(ByteOrder.LITTLE_ENDIAN);
      int cnt = len / 2;
      short[] s = new short[cnt];
      for (int i = 0; i < cnt; i++) s[i] = buf.getShort(i * 2);
      processShortsPcm(s, 0, cnt, sampleRate, mode);
      for (int i = 0; i < cnt; i++) buf.putShort(i * 2, s[i]);
    } catch (Throwable ignored) {}
  }

  public static void processShortsPcm(short[] pcm, int off, int count, int sampleRate, int mode) {
    if (mode <= 0 || pcm == null || count <= 0) return;
    synchronized (LOCK) {
      setup(sampleRate, mode);
      double fRatioBase = formantForMode(mode);
      double targetF0 = targetF0ForMode(mode);
      double fallbackPitch = fallbackPitchForMode(mode);
      boolean isRobotic = (mode == 5);

      for (int i = 0; i < count; i++) {
        float x = pcm[off + i] / 32768.0f;
        // DC-blocker
        float dc = x - dcX1 + 0.995f * dcY1;
        dcX1 = x;
        dcY1 = dc;
        float clean = preHpf.run(dc);

        // Adaptive soft noise gate before pitch processing
        float abs = Math.abs(clean);
        gateEnv = gateEnv * 0.992f + abs * 0.008f;
        if (gateEnv < noiseFloor * 1.8f) noiseFloor = noiseFloor * 0.999f + gateEnv * 0.001f;
        noiseFloor = Math.max(0.001f, Math.min(0.015f, noiseFloor));
        float targetGate = gateEnv > noiseFloor * 1.9f ? 1.0f : Math.max(0.18f, gateEnv / (noiseFloor * 1.9f));
        gateGain = gateGain * 0.985f + targetGate * 0.015f;
        clean *= gateGain;

        int idx = (int) (inWrite & BUF_MASK);
        inBuf[idx] = clean;
        inLpfBuf[idx] = fRatioBase > 1.03 ? aaLpf.run(clean) : clean;
        f0LpBuf[idx] = f0Lpf.run(clean);
        inWrite++;
      }

      while ((inWrite - synPos) >= lookAhead) {
        detectPitchYin(synPos);
        float t0 = yinRes[0];
        boolean voiced = (yinRes[1] > 0.5f);

        double pRatio = 1.0;
        double fRatio = 1.0;
        if (voiced) {
          fRatio = fRatioBase;
          if (isRobotic) {
            pRatio = (targetF0 * t0) / sr;
            pRatio = Math.max(0.55, Math.min(1.85, pRatio));
          } else if (targetF0 > 0.0 && smoothedF0 > 55f) {
            pRatio = targetF0 / smoothedF0;
            if (targetF0 > 150.0) pRatio = Math.max(1.08, Math.min(2.45, pRatio));
            else pRatio = Math.max(0.55, Math.min(1.04, pRatio));
          } else {
            pRatio = fallbackPitch;
          }

          if (markPos < synPos - t0 * 0.6) {
            while (markPos < synPos - t0 * 0.5) {
              markPos = alignPeak(markPos + (long) Math.round(t0), t0);
            }
          }
          if (Math.abs(synPos - markPos) > t0 * 1.5) {
            markPos = alignPeak(synPos, t0);
          }
        } else {
          markPos = synPos;
        }

        int synStep = Math.max(16, (int) Math.round(voiced ? (t0 / pRatio) : t0));
        int halfWin = voiced ? Math.max(24, Math.min(maxPeriod, (int) Math.round(t0))) : synStep;
        float[] srcBuf = (voiced && fRatio > 1.03) ? inLpfBuf : inBuf;

        for (int k = -halfWin; k <= halfWin; k++) {
          float w = 0.5f * (1f + (float) Math.cos(Math.PI * k / halfWin));
          float s = voiced ? interpCubic(srcBuf, markPos + k * fRatio) : inBuf[(int) ((synPos + k) & BUF_MASK)];
          int oIdx = (int) ((synPos + k) & BUF_MASK);
          outBuf[oIdx] += s * w;
          outWgt[oIdx] += w;
        }

        synPos += synStep;
      }

      for (int i = 0; i < count; i++) {
        float y = 0f;
        if (outRead >= 0) {
          int rIdx = (int) (outRead & BUF_MASK);
          float w = outWgt[rIdx];
          y = w > 1e-4f ? (outBuf[rIdx] / Math.max(0.25f, w)) : outBuf[rIdx];
          outBuf[rIdx] = 0f;
          outWgt[rIdx] = 0f;
        }
        outRead++;

        if (mode == 5) { // Robotic comb resonator
          int rPos = combIdx - combDelay;
          if (rPos < 0) rPos += combBuf.length;
          float delayed = combBuf[rPos];
          float cOut = y * 0.72f + delayed * 0.42f;
          combBuf[combIdx] = cOut;
          combIdx++;
          if (combIdx >= combBuf.length) combIdx = 0;
          y = cOut;
        } else if (mode == 7) { // Studio radio subtle warmth saturation
          y = (float) Math.tanh(y * 1.38f) * 0.88f;
        } else if (mode == 9) { // Demon sub-octave + dark presence
          subPhase += 2.0 * Math.PI * 46.0 / sr;
          if (subPhase > 2.0 * Math.PI) subPhase -= 2.0 * Math.PI;
          int rPos = combIdx - combDelay;
          if (rPos < 0) rPos += combBuf.length;
          float dark = combBuf[rPos];
          combBuf[combIdx] = y;
          combIdx++;
          if (combIdx >= combBuf.length) combIdx = 0;
          y = y * (0.90f + 0.16f * (float) Math.sin(subPhase)) + dark * 0.22f;
        } else if (mode == 10) { // Walkie-talkie PTT speaker
          float gate = Math.min(1.0f, gateEnv * 14f);
          float squelch = (rnd.nextFloat() * 2f - 1f) * 0.018f * gate;
          y = (float) Math.tanh(y * 2.5f) * 0.65f + squelch;
        } else if (mode == 12) { // Armor pierced / Tank laryngophone
          tankPhase += 2.0 * Math.PI * 82.0 / sr;
          if (tankPhase > 2.0 * Math.PI) tankPhase -= 2.0 * Math.PI;
          float gate = Math.min(1.0f, gateEnv * 12f);
          float tank = (rnd.nextFloat() * 2f - 1f) * 0.045f * gate + (float) Math.sin(tankPhase) * 0.025f * gate;
          y = (float) Math.tanh(y * 3.1f + 0.08f) * 0.62f + tank;
        }

        y = bq1.run(y);
        y = bq2.run(y);
        y = bq3.run(y);
        if (mode == 1 || mode == 2 || mode == 4 || mode == 11) y = bq4.run(y);

        // Smooth optical compressor / limiter
        float aY = Math.abs(y);
        compEnv = aY > compEnv ? (compEnv * 0.85f + aY * 0.15f) : (compEnv * 0.996f + aY * 0.004f);
        if (compEnv > 0.35f) y *= (0.35f + (compEnv - 0.35f) * 0.35f) / compEnv;
        y = y / (1.0f + Math.abs(y) * 0.14f);
        if (Float.isNaN(y)) y = 0f;

        int out = Math.round(y * 32767.0f);
        pcm[off + i] = (short) Math.max(-32768, Math.min(32767, out));
      }
    }
  }
}
// === КІНЕЦЬ ФАЙЛУ ===
