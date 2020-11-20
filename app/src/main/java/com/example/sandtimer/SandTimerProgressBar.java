package com.example.sandtimer;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class SandTimerProgressBar extends View {
    private int mProgressSeconds = 70;
    public int getProgressSeconds(){
        return mProgressSeconds;
    }
    public void setProgressSeconds(int progressSeconds){
        if(mProgressSeconds == progressSeconds) return;
        mProgressSeconds = progressSeconds;
        invalidateTextPaintAndMeasurements();
    }

    ArgbEvaluator tintColorEvaluator = new ArgbEvaluator();
    Integer[] colors = {Color.GREEN, Color.YELLOW, Color.RED,  Color.MAGENTA, Color.BLUE, Color.CYAN };
    private int getTintColor()
    {
        int remaining = getMaxSeconds() - getProgressSeconds();//120-0=120
        if(remaining > 60 * 60) return Color.BLACK;
        if(remaining < 0 ) return Color.GRAY;

        int possibleMaxRemaining = 60 * 60;
        int colorSpecLength = possibleMaxRemaining / (colors.length - 1);//23
        int i = remaining / colorSpecLength;//119/23
        i = Math.min(i, colors.length - 2);

        float fraction = (float)(remaining % colorSpecLength) / (float)(colorSpecLength);
        return (int)tintColorEvaluator.evaluate(fraction, colors[i], colors[i+1]);
    }

    private int mMaxSeconds = 10 * 60;
    public int getMaxSeconds(){return mMaxSeconds;}
    public void setMaxSeconds(int maxSeconds){
        if(mMaxSeconds == maxSeconds) return;
        mMaxSeconds = maxSeconds;
        invalidateTextPaintAndMeasurements();
    }

    private float mLidHeightRatio = 0.07f;
    /**
     * Gets the LidHeightRatio attribute value.
     *
     * @return The LidHeightRatio attribute value.
     */
    public float getLidHeightRatio() {
        return mLidHeightRatio;
    }

    /**
     * Sets the view"s LidHeightRatio attribute value. It should be between 0 and 1
     *
     * @param lidHeightRatio The example string attribute value to use.
     */
    public void setLidHeightRatio(float lidHeightRatio) {
        mLidHeightRatio = lidHeightRatio;
        invalidateTextPaintAndMeasurements();
    }

    float mMiddleWidthRatio = 0.04f;
    public float getMiddleWidth(){ return mMiddleWidthRatio; }
    public void setMiddleWidth (float middleWidth){
        mMiddleWidthRatio = middleWidth;
        invalidateTextPaintAndMeasurements();
    }

    float mPaddingGlassRatio = 0.05f;
    public float getPaddingGlassRatio () { return mPaddingGlassRatio; }
    public void setPaddingGlassRatio(float paddingGlassRatio){
        mPaddingGlassRatio = paddingGlassRatio;
        invalidateTextPaintAndMeasurements();
    }

    private Paint mPaintClearInside;
    private Paint mPaintClearOutside;
    private Paint mPaintEdge;
    private Paint mPaintBitmap;
    private Paint mPaintLid;
    Paint paintTint = new Paint();

    private Bitmap bitmapSand;
    private Bitmap bitmapBottom;
    private Bitmap bitmapTop;
    private Rect rectBitmapTopSource;
    private Rect rectBitmapBottomSource;

    private Path pathEdge;

    private Rect rectTopLid;
    private Rect rectBottomLid;
    private Rect rectTopGlass;
    private Rect rectBottomGlass;
    private Rect rectWhole;
    private Rect rectProgressTop;
    private Rect rectProgressBottom;

    BitmapShader sandShader;

    public SandTimerProgressBar(Context context) {
        super(context);
        init(null, 0);
    }
    public SandTimerProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }
    public SandTimerProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.SandTimerProgressBar, defStyle, 0);

        int minPadding=15;
        paddingLeft = minPadding+getPaddingLeft();
        paddingTop = minPadding+getPaddingTop();
        paddingRight = minPadding+getPaddingRight();
        paddingBottom = minPadding+getPaddingBottom();

        mPaintClearInside = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintClearInside.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mPaintClearOutside = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintClearOutside.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        mPaintEdge = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaintEdge.setStrokeWidth(7f);
        mPaintEdge.setStyle(Paint.Style.STROKE);

        mPaintBitmap = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
//        mPaintBitmap.setColorFilter(colorFilterPD);

        mPaintLid = new Paint();
        mPaintLid.setPathEffect(new CornerPathEffect(10));

        bitmapSand = BitmapFactory.decodeResource(getResources(), R.drawable.sand_800);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();

        try {
            setProgressSeconds(a.getInt(R.styleable.SandTimerProgressBar_ProgressSeconds, getProgressSeconds()));
        }
        finally { }
        try {
            setMaxSeconds(a.getInt(R.styleable.SandTimerProgressBar_MaxSeconds, getMaxSeconds()));
        }finally {

        }
        a.recycle();

//        sandShader = new BitmapShader(patternBMP, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
//        drawPaint.setColor(0xFFFFFFFF);
//        drawPaint.setShader(sandShader);

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    private void invalidateTextPaintAndMeasurements() {
        int w = getWidth();
        int h = getHeight();

        if(w <= 0 || h <= 0) return;


        rectWhole = new Rect(0 ,0, w, h);

        contentWidth = w - paddingLeft - paddingRight;
        contentHeight = h - paddingTop - paddingBottom;

        int mLidHeight = (int) (contentHeight * mLidHeightRatio);
        paddingGlass = (int)(contentWidth * mPaddingGlassRatio);
        mMiddleWidth = (int)(contentWidth * mMiddleWidthRatio);
        glassWidth = contentWidth - 2 * paddingGlass;

        int colorLidLight = getResources().getColor(R.color.sand_timer_lid_light);
        int colorLidDark = getResources().getColor(R.color.sand_timer_lid_dark);
        mPaintLid.setShader(
                new LinearGradient(paddingLeft, 0,w-paddingRight,0,
                        colorLidDark, colorLidLight, Shader.TileMode.CLAMP));

        int radius = h / 100;
        mPaintEdge.setShadowLayer(radius, 0, 0, Color.BLACK);
        mPaintEdge.setStrokeWidth(radius/2);
        mPaintLid.setShadowLayer(radius, 0, 0, Color.BLACK);

        int left = paddingLeft + paddingGlass;
        int right = w - paddingRight - paddingGlass;
        int middleY = paddingTop + (contentHeight / 2);
        rectTopGlass = new Rect(left, paddingTop + mLidHeight, right, middleY);
        rectBottomGlass = new Rect(left, middleY, right, h - paddingBottom - mLidHeight);

        int edgeStrokeWidth = (int)( mPaintEdge.getStrokeWidth() / 2);
        rectTopLid = new Rect(paddingLeft, paddingTop, w - paddingRight, paddingTop + mLidHeight + edgeStrokeWidth);
        rectBottomLid = new Rect(paddingLeft, h - paddingBottom - mLidHeight - edgeStrokeWidth, w - paddingRight, h - paddingBottom);

        float sandFillRatio = 1f;
        rectBitmapTopSource = new Rect(0, 0, rectTopGlass.width(), (int)(sandFillRatio * rectTopGlass.height()));
        rectBitmapBottomSource = new Rect(0, 0, rectBottomGlass.width(), (int)(sandFillRatio * rectBottomGlass.height()));

        createPaths();

        bitmapBottom = Bitmap.createBitmap(rectBitmapBottomSource.width(), rectBitmapBottomSource.height(),  bitmapSand.getConfig());
        bitmapTop = Bitmap.createBitmap(rectBitmapTopSource.width(), rectBitmapTopSource.height(), bitmapSand.getConfig());

        int tintColor = getTintColor();
        paintTint.setColorFilter(new PorterDuffColorFilter(tintColor, PorterDuff.Mode.MULTIPLY));
        Canvas c = new Canvas(bitmapTop);
        c.drawBitmap(bitmapSand, null, rectBitmapTopSource, mPaintBitmap);
        c.drawBitmap(bitmapSand, null, rectBitmapTopSource, paintTint);
        c = new Canvas(bitmapBottom);
        c.drawBitmap(bitmapSand, null, rectBitmapBottomSource, mPaintBitmap);
        c.drawBitmap(bitmapSand, null, rectBitmapBottomSource, paintTint);
        c.drawPath(pathSand1, mPaintClearInside);
        c.drawPath(pathSand2, mPaintClearInside);

        int progressHeight;
        try{ progressHeight = (int)( rectTopGlass.height() * getProgressSeconds() / getMaxSeconds()); }
        catch (Exception e) { progressHeight = 0; }

        rectProgressTop = new Rect(rectTopGlass.left, rectTopGlass.top, rectTopGlass.right,
                rectTopGlass.top + progressHeight);

        int offset = (int)(0.8 * rectBottomGlass.height() * getProgressSeconds() / getMaxSeconds());
        int top = rectBottomGlass.bottom - offset;
        rectProgressBottom = new Rect(rectBottomGlass.left, top,
                rectBottomGlass.right, top + bitmapBottom.getHeight());

        //mPaintLid.setColorFilter(new PorterDuffColorFilter(tintColor, PorterDuff.Mode.OVERLAY));
        //mPaintEdge.setColor(tintColor);
        invalidate();
    }

    int paddingLeft;
    int paddingTop;
    int paddingRight;
    int paddingBottom;
    int paddingGlass;
    int contentWidth;
    int contentHeight;
    int mMiddleWidth;
    int glassWidth;
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        invalidateTextPaintAndMeasurements();
        }
    Path p1, p2;
    Path pathSand1, pathSand2;
    private void createSandPaths(){
        pathSand1 = new Path();
        pathSand2 = new Path();

        pathSand1.moveTo(rectBitmapBottomSource.width() / 2, 0);
        pathSand2.moveTo(rectBitmapBottomSource.width() / 2, 0);

        int height = (int)(rectBitmapBottomSource.height() * 0.4f);

        pathSand1.lineTo(0, height);
        pathSand1.lineTo(0, 0);
        pathSand1.close();

        pathSand2.lineTo(rectBitmapBottomSource.width(), height);
        pathSand2.lineTo(rectBitmapBottomSource.width(), 0);
        pathSand2.close();
    }
    private void createPaths(){
        createSandPaths();

        pathEdge = new Path();
        p1 = new Path();
        p2 = new Path();

        p1.moveTo(0,0);


        float x = rectTopGlass.left;
        float y = rectTopGlass.top;
        pathEdge.moveTo(x, y);
        p1.lineTo(x, 0);
        p1.lineTo(x, y);
        p2.moveTo(x, y);
        p2.lineTo(x, 0);
        p2.lineTo(getWidth(), 0);



        float preX = x;
        float preY = y;
        x = rectTopGlass.left + (glassWidth - mMiddleWidth) / 2;
        y = rectTopGlass.bottom;
//        pathEdge.lineTo(x, y);
        pathEdge.cubicTo(preX, preY + 300, x, y - 250, x, y);
        p1.cubicTo(preX, preY + 300, x, y - 250, x, y);


        preX = x;
        preY = y;
        x = rectBottomGlass.left;
        y = rectBottomGlass.bottom;
//        pathEdge.lineTo(x, y);
        pathEdge.cubicTo(preX, preY + 250, x, y - 300, x, y);
        p1.cubicTo(preX, preY + 250, x, y - 300, x, y);
        p1.lineTo(getWidth(), y);
        p1.lineTo(getWidth(), getHeight());
        p1.lineTo(0, getHeight());
        p1.close();

        x = rectBottomGlass.right;
        pathEdge.lineTo(x, y);
       p2.lineTo(getWidth(), y);
       p2.lineTo(x, y);

        preX = x;
        preY = y;
        x = rectTopGlass.right - glassWidth/2  + mMiddleWidth / 2;
        y = rectTopGlass.bottom;
//        pathEdge.lineTo(x, y);
        pathEdge.cubicTo(preX, preY - 300, x, y + 250, x, y);
        p2.cubicTo(preX, preY - 300, x, y + 250, x, y);

        preX = x;
        preY = y;
        x = rectTopGlass.right;
        y = rectTopGlass.top;
//        pathEdge.lineTo(x, y);
        pathEdge.cubicTo(preX, preY - 250, x, y + 300, x, y);
        p2.cubicTo(preX, preY - 250, x, y + 300, x, y);

        pathEdge.close();
        p2.close();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.drawRect(rectWhole, mPaintClearInside);

        //canvas.drawColor(Color.WHITE);//, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(bitmapTop, rectBitmapTopSource, rectTopGlass, mPaintBitmap);

        canvas.drawRect(rectProgressTop, mPaintClearInside);
        canvas.drawBitmap(bitmapBottom, rectBitmapBottomSource, rectProgressBottom, mPaintBitmap);

//        mPaintClearOutside.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
//        mPaintClearOutside.setStyle(Paint.Style.FILL);
//        canvas.drawPath(pathEdge, mPaintClearOutside);

        canvas.drawPath(p1, mPaintClearInside);
        canvas.drawPath(p2, mPaintClearInside);

        canvas.drawPath(pathEdge, mPaintEdge);
        canvas.drawRect(rectTopLid, mPaintLid);
        canvas.drawRect(rectBottomLid, mPaintLid);
    }
}