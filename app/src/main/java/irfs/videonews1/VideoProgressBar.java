package irfs.videonews1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by tom on 10/04/15.
 */
public class VideoProgressBar extends View {

    Paint mPaint;
    int foregroundColor = Color.rgb(180,0,1);
    int backgroundColor = Color.rgb(85,85,85);

    public void setPosition(int position) {
        this.position = position;
        invalidate();
    }

    public void setDuration(int duration) {
        this.duration = duration;
        invalidate();
    }

    public void setCaptionPositions(int[] captionPositions) {
        this.captionPositions = captionPositions;
        invalidate();
    }

    private int position, duration;
    private int[] captionPositions = new int[] {};

    public VideoProgressBar(Context context) {
        super(context);
       init();
    }


    public VideoProgressBar (Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public VideoProgressBar (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    protected void init() {
        setWillNotDraw(false);

        mPaint = new Paint();
        mPaint.setColor(Color.rgb(200,0,0));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int defaultWidth = 100;
        int defaultHeight = 10;

        int width, height;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) { // match parent
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) { //either
            width = Math.min(defaultWidth, widthSize);
        } else { width = defaultWidth; }//wrap content

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(defaultHeight, heightSize);
        } else { height = defaultHeight; }


        setMeasuredDimension(width,height);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float barWidth = ((float)position/(float)duration)*getWidth();

        float centre = (getHeight() / 2);

        // back line
        mPaint.setColor(backgroundColor);

        float gap = 4f;
        for (int i = 0; i<captionPositions.length-1;i++) {
            float startX = ((float) captionPositions[i]) / ((float) duration)*getWidth() + gap;
            float endX = ((float) captionPositions[i+1]) / ((float) duration)*getWidth() - gap;

            canvas.drawRect(startX,0,endX,getHeight(), mPaint);
        }

        if (captionPositions.length>0) {
            float finalX = ((float) captionPositions[captionPositions.length - 1]) / ((float) duration) * getWidth() + gap;
            canvas.drawRect(finalX, 0, getWidth(), getHeight(), mPaint);
        }

        // front line
        mPaint.setColor(foregroundColor);
        canvas.drawRect(0, 0, barWidth,getHeight(), mPaint);

        /*
        for (int i = 0; i<captionPositions.length;i++) {
            float x = ((float) captionPositions[i]) / ((float) duration)*getWidth();
            if (position<captionPositions[i]) {
                mPaint.setColor(Color.rgb(200,100,100));
            } else {
                mPaint.setColor(Color.rgb(200,0,0));
            }
            canvas.drawCircle(x,getHeight()/2,getHeight()/2,mPaint);
        }
        */
    }
}
