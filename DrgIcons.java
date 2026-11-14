package org.telegram.messenger;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.view.View;

public class DrgIcons {
  public static final int GHOST = 1;
  public static final int FORWARD_CHAT = 2;
  public static final int RADAR = 3;
  public static final int ANTI_DELETE = 4;
  public static final int UNLIM_SELECT = 5;
  public static final int VOICE_CHANGER = 6;
  public static final int VOICE_TIMING = 7;
  public static final int MUTE_PM = 8;
  public static final int DELETE_OWN = 9;
  public static final int LOG = 10;

  public static final int GHOST_MSG = 11;
  public static final int GHOST_STORIES = 12;
  public static final int GHOST_ONLINE = 13;
  public static final int GHOST_TYPING = 14;
  public static final int GHOST_AUTO_OFFLINE = 15;
  public static final int GHOST_INTERACT = 16;

  public static final int CHEVRON_RIGHT = 17;
  public static final int CHECK = 18;

  public static View makeSwitchView(Context ctx, boolean initialOn) {
    final View v = new View(ctx);
    v.setTag(Boolean.valueOf(initialOn));
    v.setBackground(new Drawable() {
      private final Paint fp = new Paint(Paint.ANTI_ALIAS_FLAG);
      private final Paint sp = new Paint(Paint.ANTI_ALIAS_FLAG);
      private final RectF rf = new RectF();

      @Override
      public void draw(Canvas c) {
        Rect b = getBounds();
        float w = b.width(), h = b.height();
        if (w <= 0 || h <= 0) return;
        boolean on = Boolean.TRUE.equals(v.getTag());
        float pad = AndroidUtilities.dp(1.5f);
        rf.set(pad, pad, w - pad, h - pad);
        float rad = rf.height() / 2f;

        fp.setStyle(Paint.Style.FILL);
        fp.setColor(on ? 0xFF2ECC71 : 0xFFE74C3C);
        c.drawRoundRect(rf, rad, rad, fp);

        sp.setStyle(Paint.Style.STROKE);
        sp.setStrokeWidth(Math.max(1f, AndroidUtilities.dp(1)));
        sp.setColor(on ? 0xFF58D68D : 0xFFFF7675);
        c.drawRoundRect(rf, rad, rad, sp);

        float kr = rad - AndroidUtilities.dp(2.2f);
        float kx = on ? (rf.right - rad) : (rf.left + rad);
        float ky = rf.centerY();
        fp.setColor(0xFFFFFFFF);
        c.drawCircle(kx, ky, kr, fp);
      }

      @Override public void setAlpha(int alpha) {}
      @Override public void setColorFilter(ColorFilter colorFilter) {}
      @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    });
    return v;
  }

  public static void setSwitchState(View switchView, boolean on) {
    if (switchView != null) {
      switchView.setTag(Boolean.valueOf(on));
      switchView.invalidate();
    }
  }

  public static Drawable makeTransparentIcon() {
    return new Drawable() {
      @Override public void draw(Canvas canvas) {}
      @Override public void setAlpha(int alpha) {}
      @Override public void setColorFilter(ColorFilter colorFilter) {}
      @Override public int getOpacity() { return PixelFormat.TRANSPARENT; }
      @Override public int getIntrinsicWidth() { return AndroidUtilities.dp(24); }
      @Override public int getIntrinsicHeight() { return AndroidUtilities.dp(24); }
    };
  }

  public static Drawable makeSvg(final int type, final int color) {
    return new Drawable() {
      private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
      private final Paint fillP = new Paint(Paint.ANTI_ALIAS_FLAG);
      {
        p.setColor(color);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(AndroidUtilities.dpf2(1.75f));
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeJoin(Paint.Join.ROUND);

        fillP.setColor(color);
        fillP.setStyle(Paint.Style.FILL);
      }

      @Override
      public void draw(Canvas c) {
        Rect b = getBounds();
        float cx = b.exactCenterX();
        float cy = b.exactCenterY();
        float r = Math.min(b.width(), b.height()) * 0.37f;
        Path path = new Path();

        switch (type) {
          case GHOST: {
            // Ghost silhouette with wavy bottom and two eyes
            path.moveTo(cx - r * 0.68f, cy + r * 0.82f);
            path.lineTo(cx - r * 0.68f, cy - r * 0.14f);
            path.cubicTo(cx - r * 0.68f, cy - r * 0.96f, cx + r * 0.68f, cy - r * 0.96f, cx + r * 0.68f, cy - r * 0.14f);
            path.lineTo(cx + r * 0.68f, cy + r * 0.82f);
            path.lineTo(cx + r * 0.34f, cy + r * 0.56f);
            path.lineTo(cx, cy + r * 0.84f);
            path.lineTo(cx - r * 0.34f, cy + r * 0.56f);
            path.close();
            c.drawPath(path, p);
            c.drawCircle(cx - r * 0.24f, cy - r * 0.14f, r * 0.11f, fillP);
            c.drawCircle(cx + r * 0.24f, cy - r * 0.14f, r * 0.11f, fillP);
            break;
          }
          case FORWARD_CHAT: {
            // Double forward arrow
            path.moveTo(cx - r * 0.85f, cy + r * 0.55f);
            path.cubicTo(cx - r * 0.85f, cy - r * 0.05f, cx - r * 0.42f, cy - r * 0.28f, cx + r * 0.28f, cy - r * 0.28f);
            path.lineTo(cx + r * 0.02f, cy - r * 0.62f);
            path.moveTo(cx + r * 0.28f, cy - r * 0.28f);
            path.lineTo(cx + r * 0.02f, cy + r * 0.06f);
            // Second chevron ahead
            path.moveTo(cx + r * 0.48f, cy - r * 0.62f);
            path.lineTo(cx + r * 0.84f, cy - r * 0.28f);
            path.lineTo(cx + r * 0.48f, cy + r * 0.06f);
            c.drawPath(path, p);
            break;
          }
          case RADAR: {
            // Concentric radar rings + sweep line + ping dot
            c.drawCircle(cx, cy, r * 0.86f, p);
            c.drawCircle(cx, cy, r * 0.44f, p);
            c.drawCircle(cx, cy, r * 0.11f, fillP);
            c.drawLine(cx, cy, cx + r * 0.62f, cy - r * 0.62f, p);
            c.drawCircle(cx + r * 0.48f, cy + r * 0.26f, r * 0.12f, fillP);
            break;
          }
          case ANTI_DELETE: {
            // Shield with checkmark/restore protection
            path.moveTo(cx, cy - r * 0.9f);
            path.lineTo(cx + r * 0.74f, cy - r * 0.58f);
            path.lineTo(cx + r * 0.74f, cy + r * 0.02f);
            path.cubicTo(cx + r * 0.74f, cy + r * 0.52f, cx + r * 0.34f, cy + r * 0.8f, cx, cy + r * 0.94f);
            path.cubicTo(cx - r * 0.34f, cy + r * 0.8f, cx - r * 0.74f, cy + r * 0.52f, cx - r * 0.74f, cy + r * 0.02f);
            path.lineTo(cx - r * 0.74f, cy - r * 0.58f);
            path.close();
            c.drawPath(path, p);
            Path chk = new Path();
            chk.moveTo(cx - r * 0.32f, cy + r * 0.02f);
            chk.lineTo(cx - r * 0.06f, cy + r * 0.28f);
            chk.lineTo(cx + r * 0.36f, cy - r * 0.22f);
            c.drawPath(chk, p);
            break;
          }
          case UNLIM_SELECT: {
            // Multi-select checkboxes/checkmarks + list lines
            c.drawRoundRect(new RectF(cx - r * 0.84f, cy - r * 0.68f, cx - r * 0.18f, cy - r * 0.02f), r * 0.14f, r * 0.14f, p);
            path.moveTo(cx - r * 0.68f, cy - r * 0.36f);
            path.lineTo(cx - r * 0.52f, cy - r * 0.2f);
            path.lineTo(cx - r * 0.3f, cy - r * 0.48f);
            c.drawRoundRect(new RectF(cx - r * 0.84f, cy + r * 0.14f, cx - r * 0.18f, cy + r * 0.8f), r * 0.14f, r * 0.14f, p);
            path.moveTo(cx - r * 0.68f, cy + r * 0.46f);
            path.lineTo(cx - r * 0.52f, cy + r * 0.62f);
            path.lineTo(cx - r * 0.3f, cy + r * 0.34f);
            path.moveTo(cx + r * 0.08f, cy - r * 0.35f);
            path.lineTo(cx + r * 0.86f, cy - r * 0.35f);
            path.moveTo(cx + r * 0.08f, cy + r * 0.47f);
            path.lineTo(cx + r * 0.86f, cy + r * 0.47f);
            c.drawPath(path, p);
            break;
          }
          case VOICE_CHANGER: {
            // Mic + sound waves
            c.drawRoundRect(new RectF(cx - r * 0.46f, cy - r * 0.82f, cx + r * 0.02f, cy + r * 0.12f), r * 0.24f, r * 0.24f, p);
            c.drawArc(new RectF(cx - r * 0.7f, cy - r * 0.36f, cx + r * 0.26f, cy + r * 0.42f), 0, 180, false, p);
            c.drawLine(cx - r * 0.22f, cy + r * 0.42f, cx - r * 0.22f, cy + r * 0.84f, p);
            c.drawLine(cx - r * 0.5f, cy + r * 0.84f, cx + r * 0.06f, cy + r * 0.84f, p);
            c.drawArc(new RectF(cx + r * 0.12f, cy - r * 0.48f, cx + r * 0.56f, cy + r * 0.18f), -55, 110, false, p);
            c.drawArc(new RectF(cx + r * 0.24f, cy - r * 0.72f, cx + r * 0.88f, cy + r * 0.42f), -55, 110, false, p);
            break;
          }
          case VOICE_TIMING: {
            // Stopwatch
            c.drawCircle(cx, cy + r * 0.12f, r * 0.74f, p);
            c.drawLine(cx - r * 0.26f, cy - r * 0.86f, cx + r * 0.26f, cy - r * 0.86f, p);
            c.drawLine(cx, cy - r * 0.86f, cx, cy - r * 0.62f, p);
            c.drawLine(cx + r * 0.54f, cy - r * 0.52f, cx + r * 0.72f, cy - r * 0.7f, p);
            c.drawLine(cx, cy + r * 0.12f, cx, cy - r * 0.28f, p);
            c.drawLine(cx, cy + r * 0.12f, cx + r * 0.34f, cy + r * 0.28f, p);
            break;
          }
          case MUTE_PM: {
            // Muted speaker with slash
            path.moveTo(cx - r * 0.78f, cy - r * 0.24f);
            path.lineTo(cx - r * 0.36f, cy - r * 0.24f);
            path.lineTo(cx + r * 0.12f, cy - r * 0.68f);
            path.lineTo(cx + r * 0.12f, cy + r * 0.68f);
            path.lineTo(cx - r * 0.36f, cy + r * 0.24f);
            path.lineTo(cx - r * 0.78f, cy + r * 0.24f);
            path.close();
            c.drawPath(path, p);
            c.drawLine(cx + r * 0.42f, cy - r * 0.28f, cx + r * 0.88f, cy + r * 0.28f, p);
            c.drawLine(cx + r * 0.88f, cy - r * 0.28f, cx + r * 0.42f, cy + r * 0.28f, p);
            break;
          }
          case DELETE_OWN: {
            // Trash bin
            c.drawLine(cx - r * 0.76f, cy - r * 0.48f, cx + r * 0.76f, cy - r * 0.48f, p);
            path.moveTo(cx - r * 0.3f, cy - r * 0.48f);
            path.lineTo(cx - r * 0.22f, cy - r * 0.82f);
            path.lineTo(cx + r * 0.22f, cy - r * 0.82f);
            path.lineTo(cx + r * 0.3f, cy - r * 0.48f);
            path.moveTo(cx - r * 0.56f, cy - r * 0.48f);
            path.lineTo(cx - r * 0.46f, cy + r * 0.78f);
            path.lineTo(cx + r * 0.46f, cy + r * 0.78f);
            path.lineTo(cx + r * 0.56f, cy - r * 0.48f);
            c.drawPath(path, p);
            c.drawLine(cx - r * 0.18f, cy - r * 0.14f, cx - r * 0.14f, cy + r * 0.46f, p);
            c.drawLine(cx + r * 0.18f, cy - r * 0.14f, cx + r * 0.14f, cy + r * 0.46f, p);
            break;
          }
          case LOG: {
            // Document / log sheet with lines
            c.drawRoundRect(new RectF(cx - r * 0.66f, cy - r * 0.84f, cx + r * 0.66f, cy + r * 0.84f), r * 0.18f, r * 0.18f, p);
            c.drawLine(cx - r * 0.36f, cy - r * 0.36f, cx + r * 0.36f, cy - r * 0.36f, p);
            c.drawLine(cx - r * 0.36f, cy, cx + r * 0.36f, cy, p);
            c.drawLine(cx - r * 0.36f, cy + r * 0.36f, cx + r * 0.16f, cy + r * 0.36f, p);
            break;
          }
          case GHOST_MSG: {
            // Double checkmarks with slash
            path.moveTo(cx - r * 0.86f, cy + r * 0.08f);
            path.lineTo(cx - r * 0.52f, cy + r * 0.42f);
            path.lineTo(cx + r * 0.12f, cy - r * 0.34f);
            path.moveTo(cx - r * 0.24f, cy + r * 0.42f);
            path.lineTo(cx + r * 0.46f, cy - r * 0.34f);
            c.drawPath(path, p);
            c.drawLine(cx - r * 0.78f, cy - r * 0.72f, cx + r * 0.78f, cy + r * 0.72f, p);
            break;
          }
          case GHOST_STORIES: {
            // Stories ring with slash
            c.drawArc(new RectF(cx - r * 0.78f, cy - r * 0.78f, cx + r * 0.78f, cy + r * 0.78f), -65, 220, false, p);
            c.drawArc(new RectF(cx - r * 0.78f, cy - r * 0.78f, cx + r * 0.78f, cy + r * 0.78f), 175, 100, false, p);
            c.drawCircle(cx, cy, r * 0.36f, p);
            c.drawLine(cx - r * 0.78f, cy - r * 0.72f, cx + r * 0.78f, cy + r * 0.72f, p);
            break;
          }
          case GHOST_ONLINE: {
            // Signal waves + dot with slash
            c.drawCircle(cx, cy + r * 0.46f, r * 0.13f, fillP);
            c.drawArc(new RectF(cx - r * 0.46f, cy, cx + r * 0.46f, cy + r * 0.92f), -135, 90, false, p);
            c.drawArc(new RectF(cx - r * 0.84f, cy - r * 0.38f, cx + r * 0.84f, cy + r * 1.3f), -135, 90, false, p);
            c.drawLine(cx - r * 0.78f, cy - r * 0.72f, cx + r * 0.78f, cy + r * 0.72f, p);
            break;
          }
          case GHOST_TYPING: {
            // Typing bubble with dots and slash
            c.drawRoundRect(new RectF(cx - r * 0.8f, cy - r * 0.56f, cx + r * 0.8f, cy + r * 0.44f), r * 0.32f, r * 0.32f, p);
            c.drawCircle(cx - r * 0.36f, cy - r * 0.06f, r * 0.09f, fillP);
            c.drawCircle(cx, cy - r * 0.06f, r * 0.09f, fillP);
            c.drawCircle(cx + r * 0.36f, cy - r * 0.06f, r * 0.09f, fillP);
            c.drawLine(cx - r * 0.78f, cy - r * 0.74f, cx + r * 0.78f, cy + r * 0.74f, p);
            break;
          }
          case GHOST_AUTO_OFFLINE: {
            // Crescent moon (auto sleep / offline)
            path.moveTo(cx + r * 0.22f, cy - r * 0.8f);
            path.cubicTo(cx - r * 0.54f, cy - r * 0.78f, cx - r * 0.88f, cy + r * 0.18f, cx - r * 0.22f, cy + r * 0.68f);
            path.cubicTo(cx + r * 0.18f, cy + r * 0.96f, cx + r * 0.72f, cy + r * 0.76f, cx + r * 0.84f, cy + r * 0.34f);
            path.cubicTo(cx + r * 0.26f, cy + r * 0.48f, cx - r * 0.18f, cy - r * 0.12f, cx + r * 0.22f, cy - r * 0.8f);
            c.drawPath(path, p);
            break;
          }
          case GHOST_INTERACT: {
            // Open eye (read on interaction)
            path.moveTo(cx - r * 0.9f, cy);
            path.quadTo(cx, cy - r * 0.74f, cx + r * 0.9f, cy);
            path.quadTo(cx, cy + r * 0.74f, cx - r * 0.9f, cy);
            c.drawPath(path, p);
            c.drawCircle(cx, cy, r * 0.26f, p);
            c.drawCircle(cx, cy, r * 0.09f, fillP);
            break;
          }
          case CHEVRON_RIGHT: {
            path.moveTo(cx - r * 0.24f, cy - r * 0.52f);
            path.lineTo(cx + r * 0.28f, cy);
            path.lineTo(cx - r * 0.24f, cy + r * 0.52f);
            c.drawPath(path, p);
            break;
          }
          case CHECK:
          default: {
            path.moveTo(cx - r * 0.6f, cy);
            path.lineTo(cx - r * 0.15f, cy + r * 0.48f);
            path.lineTo(cx + r * 0.65f, cy - r * 0.45f);
            c.drawPath(path, p);
            break;
          }
        }
      }

      @Override public void setAlpha(int a) { p.setAlpha(a); fillP.setAlpha(a); }
      @Override public void setColorFilter(ColorFilter cf) { p.setColorFilter(cf); fillP.setColorFilter(cf); }
      @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
      @Override public int getIntrinsicWidth() { return AndroidUtilities.dp(24); }
      @Override public int getIntrinsicHeight() { return AndroidUtilities.dp(24); }
    };
  }
}
// === КІНЕЦЬ ФАЙЛУ ===
