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
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by tomh on 23/03/15.
 */
public class TimelineElement extends RelativeLayout {

    float mTextSize = 18;
    float mPercent = 42;
    Path bubblePath = new Path();
    boolean mActive = true;
    Paint paint = new Paint();
    TextView textView;
    float textWidth = 0f;
    int hPadding = 20;

    int backgroundColor = Color.rgb(200,100,100);

    public TimelineElement (final Context context, int position) {
        super(context);
        init(context);

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
        init(context);

    }

    public TimelineElement (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);

    }

    protected void init(Context context) {

        setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        setWillNotDraw(false);

        textView = new TextView(context);
        textView.setText("");
        textView.setVisibility(View.VISIBLE);
        textView.setBackgroundColor(Color.TRANSPARENT);
        //textView.setHeight(120);
        textView.setPadding(hPadding,20,hPadding,40); //extra space at bottom for arrow.
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
    public void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        //getAlpha();
        canvas.save();
        paint.setColor(Color.BLUE);
        canvas.drawRect(0,0,canvas.getWidth(),80,paint);
        //canvas.clipPath(bubblePath, Region.Op.UNION); // doesnt clip anything
        //canvas.clipPath(bubblePath, Region.Op.INTERSECT); // makes top dissapear
        //canvas.clipPath(bubblePath, Region.Op.REVERSE_DIFFERENCE); // draws no background
        //canvas.clipPath(bubblePath, Region.Op.XOR); // draws top and bottom but not middle bit. Once faded, only draws outside
        //canvas.clipPath(bubblePath, Region.Op.REPLACE); // makes top dissapear
        //canvas.clipPath(bubblePath, Region.Op.DIFFERENCE); //draws top and bottom but not middle. Once faded, only draws bottom
        canvas.clipPath(bubblePath);

        //canvas.drawColor(Color.rgb(200,100,100));
        canvas.drawColor(backgroundColor);

        paint.setStyle(Paint.Style.FILL);
        //paint.setColor(Color.rgb(200, 0, 0));
        paint.setColor(Color.rgb(200, 0, 0));
        float barwidth = getWidth() * (mPercent / 100.0f);
        canvas.drawRect(0,0,barwidth,getHeight(),paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(5);
        canvas.drawPath(bubblePath,paint);

        canvas.restore();

    }

    @Override
    protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(),getMeasuredHeight());
        //setMeasuredDimension(300,200);
    }

}
