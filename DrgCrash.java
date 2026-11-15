package org.telegram.messenger;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.ApplicationExitInfo;
import android.content.*;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.util.Log;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class DrgCrash {
  private static final String PREFS = "drg_crash_prefs";
  private static volatile boolean installed = false;
  private static volatile boolean handlingCrash = false;
  private static volatile String currentStage = "init";
  private static Context appCtx = null;

  private static SharedPreferences sp(Context c) {
    Context ctx = c != null ? c : appCtx;
    return ctx.getSharedPreferences(PREFS, 0);
  }

  public static boolean isCrashProcess(Context c) {
    try {
      String pn = null;
      if (Build.VERSION.SDK_INT >= 28) {
        pn = Application.getProcessName();
      }
      if (pn == null || pn.isEmpty()) {
        try (FileInputStream fis = new FileInputStream("/proc/self/cmdline")) {
          byte[] buf = new byte[128];
          int r = fis.read(buf);
          if (r > 0) {
            int len = 0;
            while (len < r && buf[len] != 0) len++;
            pn = new String(buf, 0, len);
          }
        } catch (Throwable ignored) {}
      }
      if ((pn == null || pn.isEmpty()) && c != null) {
        int pid = android.os.Process.myPid();
        ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
          List<ActivityManager.RunningAppProcessInfo> ps = am.getRunningAppProcesses();
          if (ps != null) {
            for (ActivityManager.RunningAppProcessInfo p : ps) {
              if (p != null && p.pid == pid) {
                pn = p.processName;
                break;
              }
            }
          }
        }
      }
      return pn != null && pn.endsWith(":drg_crash");
    } catch (Throwable ignored) {
      return false;
    }
  }

  public static void install(final Context ctx) {
    if (ctx != null) appCtx = ctx.getApplicationContext();
    final Context c = appCtx != null ? appCtx : ctx;
    if (c == null || isCrashProcess(c)) return;
    if (installed) return;
    installed = true;

    try {
      SharedPreferences p = sp(c);
      String prevStage = p.getString("stage", "");
      long prevTs = p.getLong("stage_ts", 0L);
      if (!prevStage.isEmpty() && !"dialogs_onResume".equals(prevStage) && !"ready".equals(prevStage)) {
        p.edit().putString("prev_incomplete_stage", prevStage).putLong("prev_incomplete_ts", prevTs).commit();
        // If previous launch died very early before LaunchActivity, save exit report immediately before anything else can crash
        if ("app_onCreate".equals(prevStage) || "postInit".equals(prevStage)) {
          String earlyReport = collectAbnormalExitReport(c, false);
          if (earlyReport != null) {
            p.edit().putString("last_saved_report", earlyReport).commit();
            saveTextAsyncJoin(c, earlyReport, 900L);
          }
        }
      }

      final Thread.UncaughtExceptionHandler prevHandler = Thread.getDefaultUncaughtExceptionHandler();
      Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
        if (!handlingCrash) {
          handlingCrash = true;
          try {
            StringBuilder jSb = new StringBuilder();
            jSb.append("TIME: ").append(fmtTime(System.currentTimeMillis())).append("\n");
            jSb.append("THREAD: ").append(t != null ? t.getName() : "?").append("\n");
            jSb.append("STAGE: ").append(currentStage).append("\n");
            jSb.append("TRACE:\n").append(Log.getStackTraceString(e)).append("\n");
            String javaCrash = jSb.toString();

            String fullReport = buildFullReport(c, javaCrash, currentStage, System.currentTimeMillis(), "");
            sp(c).edit()
              .putString("last_java_crash", javaCrash)
              .putString("last_saved_report", fullReport)
              .commit();

            saveTextAsyncJoin(c, fullReport, 1000L);

            try {
              Intent i = new Intent(c, DrgCrashActivity.class);
              i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
              i.putExtra("report", fullReport);
              c.startActivity(i);
            } catch (Throwable ignored) {}
          } catch (Throwable ignored) {}
        }
        if (prevHandler != null) prevHandler.uncaughtException(t, e);
      });
    } catch (Throwable ignored) {}
  }

  public static void stage(final String st) {
    currentStage = st;
    try {
      if (appCtx != null) {
        sp(appCtx).edit().putString("stage", st).putLong("stage_ts", System.currentTimeMillis()).apply();
      }
    } catch (Throwable ignored) {}
  }

  public static boolean checkAndShowOnLaunch(final Activity act) {
    if (act == null || isCrashProcess(act)) return false;
    final Context c = act.getApplicationContext();
    if (appCtx == null) appCtx = c;
    try {
      String report = collectAbnormalExitReport(c, true);
      if (report != null) {
        saveTextAsyncJoin(c, report, 900L);
        Intent i = new Intent(act, DrgCrashActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.putExtra("report", report);
        act.startActivity(i);
        return true;
      }
    } catch (Throwable ignored) {}
    return false;
  }

  private static String collectAbnormalExitReport(Context c, boolean markHandled) {
    try {
      SharedPreferences p = sp(c);
      String javaCrash = p.getString("last_java_crash", "");
      String incStage = p.getString("prev_incomplete_stage", "");
      long incTs = p.getLong("prev_incomplete_ts", 0L);
      long lastHandledTs = p.getLong("last_handled_exit_ts", 0L);

      boolean hasCrash = !javaCrash.isEmpty();
      long newestTs = lastHandledTs;
      StringBuilder exitSb = new StringBuilder();

      if (Build.VERSION.SDK_INT >= 30) {
        try {
          ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
          if (am != null) {
            List<ApplicationExitInfo> list = am.getHistoricalProcessExitReasons(c.getPackageName(), 0, 12);
            if (list != null && !list.isEmpty()) {
              for (int i = 0; i < list.size(); i++) {
                ApplicationExitInfo info = list.get(i);
                if (info == null) continue;
                long ts = info.getTimestamp();
                if (i == 0 && ts > newestTs) newestTs = ts;
                int r = info.getReason();
                if (i == 0 && ts > lastHandledTs && isAbnormalReason(r, incStage)) {
                  hasCrash = true;
                }
                exitSb.append("#").append(i + 1)
                  .append(" [").append(fmtTime(ts)).append("] ")
                  .append(reasonName(r))
                  .append(" status=").append(info.getStatus())
                  .append(" imp=").append(info.getImportance())
                  .append(" pss=").append(info.getPss() / 1024L).append("MB")
                  .append(" rss=").append(info.getRss() / 1024L).append("MB\n");
                if (info.getDescription() != null && !info.getDescription().isEmpty()) {
                  exitSb.append("  desc: ").append(info.getDescription()).append("\n");
                }
                String trace = readShortStream(info.getTraceInputStream(), 5000);
                if (!trace.isEmpty()) {
                  exitSb.append("  trace:\n").append(trace).append("\n");
                }
              }
            }
          }
        } catch (Throwable t) {
          exitSb.append("ExitInfo error: ").append(t.toString()).append("\n");
        }
      }

      if (!hasCrash) return null;

      String report = buildFullReport(c, javaCrash, incStage, incTs, exitSb.toString());
      if (markHandled) {
        p.edit()
          .putLong("last_handled_exit_ts", newestTs)
          .putString("last_saved_report", report)
          .remove("last_java_crash")
          .remove("prev_incomplete_stage")
          .commit();
      }
      return report;
    } catch (Throwable ignored) {}
    return null;
  }

  private static boolean isAbnormalReason(int r, String incStage) {
    if (r == 2 || r == 3 || r == 4 || r == 5 || r == 6 || r == 7 || r == 9) return true;
    if (r == 13 && incStage != null && !incStage.isEmpty() && !"dialogs_onResume".equals(incStage)) return true;
    return false;
  }

  private static String buildFullReport(Context c, String javaCrash, String incStage, long incTs, String exitInfoText) {
    StringBuilder report = new StringBuilder();
    report.append("=== DrgGram Crash Report ===\n");
    report.append("Time: ").append(fmtTime(System.currentTimeMillis())).append("\n");
    report.append("Device: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append(" (").append(Build.DEVICE).append(")\n");
    report.append("Android: ").append(Build.VERSION.RELEASE).append(" (SDK ").append(Build.VERSION.SDK_INT).append(")\n");
    report.append("Display: ").append(Build.DISPLAY).append("\n");
    report.append("App: ").append(appVersion(c)).append("\n");
    report.append("Account configs: ").append(countConfigFiles(c)).append("\n");
    report.append("RAM: ").append(memSummary(c)).append("\n");
    if (incStage != null && !incStage.isEmpty()) {
      report.append("Incomplete stage: ").append(incStage).append(" at ").append(fmtTime(incTs)).append("\n");
    }
    report.append("\n--- LAST JAVA CRASH ---\n");
    report.append(javaCrash == null || javaCrash.isEmpty() ? "(none)\n" : javaCrash).append("\n");
    report.append("--- SYSTEM EXIT REASONS (newest first) ---\n");
    report.append(exitInfoText == null || exitInfoText.isEmpty() ? "(none or SDK < 30)\n" : exitInfoText);
    return report.toString();
  }

  private static void saveTextAsyncJoin(final Context c, final String text, final long waitMs) {
    Thread th = new Thread(() -> saveTextNow(c, text));
    th.start();
    try { th.join(waitMs); } catch (Throwable ignored) {}
  }

  public static String saveTextNow(Context ctx, String content) {
    if (ctx == null || content == null) return null;
    byte[] data;
    try { data = content.getBytes("UTF-8"); } catch (Throwable t) { data = content.getBytes(); }
    String name = "DrgGram_crash_" + new SimpleDateFormat("MMdd_HHmmss", Locale.US).format(new Date()) + ".txt";

    try {
      File ext = ctx.getExternalFilesDir(null);
      if (ext != null) {
        if (!ext.exists()) ext.mkdirs();
        File bf = new File(ext, "DrgGram_crash.txt");
        try (FileOutputStream fos = new FileOutputStream(bf, false)) {
          fos.write(data);
          fos.flush();
        } catch (Throwable ignored) {}
      }
    } catch (Throwable ignored) {}

    try {
      if (Build.VERSION.SDK_INT >= 29) {
        ContentResolver cr = ctx.getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        cv.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
        cv.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        Uri uri = cr.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
        if (uri != null) {
          try (OutputStream os = cr.openOutputStream(uri)) {
            if (os != null) {
              os.write(data);
              os.flush();
              return "Download/" + name;
            }
          }
        }
      } else {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (dir != null) {
          if (!dir.exists()) dir.mkdirs();
          File f = new File(dir, name);
          try (FileOutputStream fos = new FileOutputStream(f, false)) {
            fos.write(data);
            fos.flush();
            return f.getAbsolutePath();
          }
        }
      }
    } catch (Throwable ignored) {}
    return null;
  }

  private static String readShortStream(InputStream is, int maxChars) {
    if (is == null) return "";
    try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line).append("\n");
        if (sb.length() >= maxChars) {
          sb.append("...(truncated)\n");
          break;
        }
      }
      return sb.toString();
    } catch (Throwable t) {
      return "";
    }
  }

  private static String reasonName(int r) {
    switch (r) {
      case 1: return "EXIT_SELF";
      case 2: return "SIGNALED";
      case 3: return "LOW_MEMORY";
      case 4: return "CRASH_JAVA";
      case 5: return "CRASH_NATIVE";
      case 6: return "ANR";
      case 7: return "INITIALIZATION_FAILURE";
      case 8: return "PERMISSION_CHANGE";
      case 9: return "EXCESSIVE_RESOURCE_USAGE";
      case 10: return "USER_REQUESTED";
      case 11: return "USER_STOPPED";
      case 12: return "DEPENDENCY_DIED";
      case 13: return "OTHER";
      case 14: return "FREEZER";
      case 15: return "PACKAGE_STATE_CHANGE";
      case 16: return "PACKAGE_UPDATED";
      default: return "REASON_" + r;
    }
  }

  private static String fmtTime(long ts) {
    if (ts <= 0) return "-";
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date(ts));
  }

  private static int countConfigFiles(Context c) {
    try {
      File spDir = new File(c.getApplicationInfo().dataDir, "shared_prefs");
      File[] files = spDir.listFiles();
      if (files == null) return 0;
      int cnt = 0;
      for (File f : files) {
        String n = f.getName();
        if (n.startsWith("userconf") && n.endsWith(".xml") && f.length() > 80) cnt++;
      }
      return cnt;
    } catch (Throwable t) {
      return -1;
    }
  }

  private static String appVersion(Context c) {
    try {
      PackageInfo pi = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
      return pi.versionName + " (" + pi.versionCode + ")";
    } catch (Throwable t) {
      return "?";
    }
  }

  private static String memSummary(Context c) {
    try {
      ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
      ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
      am.getMemoryInfo(mi);
      long heapMax = Runtime.getRuntime().maxMemory() / (1024 * 1024);
      long heapUsed = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
      return "avail=" + (mi.availMem / (1024 * 1024)) + "MB / total=" + (mi.totalMem / (1024 * 1024))
        + "MB, lowMem=" + mi.lowMemory + ", javaHeap=" + heapUsed + "/" + heapMax + "MB";
    } catch (Throwable t) {
      return "?";
    }
  }
}
// === КІНЕЦЬ ФАЙЛУ ===
