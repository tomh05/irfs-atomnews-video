package irfs.videonews1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by tomh on 23/03/15.
 */
public class TimelineElement extends RelativeLayout {

    float mPercent = 42;
    Path bubblePath = new Path();
    boolean mActive = true;
    Paint paint = new Paint();
    float width = 0;
    float interp = 0;
    TextView textView;
    float textWidth = 0f;

    int backgroundColor = Color.rgb(200,100,100);


    public TimelineElement (final Context context, int position) {
        super(context);

        setWillNotDraw(false);

        textView = new TextView(context);
        textView.setText("");
        textView.setVisibility(View.VISIBLE);
        textView.setBackgroundColor(Color.TRANSPARENT);
        //textView.setHeight(120);
        textView.setPadding(20,20,20,40); //extra space at bottom for arrow.
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(18);
        textView.setSingleLine();


        //button.setTag(position);

        this.addView(textView);


        Log.d("timelinebutton", "creating path");

    }

    // This is a messy workaround - we don't know what the final size of the textview should be...
    public void setInterp(float _interp) {
        //this.width = width;
        //setMeasuredDimension((int) ((float) getMeasuredWidth()* width),getMeasuredHeight());
        //width = getWidth() * _width;
        // capture maximum width from autosize
        //if (textView.getWidth() > fullTextWidth) fullTextWidth = textView.getWidth();
        //Log.d("timeel","measured width"+fullTextWidth);
        //textView.setWidth((int) (fullTextWidth*_width) );

        float widthInterp = _interp/0.3f;
        if (widthInterp>1f) widthInterp = 1f;
        int newWidth = (int) (textWidth * (widthInterp));
        textView.setWidth(newWidth);

        // morph color
        // 0    200 0
        // 200, 100 100
        float colorInterp = (_interp - 0.9f) / 0.1f;
        if (colorInterp<0) colorInterp=0;
        int r = (int) (200f * colorInterp);
        int g = 200 - (int) (100 * colorInterp);
        int b = (int) (100f * colorInterp);
        backgroundColor = Color.rgb(r,g,b);
        //rebuildPath();
    }

    public TimelineElement (Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d("timelinebutton", "creating path 2");

    }

    public TimelineElement (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.d("timelinebutton", "creating path 3");

    }

    public void setText(String _text) {
        textView.setText(_text);
        Paint paint = new Paint();
        paint.setTextSize(62); // nasty hack. Some multiplier of 18?
        textWidth = paint.measureText(_text);

    }

    public void setPercent(float _percent) {
        mPercent = _percent;
        invalidate();
    }

    public void setActive(boolean _active) {
        mActive = _active;
        rebuildPath();
        invalidate();

    }

    @Override
    protected void onSizeChanged(int w,int h,int oldw,int oldh) {
        super.onSizeChanged(w,h,oldw,oldh);
        width = w;

        rebuildPath();

    }

    /*
    Draw bubble shape with/without arrow, depending on state.
     */
    private void rebuildPath() {
        int w = getWidth();
        int h = getHeight();
        int arrowSize = 20;
        bubblePath = new Path();
        bubblePath.lineTo(w,0);
        bubblePath.lineTo(w,h-arrowSize);
        if (mActive) {
            bubblePath.lineTo(w / 2 + arrowSize, h - arrowSize);
            bubblePath.lineTo(w / 2, h);
            bubblePath.lineTo(w / 2 - arrowSize, h - arrowSize);
        }
        bubblePath.lineTo(0,h-arrowSize);
        bubblePath.close();

    }
    @Override
    public void draw(Canvas canvas) {

        canvas.save();
        canvas.clipPath(bubblePath);


        //canvas.drawColor(Color.rgb(200,100,100));
        canvas.drawColor(backgroundColor);

        paint.setStyle(Paint.Style.FILL);
        //paint.setColor(Color.rgb(200, 0, 0));
        paint.setColor(Color.rgb(200,0,0));
        float barwidth = width * (mPercent / 100.0f);
        canvas.drawRect(0,0,barwidth,getHeight(),paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(5);
        canvas.drawPath(bubblePath,paint);


        super.draw(canvas);

        canvas.restore();

    }

    @Override
    protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(),getMeasuredHeight());
    }

}
