package com.intro.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

import com.intro.android.R;

/**
 * Created by vinove on 6/7/16.
 */
public class AViewFlipper extends ViewFlipper{

    Paint paint = new Paint();

    public AViewFlipper(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    protected void dispatchDraw(Canvas canvas)
    {
        super.dispatchDraw(canvas);
        int width = getWidth();

        float margin = 5;
        float radius = getHeight()/45;
        float cx = width/2-(14 * 2 *getChildCount()/2);
        float cy = 50; //  getHeight()-15;
//        float cx = width/2–((radius + margin) * 2 * getChildCount()/2);
//        float cy = getHeight()–15;

        canvas.save();

        for (int i = 0; i < getChildCount(); i++)
        {
            if (i == getDisplayedChild())
            {
                paint.setColor(Color.WHITE);
                canvas.drawCircle(cx, cy, radius, paint);

            } else
            {
                paint.setColor(Color.GRAY);
                canvas.drawCircle(cx, cy, radius, paint);
            }
            cx += 2 * (radius + margin);
        }
        canvas.restore();
    }

}
