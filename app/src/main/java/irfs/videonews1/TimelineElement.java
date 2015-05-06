package irfs.videonews1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by tomh on 23/03/15.
 */
public class TimelineElement extends RelativeLayout {

    float mTextSize = 20;
    float mPercent = 0;
    Path bubblePath = new Path();
    boolean mActive = true;
    Paint paint = new Paint();
    TextView textView;
    float textWidth = 0f;
    int hPadding = 20;
    int defaultHeight;

    int foregroundColor = Color.rgb(180,0,1);
    int backgroundColor = Color.rgb(154,154,154);

    public TimelineElement (final Context context, int position) {
        super(context);
        init(context);

    }

    // This is a messy workaround - we don't know what the final size of the textview should be...
    public void setInterp(float _interp) {

        float widthInterp = _interp/0.3f;
        if (widthInterp>1f) widthInterp = 1f;
        int newWidth = (int) (textWidth * (widthInterp));
        int height;
        if (getRootView().getTag()=="large_size") {
           height= (int) (48 * getContext().getResources().getDisplayMetrics().scaledDensity);
        } else {
            height= (int) (42 * getContext().getResources().getDisplayMetrics().scaledDensity);
        }
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(newWidth,height);
        p.rightMargin = 4;
        setLayoutParams(p);

        // morph color
        // 19 170 15
        // 154, 154, 154
        float colorInterp = (_interp - 0.9f) / 0.1f;
        if (colorInterp<0) colorInterp=0;
        int r = 19  + (int) ((float)(154-19) * colorInterp);
        int g = 170 + (int) ((float)(154-170) * colorInterp);
        int b = 15  + (int) ((float)(154-15) * colorInterp);
        backgroundColor = Color.rgb(r,g,b);
        invalidate();
    }

    public TimelineElement (Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    public TimelineElement (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);

    }

    protected void init(Context context) {

        setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        setWillNotDraw(false);


        if (getRootView().getTag()=="large_size") {

           defaultHeight= (int) (48 * getContext().getResources().getDisplayMetrics().scaledDensity);
            hPadding = 20;
            mTextSize = 20;
        } else {

            defaultHeight= (int) (42 * getContext().getResources().getDisplayMetrics().scaledDensity);
            hPadding = 18;
            mTextSize = 18;
        }

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, defaultHeight);
        p.rightMargin = 4;
        setLayoutParams(p);

        textView = new TextView(context);
        textView.setText("");
        textView.setVisibility(View.VISIBLE);
        textView.setBackgroundColor(Color.TRANSPARENT);
        //textView.setHeight(120);
        textView.setPadding(hPadding,hPadding,hPadding,2*hPadding); //extra space at bottom for arrow.
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(mTextSize);
        textView.setSingleLine();

        //button.setTag(position);

        this.addView(textView);
    }

    public void setText(String _text) {
        textView.setText(_text);
        Paint paint = new Paint();
        float scaledDensity = getContext().getResources().getDisplayMetrics().scaledDensity;
        paint.setTextSize(mTextSize*scaledDensity); // nasty hack. Some multiplier of 18?
        textWidth = paint.measureText(_text) + 2*hPadding;
        textView.setWidth((int)textWidth);

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
        //width = w;

        rebuildPath();

    }

    /*
    Draw bubble shape with/without arrow, depending on state.
     */
    private void rebuildPath() {
        int w = getWidth();
        int h = getHeight();
        int arrowSize = hPadding;
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
    public void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        //getAlpha();
        canvas.save();
        //paint.setColor(Color.BLUE);

        canvas.clipPath(bubblePath);

        //canvas.drawColor(Color.rgb(200,100,100));
        canvas.drawColor(backgroundColor);

        paint.setStyle(Paint.Style.FILL);
        //paint.setColor(Color.rgb(200, 0, 0));
        paint.setColor(foregroundColor);
        float barwidth = getWidth() * (mPercent / 100.0f);
        canvas.drawRect(0,0,barwidth,getHeight(),paint);

        //paint.setStyle(Paint.Style.STROKE);
        //paint.setColor(Color.WHITE);
        //paint.setStrokeWidth(5);
        //canvas.drawPath(bubblePath,paint);

        canvas.restore();

    }


    protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(),getMeasuredHeight());

        //setMeasuredDimension(300,200);
        int defaultWidth = (int) textWidth;
        if (getRootView().getTag()=="large_size") {
           defaultHeight= (int) (48 * getContext().getResources().getDisplayMetrics().scaledDensity);
        } else {
            defaultHeight= (int) (42 * getContext().getResources().getDisplayMetrics().scaledDensity);
        }

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

        setMeasuredDimension(width, height);
    }
}

