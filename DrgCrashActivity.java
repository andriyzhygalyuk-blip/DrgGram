package org.telegram.messenger;

import android.app.Activity;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;

public class DrgCrashActivity extends Activity {
  private String reportText = "";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    try {
      requestWindowFeature(Window.FEATURE_NO_TITLE);
    } catch (Throwable ignored) {}

    reportText = getIntent() != null ? getIntent().getStringExtra("report") : null;
    if (reportText == null || reportText.isEmpty()) {
      reportText = getSharedPreferences("drg_crash_prefs", 0).getString("last_saved_report", "No crash report data.");
    }

    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setBackgroundColor(0xFF10141A);
    int pad = dp(16);
    root.setPadding(pad, dp(42), pad, dp(24));

    TextView title = new TextView(this);
    title.setText("DrgGram Crash Report");
    title.setTextColor(0xFFFF6B6B);
    title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 19);
    title.setTypeface(Typeface.DEFAULT_BOLD);
    root.addView(title);

    TextView sub = new TextView(this);
    sub.setText("Скопіюйте цей текст або зробіть скріншот і надішліть розробнику:");
    sub.setTextColor(0xFFB0BEC5);
    sub.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
    LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-1, -2);
    subLp.topMargin = dp(6);
    subLp.bottomMargin = dp(12);
    root.addView(sub, subLp);

    ScrollView sv = new ScrollView(this);
    sv.setBackgroundColor(0xFF181E27);
    TextView tv = new TextView(this);
    tv.setText(reportText);
    tv.setTextColor(0xFFE8EEF5);
    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11.5f);
    tv.setTypeface(Typeface.MONOSPACE);
    tv.setTextIsSelectable(true);
    tv.setPadding(dp(12), dp(12), dp(12), dp(12));
    sv.addView(tv, new ScrollView.LayoutParams(-1, -2));
    root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1f));

    LinearLayout btns = new LinearLayout(this);
    btns.setOrientation(LinearLayout.HORIZONTAL);
    LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(-1, -2);
    bLp.topMargin = dp(12);
    root.addView(btns, bLp);

    Button bCopy = makeBtn("Копіювати", 0xFF2ECC71);
    bCopy.setOnClickListener(v -> {
      try {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("DrgGram Crash", reportText));
        Toast.makeText(this, "Скопійовано в буфер!", Toast.LENGTH_SHORT).show();
      } catch (Throwable ignored) {}
    });
    btns.addView(bCopy, new LinearLayout.LayoutParams(0, dp(46), 1f));

    Button bSave = makeBtn("В Download", 0xFF3498DB);
    LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(0, dp(46), 1f);
    sLp.leftMargin = dp(8);
    bSave.setOnClickListener(v -> {
      String path = DrgCrash.saveTextNow(this, reportText);
      Toast.makeText(this, path != null ? ("Збережено: " + path) : "Не вдалося зберегти", Toast.LENGTH_LONG).show();
    });
    btns.addView(bSave, sLp);

    Button bOpen = makeBtn("Запустити", 0xFF546E7A);
    LinearLayout.LayoutParams oLp = new LinearLayout.LayoutParams(0, dp(46), 1f);
    oLp.leftMargin = dp(8);
    bOpen.setOnClickListener(v -> {
      try {
        Intent launch = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (launch != null) {
          launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
          startActivity(launch);
        }
      } catch (Throwable ignored) {}
      finish();
    });
    btns.addView(bOpen, oLp);

    setContentView(root);
  }

  private Button makeBtn(String txt, int color) {
    Button b = new Button(this);
    b.setText(txt);
    b.setTextColor(Color.WHITE);
    b.setBackgroundColor(color);
    b.setAllCaps(false);
    b.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
    return b;
  }

  private int dp(int v) {
    return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
  }
}
// === КІНЕЦЬ ФАЙЛУ ===
