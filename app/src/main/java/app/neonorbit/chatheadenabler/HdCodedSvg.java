package app.neonorbit.chatheadenabler;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class HdCodedSvg extends Drawable {
  private final int size;
  private ColorFilter filter;

  public HdCodedSvg(int size, int color) {
    this.size = size;
    setBounds(0, 0, size, size);
    invalidateSelf();
    filter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN);
  }

  public static Drawable create(int size, int color) {
    return new HdCodedSvg(size, color);
  }

  @Override
  public void draw(Canvas canvas) {
    Rect b = getBounds();
    draw(canvas, b.width(), b.height(), b.left, b.top);
  }

  @Override
  public void setAlpha(int i) {}

  @Override
  public void setColorFilter(ColorFilter c) {
    filter = c;
    invalidateSelf();
  }

  @Override
  public int getOpacity() {
    return PixelFormat.OPAQUE;
  }

  @Override
  public int getIntrinsicHeight() {
    return size;
  }

  @Override
  public int getIntrinsicWidth() {
    return size;
  }

  private void draw(Canvas canvas, int w, int h, int dx, int dy) {
    final Path path = new Path();
    final Paint fill = new Paint();
    final Paint stroke = new Paint();
    final Matrix matrix = new Matrix();

    final float ow = 24f;
    final float oh = 24f;
    final float od = Math.min(w / ow, h / oh);

    matrix.reset();
    matrix.setScale(od, od);
    reset(fill, stroke, od);
    reset(fill, stroke, od, 1, 3, 0, 2);

    canvas.save();
    canvas.translate((w - od * ow) / 2f + dx, (h - od * oh) / 2f + dy);
    canvas.save();
    canvas.scale(1.0f, 1.0f);
    canvas.translate(0.0f, od);
    canvas.save();
    canvas.translate(-1212.0f * od, -97.0f * od);

    path.reset();
    path.moveTo(1231.0F, 107.0F);
    path.rLineTo(-14.0F, 0);
    path.cubicTo(1215.6744261325434F, 107.0F, 1214.4017883647095F, 106.47285617657492F, 1213.4644660940673F, 105.53553390593274F);
    path.cubicTo(1212.5271438234251F, 104.59821163529055F, 1212.0F, 103.32557386745651F, 1212.0F, 102.0F);
    path.cubicTo(1212.0F, 100.67442613254349F, 1212.5271438234251F, 99.40178836470945F, 1213.4644660940673F, 98.46446609406726F);
    path.cubicTo(1214.4017883647095F, 97.52714382342508F, 1215.6744261325434F, 97.0F, 1217.0F, 97.0F);
    path.rLineTo(14.0F, 0);
    path.cubicTo(1232.3255738674566F, 97.0F, 1233.5982116352905F, 97.52714382342508F, 1234.5355339059327F, 98.46446609406726F);
    path.cubicTo(1235.4728561765749F, 99.40178836470945F, 1236.0F, 100.67442613254349F, 1236.0F, 102.0F);
    path.cubicTo(1236.0F, 103.32557386745651F, 1235.4728561765749F, 104.59821163529055F, 1234.5355339059327F, 105.53553390593274F);
    path.cubicTo(1233.5982116352905F, 106.47285617657492F, 1232.3255738674566F, 107.0F, 1231.0F, 107.0F);
    path.close();
    path.moveTo(1231.0F, 107.0F);
    path.rMoveTo(0, -8.0F);
    path.rLineTo(-14.0F, 0);
    path.cubicTo(1216.204655679526F, 99.0F, 1215.4410730188258F, 99.31628629405505F, 1214.8786796564405F, 99.87867965644035F);
    path.cubicTo(1214.316286294055F, 100.44107301882566F, 1214.0F, 101.2046556795261F, 1214.0F, 102.0F);
    path.cubicTo(1214.0F, 102.7953443204739F, 1214.316286294055F, 103.55892698117434F, 1214.8786796564405F, 104.12132034355965F);
    path.cubicTo(1215.4410730188258F, 104.68371370594495F, 1216.204655679526F, 105.0F, 1217.0F, 105.0F);
    path.rLineTo(14.0F, 0);
    path.cubicTo(1231.795344320474F, 105.0F, 1232.5589269811742F, 104.68371370594495F, 1233.1213203435595F, 104.12132034355965F);
    path.cubicTo(1233.683713705945F, 103.55892698117434F, 1234.0F, 102.7953443204739F, 1234.0F, 102.0F);
    path.cubicTo(1234.0F, 101.2046556795261F, 1233.683713705945F, 100.44107301882566F, 1233.1213203435595F, 99.87867965644035F);
    path.cubicTo(1232.5589269811742F, 99.31628629405505F, 1231.795344320474F, 99.0F, 1231.0F, 99.0F);
    path.close();
    path.moveTo(1231.0F, 99.0F);
    path.rMoveTo(0, 4F);
    path.rLineTo(-1.0F, 0);
    path.cubicTo(1229.7348852265086F, 103.0F, 1229.480357672942F, 102.89457123531498F, 1229.2928932188136F, 102.70710678118655F);
    path.cubicTo(1229.105428764685F, 102.51964232705812F, 1229.0F, 102.2651147734913F, 1229.0F, 102.0F);
    path.cubicTo(1229.0F, 101.7348852265087F, 1229.105428764685F, 101.48035767294188F, 1229.2928932188136F, 101.29289321881345F);
    path.cubicTo(1229.480357672942F, 101.10542876468502F, 1229.7348852265086F, 101.0F, 1230.0F, 101.0F);
    path.rLineTo(1.0F, 0);
    path.cubicTo(1231.2651147734914F, 101.0F, 1231.519642327058F, 101.10542876468502F, 1231.7071067811864F, 101.29289321881345F);
    path.cubicTo(1231.894571235315F, 101.48035767294188F, 1232.0F, 101.7348852265087F, 1232.0F, 102.0F);
    path.cubicTo(1232.0F, 102.2651147734913F, 1231.894571235315F, 102.51964232705812F, 1231.7071067811864F, 102.70710678118655F);
    path.cubicTo(1231.519642327058F, 102.89457123531498F, 1231.2651147734914F, 103.0F, 1231.0F, 103.0F);
    path.close();
    path.moveTo(1231.0F, 103.0F);
    path.rMoveTo(-14.0F, 6.0F);
    path.rLineTo(14.0F, 0);
    path.cubicTo(1232.3255738674566F, 109.0F, 1233.5982116352905F, 109.52714382342508F, 1234.5355339059327F, 110.46446609406726F);
    path.cubicTo(1235.4728561765749F, 111.40178836470945F, 1236.0F, 112.67442613254349F, 1236.0F, 114.0F);
    path.cubicTo(1236.0F, 115.32557386745651F, 1235.4728561765749F, 116.59821163529055F, 1234.5355339059327F, 117.53553390593274F);
    path.cubicTo(1233.5982116352905F, 118.47285617657492F, 1232.3255738674566F, 119.0F, 1231.0F, 119.0F);
    path.rLineTo(-14.0F, 0);
    path.cubicTo(1215.6744261325434F, 119.0F, 1214.4017883647095F, 118.47285617657492F, 1213.4644660940673F, 117.53553390593274F);
    path.cubicTo(1212.5271438234251F, 116.59821163529055F, 1212.0F, 115.32557386745651F, 1212.0F, 114.0F);
    path.cubicTo(1212.0F, 112.67442613254349F, 1212.5271438234251F, 111.40178836470945F, 1213.4644660940673F, 110.46446609406726F);
    path.cubicTo(1214.4017883647095F, 109.52714382342508F, 1215.6744261325434F, 109.0F, 1217.0F, 109.0F);
    path.close();
    path.moveTo(1217.0F, 109.0F);
    path.rMoveTo(0.0F, 8.0F);
    path.rLineTo(14.0F, 0);
    path.cubicTo(1231.795344320474F, 117.0F, 1232.5589269811742F, 116.68371370594495F, 1233.1213203435595F, 116.12132034355965F);
    path.cubicTo(1233.683713705945F, 115.55892698117434F, 1234.0F, 114.7953443204739F, 1234.0F, 114.0F);
    path.cubicTo(1234.0F, 113.2046556795261F, 1233.683713705945F, 112.44107301882566F, 1233.1213203435595F, 111.87867965644035F);
    path.cubicTo(1232.5589269811742F, 111.31628629405505F, 1231.795344320474F, 111.0F, 1231.0F, 111.0F);
    path.rLineTo(-14.0F, 0);
    path.cubicTo(1216.204655679526F, 111.0F, 1215.4410730188258F, 111.31628629405505F, 1214.8786796564405F, 111.87867965644035F);
    path.cubicTo(1214.316286294055F, 112.44107301882566F, 1214.0F, 113.2046556795261F, 1214.0F, 114.0F);
    path.cubicTo(1214.0F, 114.7953443204739F, 1214.316286294055F, 115.55892698117434F, 1214.8786796564405F, 116.12132034355965F);
    path.cubicTo(1215.4410730188258F, 116.68371370594495F, 1216.204655679526F, 117.0F, 1217.0F, 117.0F);
    path.close();
    path.moveTo(1217.0F, 117.0F);
    path.rMoveTo(0.0F, -4.0F);
    path.rLineTo(1.0F, 0);
    path.cubicTo(1218.2651147734914F, 113.0F, 1218.519642327058F, 113.10542876468502F, 1218.7071067811864F, 113.29289321881345F);
    path.cubicTo(1218.894571235315F, 113.48035767294188F, 1219.0F, 113.7348852265087F, 1219.0F, 114.0F);
    path.cubicTo(1219.0F, 114.2651147734913F, 1218.894571235315F, 114.51964232705812F, 1218.7071067811864F, 114.70710678118655F);
    path.cubicTo(1218.519642327058F, 114.89457123531498F, 1218.2651147734914F, 115.0F, 1218.0F, 115.0F);
    path.rLineTo(-1.0F, 0);
    path.cubicTo(1216.7348852265086F, 115.0F, 1216.480357672942F, 114.89457123531498F, 1216.2928932188136F, 114.70710678118655F);
    path.cubicTo(1216.105428764685F, 114.51964232705812F, 1216.0F, 114.2651147734913F, 1216.0F, 114.0F);
    path.cubicTo(1216.0F, 113.7348852265087F, 1216.105428764685F, 113.48035767294188F, 1216.2928932188136F, 113.29289321881345F);
    path.cubicTo(1216.480357672942F, 113.10542876468502F, 1216.7348852265086F, 113.0F, 1217.0F, 113.0F);
    path.close();
    path.transform(matrix);

    canvas.drawPath(path, fill);
    canvas.drawPath(path, stroke);
    canvas.restore();
    reset(fill, stroke, od, 1, 3, 0, 2);
    canvas.restore();
    reset(fill, stroke, od);
    canvas.restore();
  }

  private void reset(Paint fill, Paint stroke, float od, Integer... o) {
    fill.reset();
    stroke.reset();
    if (filter != null) {
      fill.setColorFilter(filter);
      stroke.setColorFilter(filter);
    }
    fill.setAntiAlias(true);
    stroke.setAntiAlias(true);
    fill.setStyle(Paint.Style.FILL);
    stroke.setStyle(Paint.Style.STROKE);
    for (Integer i : o) {
      switch (i) {
        case 0: stroke.setStrokeJoin(Paint.Join.MITER); break;
        case 1: stroke.setColor(Color.argb(0, 0, 0, 0)); break;
        case 2: stroke.setStrokeMiter(4.0f * od); break;
        case 3: stroke.setStrokeCap(Paint.Cap.BUTT); break;
      }
    }
  }
}
