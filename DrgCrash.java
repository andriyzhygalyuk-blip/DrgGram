package org.telegram.messenger;

import android.app.ActivityManager;
import android.app.ApplicationExitInfo;
import android.content.*;
import android.content.pm.PackageInfo;
import android.database.Cursor;
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
  private static volatile boolean dumpedThisRun = false;
  private static volatile boolean handlingCrash = false;
  private static volatile String currentStage = "init";
  private static Context appCtx = null;

  private static SharedPreferences sp(Context c) {
    Context ctx = c != null ? c : (appCtx != null ? appCtx : ApplicationLoader.applicationContext);
    return ctx.getSharedPreferences(PREFS, 0);
  }

  public static void install(final Context ctx) {
    if (ctx != null) appCtx = ctx.getApplicationContext();
    if (installed) return;
    installed = true;
    try {
      SharedPreferences p = sp(ctx);
      String prevStage = p.getString("stage", "");
      long prevTs = p.getLong("stage_ts", 0L);
      if (!prevStage.isEmpty() && !"dialogs_onResume".equals(prevStage) && !"ready".equals(prevStage)) {
        p.edit().putString("prev_incomplete_stage", prevStage).putLong("prev_incomplete_ts", prevTs).apply();
      }
      final Thread.UncaughtExceptionHandler prevHandler = Thread.getDefaultUncaughtExceptionHandler();
      Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
        if (!handlingCrash) {
          handlingCrash = true;
          try {
            StringBuilder sb = new StringBuilder();
            sb.append("TIME: ").append(fmtTime(System.currentTimeMillis())).append("\n");
            sb.append("THREAD: ").append(t != null ? t.getName() : "?").append("\n");
            sb.append("STAGE: ").append(currentStage).append("\n");
            sb.append("SELECTED_ACC: ").append(safeSelectedAcc()).append("\n");
            sb.append("ACTIVE_ACCS: ").append(safeActiveAccCount()).append("\n");
            sb.append("TRACE:\n").append(Log.getStackTraceString(e)).append("\n");
            sp(appCtx).edit().putString("last_java_crash", sb.toString()).commit();
          } catch (Throwable ignored) {}
        }
        if (prevHandler != null) prevHandler.uncaughtException(t, e);
      });
    } catch (Throwable ignored) {}
  }

  public static void stage(final String st) {
    currentStage = st;
    try {
      Context c = appCtx != null ? appCtx : ApplicationLoader.applicationContext;
      if (c != null) {
        sp(c).edit().putString("stage", st).putLong("stage_ts", System.currentTimeMillis()).apply();
      }
    } catch (Throwable ignored) {}
    if ("dialogs_onResume".equals(st)) {
      dumpIfNeeded(appCtx != null ? appCtx : ApplicationLoader.applicationContext);
    }
  }

  public static void dumpIfNeeded(final Context ctx) {
    if (dumpedThisRun) return;
    dumpedThisRun = true;
    final Context c = ctx != null ? ctx.getApplicationContext() : (appCtx != null ? appCtx : ApplicationLoader.applicationContext);
    if (c == null) return;
    new Thread(() -> {
      try {
        Thread.sleep(1800L);
        SharedPreferences p = sp(c);
        String javaCrash = p.getString("last_java_crash", "");
        String incStage = p.getString("prev_incomplete_stage", "");
        long incTs = p.getLong("prev_incomplete_ts", 0L);

        boolean hasAbnormalExit = !javaCrash.isEmpty();
        StringBuilder exitSb = new StringBuilder();

        if (Build.VERSION.SDK_INT >= 30) {
          try {
            ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
              List<ApplicationExitInfo> list = am.getHistoricalProcessExitReasons(c.getPackageName(), 0, 15);
              if (list != null && !list.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                  ApplicationExitInfo info = list.get(i);
                  if (info == null) continue;
                  int r = info.getReason();
                  if (r != ApplicationExitInfo.REASON_EXIT_SELF && r != ApplicationExitInfo.REASON_USER_REQUESTED) {
                    hasAbnormalExit = true;
                  }
                  exitSb.append("#").append(i + 1)
                    .append(" [").append(fmtTime(info.getTimestamp())).append("] ")
                    .append(reasonName(r))
                    .append(" status=").append(info.getStatus())
                    .append(" imp=").append(info.getImportance())
                    .append(" pss=").append(info.getPss() / 1024L).append("MB")
                    .append(" rss=").append(info.getRss() / 1024L).append("MB\n");
                  if (info.getDescription() != null && !info.getDescription().isEmpty()) {
                    exitSb.append("  desc: ").append(info.getDescription()).append("\n");
                  }
                  String trace = readShortStream(info.getTraceInputStream(), 6000);
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

        if (!hasAbnormalExit && incStage.isEmpty() && p.getBoolean("initial_diag_saved", false)) {
          return;
        }

        StringBuilder report = new StringBuilder();
        report.append("=== DrgGram Diagnostic Log ===\n");
        report.append("Report Time: ").append(fmtTime(System.currentTimeMillis())).append("\n");
        report.append("Device: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append(" (").append(Build.DEVICE).append(")\n");
        report.append("Android: ").append(Build.VERSION.RELEASE).append(" (SDK ").append(Build.VERSION.SDK_INT).append(")\n");
        report.append("Display: ").append(Build.DISPLAY).append("\n");
        report.append("App Version: ").append(appVersion(c)).append("\n");
        report.append("Accounts: ").append(safeActiveAccCount()).append(" active, selected=").append(safeSelectedAcc()).append("\n");
        report.append("RAM: ").append(memSummary(c)).append("\n");
        if (!incStage.isEmpty()) {
          report.append("Prev incomplete stage: ").append(incStage).append(" at ").append(fmtTime(incTs)).append("\n");
        }
        report.append("\n--- LAST JAVA CRASH ---\n");
        report.append(javaCrash.isEmpty() ? "(none)\n" : javaCrash).append("\n");
        report.append("--- SYSTEM PROCESS EXIT REASONS (newest first) ---\n");
        report.append(exitSb.length() == 0 ? "(none or SDK < 30)\n" : exitSb.toString());

        String text = report.toString();
        boolean saved = writeToDownloads(c, "DrgGram_crash.txt", text);
        p.edit().putBoolean("initial_diag_saved", true).remove("last_java_crash").remove("prev_incomplete_stage").apply();

        if (hasAbnormalExit) {
          DrgPluginManager.log(Math.max(0, safeSelectedAcc()), text.length() > 8000 ? text.substring(0, 8000) : text);
          if (saved) {
            DrgPluginManager.toast(DrgPluginManager.tr(
              "Звіт вильоту збережено в Download/DrgGram_crash.txt",
              "Отчёт вылета сохранён в Download/DrgGram_crash.txt",
              "Crash log saved to Download/DrgGram_crash.txt"
            ));
          }
        }
      } catch (Throwable ignored) {}
    }).start();
  }

  private static boolean writeToDownloads(Context ctx, String fileName, String content) {
    byte[] data;
    try {
      data = content.getBytes("UTF-8");
    } catch (Throwable t) {
      data = content.getBytes();
    }
    try {
      if (Build.VERSION.SDK_INT >= 29) {
        ContentResolver cr = ctx.getContentResolver();
        Uri existing = null;
        try (Cursor cur = cr.query(MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            new String[]{MediaStore.MediaColumns._ID},
            MediaStore.MediaColumns.DISPLAY_NAME + "=?",
            new String[]{fileName}, null)) {
          if (cur != null && cur.moveToFirst()) {
            long id = cur.getLong(0);
            existing = Uri.withAppendedPath(MediaStore.Downloads.EXTERNAL_CONTENT_URI, String.valueOf(id));
          }
        } catch (Throwable ignored) {}

        if (existing != null) {
          try (OutputStream os = cr.openOutputStream(existing, "wt")) {
            if (os != null) {
              os.write(data);
              os.flush();
              return true;
            }
          } catch (Throwable ignored) {}
        }

        ContentValues cv = new ContentValues();
        cv.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        cv.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
        cv.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        Uri uri = cr.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
        if (uri == null) {
          String alt = "DrgGram_crash_" + new SimpleDateFormat("HHmmss", Locale.US).format(new Date()) + ".txt";
          cv.put(MediaStore.MediaColumns.DISPLAY_NAME, alt);
          uri = cr.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
        }
        if (uri != null) {
          try (OutputStream os = cr.openOutputStream(uri)) {
            if (os != null) {
              os.write(data);
              os.flush();
              return true;
            }
          }
        }
      } else {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (dir != null) {
          if (!dir.exists()) dir.mkdirs();
          File f = new File(dir, fileName);
          try (FileOutputStream fos = new FileOutputStream(f, false)) {
            fos.write(data);
            fos.flush();
            return true;
          }
        }
      }
    } catch (Throwable ignored) {}
    return false;
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

  private static int safeSelectedAcc() {
    try { return UserConfig.selectedAccount; } catch (Throwable t) { return -1; }
  }

  private static int safeActiveAccCount() {
    int c = 0;
    for (int i = 0; i < 100; i++) {
      try { if (UserConfig.getInstance(i).isClientActivated()) c++; } catch (Throwable ignored) {}
    }
    return c;
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
