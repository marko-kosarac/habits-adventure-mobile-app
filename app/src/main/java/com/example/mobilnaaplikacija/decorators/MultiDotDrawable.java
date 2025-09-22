package com.example.mobilnaaplikacija.decorators;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

public class MultiDotDrawable extends Drawable {
    private final List<Integer> colors;
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint plusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int alpha = 255;

    public MultiDotDrawable(List<Integer> colors) {
        this.colors = colors == null ? new ArrayList<>() : new ArrayList<>(colors);
        plusPaint.setColor(Color.WHITE);
        plusPaint.setStyle(Paint.Style.STROKE);
        plusPaint.setStrokeWidth(2f);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect b = getBounds();
        if (b.width() <= 0 || b.height() <= 0) return;

        int totalTasks = colors.size();
        int showCount = Math.min(totalTasks, 4);
        boolean more = totalTasks > 3;

        float width = b.width();
        float height = b.height();

        //radijus relativan u odnosu na velicinu celije
        float radius = Math.min(width / 10f, height / 6f);
        float centerX = b.exactCenterX();
        float centerY = b.exactCenterY();

        //vertikalne pozicije za 2 reda
        float rowGap = radius * 1.8f;
        float yTop = centerY - rowGap/2f;
        float yBottom = centerY + rowGap/2f;

        //horizontalni spejsing za 2 kolone
        float colGap = radius * 2.8f;
        float leftX = centerX - colGap/2f;
        float rightX = centerX + colGap/2f;

        dotPaint.setAlpha(alpha);
        plusPaint.setAlpha(alpha);

        //crta obojen krug
        java.util.function.BiConsumer<Integer, Float[]> drawCircle = (color, coords) -> {
            dotPaint.setColor(color);
            canvas.drawCircle(coords[0], coords[1], radius, dotPaint);
        };

        //zadataka == 1 -> centar
        //2 -> lijevo i desno u jedan red
        //3 -> 2 u gornjem, jedan u donjem centar
        //4 -> 2 gore, 2 dole (posljednji je '+')
        switch (showCount) {
            case 0:
                break;
            case 1: {
                int c = colors.get(0);
                drawCircle.accept(c, new Float[]{centerX, centerY});
                break;
            }
            case 2: {
                drawCircle.accept(colors.get(0), new Float[]{leftX, centerY});
                drawCircle.accept(colors.get(1), new Float[]{rightX, centerY});
                break;
            }
            case 3: {
                drawCircle.accept(colors.get(0), new Float[]{leftX, yTop});
                drawCircle.accept(colors.get(1), new Float[]{rightX, yTop});
                drawCircle.accept(colors.get(2), new Float[]{centerX, yBottom});
                break;
            }
            case 4: {
                drawCircle.accept(colors.get(0), new Float[]{leftX, yTop});
                drawCircle.accept(colors.get(1), new Float[]{rightX, yTop});
                drawCircle.accept(colors.get(2), new Float[]{leftX, yBottom});

                //vise zadataka -> '+' u krugu; else normalan obojen krug
                if (more) {
                    //crta taman krug i bijeli plus
                    dotPaint.setColor(Color.DKGRAY);
                    canvas.drawCircle(rightX, yBottom, radius, dotPaint);

                    //crta plus
                    float plusSize = radius * 0.9f;
                    plusPaint.setStrokeWidth(Math.max(2f, radius / 6f));
                    canvas.drawLine(rightX - plusSize/2f, yBottom, rightX + plusSize/2f, yBottom, plusPaint);
                    canvas.drawLine(rightX, yBottom - plusSize/2f, rightX, yBottom + plusSize/2f, plusPaint);
                } else {
                    drawCircle.accept(colors.get(3), new Float[]{rightX, yBottom});
                }
                break;
            }
        }
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;
        dotPaint.setAlpha(alpha);
        plusPaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        dotPaint.setColorFilter(colorFilter);
        plusPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
