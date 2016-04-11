package com.dundunwen;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import edu.cqut.cn.loading.R;

/**
 * Created by dun on 2016/4/9.
 */
public class LoadingView extends View{

    private static final String TAG = "LoadingView";

    private Context mContext = getContext();
    private int radius = 10;
    private int ringRadius = 5;
    private int interval = radius/2;
    private static final int DEFAULT_WIDTH = 140;
    private static final int DEFAULT_HEIGHT = 140;
    private int fillColor = 0xffe61a5f;
    private int ringColor = 0xffe61a5f;
    private int caicleNum = 5;

    /**当前实心圆圈指向下标*/
    private int currentFillCircleIndex = 0;
    private PointPosition[] positions = null;

    private Paint fillCirclePaint;
    private Paint ringPaint;

    public static final int ANIMATE_START = 1;
    public static final int ANIMATE_STOP = 2;
    private int animateStatus = 2;

    private ValueAnimator mAnimator;

    public LoadingView(Context context) {
        this(context,null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context,attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LoadingView);
        fillColor = ta.getColor(R.styleable.LoadingView_fillColor,fillColor);
        ringColor = ta.getColor(R.styleable.LoadingView_ringColor,ringColor);
        ta.recycle();

        fillCirclePaint = new Paint();
        fillCirclePaint.setColor(fillColor);
        fillCirclePaint.setStyle(Paint.Style.FILL);

        ringPaint = new Paint();
        ringPaint.setStrokeWidth(ringRadius);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setColor(ringColor);

    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width  = measureDimension(Dp2Px(DEFAULT_WIDTH), widthMeasureSpec);
        int height = measureDimension(Dp2Px(DEFAULT_HEIGHT), heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private int measureDimension(int defaultSize,int measureSpec){
        int result = defaultSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(defaultSize, specSize);
        } else {
            result = defaultSize;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(positions == null){
            initPositions();
        }
//        countPositions(45,true);
        drawCircle(canvas);
        setAnimateStatus(ANIMATE_START);
    }



    private void drawCircle(Canvas canvas) {
        for (int i = 0; i < positions.length; i++) {
            PointPosition position = positions[i];
            canvas.save();
            canvas.translate((int)position.X,(int)position.Y);

            if(i == currentFillCircleIndex){
                canvas.drawCircle(0,0,Dp2Px(radius),fillCirclePaint);
            }else{
                canvas.drawCircle(0,0,Dp2Px(radius),ringPaint);
            }
            canvas.restore();
        }
    }

    private void countPositions(double degree,boolean isClockwise) {

        double degreeInPi = Math.toRadians(degree);

        PointPosition circleOnLeft;
        PointPosition circleOnRight;
        if(currentFillCircleIndex == caicleNum-1){
            circleOnLeft = positions[0];
            circleOnRight = positions[currentFillCircleIndex];
        }else{
            circleOnLeft = positions[currentFillCircleIndex];
            circleOnRight = positions[currentFillCircleIndex+1];
        }

        double centerX = (circleOnRight.X + circleOnLeft.X)/2;
        double centerY = (circleOnLeft.Y+circleOnRight.Y)/2;


        double distance = (Dp2Px(radius) + Dp2Px(interval)/2) * (currentFillCircleIndex==caicleNum-1?(caicleNum-1):1);
        if(!isClockwise){
            circleOnLeft.X = centerX + distance * Math.cos(Math.PI - degreeInPi);
            circleOnLeft.Y = centerY + distance * Math.sin(Math.PI - degreeInPi);

            circleOnRight.X = centerX + distance * Math.cos(2*Math.PI-degreeInPi);
            circleOnRight.Y = centerY + distance * Math.sin(2*Math.PI-degreeInPi);
        } else {
            circleOnLeft.X = centerX + distance * Math.cos(Math.PI + degreeInPi);
            circleOnLeft.Y = centerY + distance * Math.sin(Math.PI + degreeInPi);

            circleOnRight.X = centerX + distance * Math.cos(degreeInPi);
            circleOnRight.Y = centerY + distance * Math.sin(degreeInPi);
        }
    }

    private void initPositions() {

        positions = new PointPosition[caicleNum];
        int centerX = getWidth()/2;
        int centerY = getHeight()/2;
        for (int i = 0 ; i < positions.length ; i++) {
            int currX = centerX + (i-caicleNum/2) * (2 * Dp2Px(radius) +Dp2Px(interval));
            positions[i] = new PointPosition(currX,centerY);
        }
    }

    private boolean isClockWise = false;
    private void animationStrat(){
     if(mAnimator==null) {
         mAnimator = ValueAnimator.ofInt(0, 180);
         mAnimator.setRepeatCount(-1);
         mAnimator.setDuration(1000);
         mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
             @Override
             public void onAnimationUpdate(ValueAnimator animation) {
                 int value = (int) animation.getAnimatedValue();
                 countPositions(value,isClockWise);
                 invalidate();
             }
         });

         mAnimator.addListener(new Animator.AnimatorListener() {
             @Override
             public void onAnimationStart(Animator animation) {

             }

             @Override
             public void onAnimationEnd(Animator animation) {
             }

             @Override
             public void onAnimationCancel(Animator animation) {
             }

             @Override
             public void onAnimationRepeat(Animator animation) {
                 isClockWise = !isClockWise;
                 int tempIndex = currentFillCircleIndex;
                 setCurrentFillCircleIndex(currentFillCircleIndex+1);
                 swap(tempIndex,currentFillCircleIndex);
             }
         });
     }

        mAnimator.start();
    }

    public void setCurrentFillCircleIndex(int currentFillCircleIndex) {
        if(currentFillCircleIndex>=caicleNum){
            this.currentFillCircleIndex = 0;
        }else{
            this.currentFillCircleIndex = currentFillCircleIndex;
        }
    }

    public int getAnimateStatus() {
        return animateStatus;
    }

    public void setAnimateStatus(int animateStatus) {
        if(this.animateStatus == animateStatus){
            return;
        }
        this.animateStatus = animateStatus;
        if(animateStatus==ANIMATE_START){
            animationStrat();
        }else{
            animationStop();
        }
    }

    private void animationStop() {
        if(mAnimator!=null){
            mAnimator.cancel();
            mAnimator.removeAllUpdateListeners();
            mAnimator.removeAllListeners();
            mAnimator = null;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.post(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.post(new Runnable() {
            @Override
            public void run() {

                setAnimateStatus(ANIMATE_STOP);
            }
        });

    }

    /**
     * 管理圆心坐标
     * */
    private class PointPosition{
        public double X;
        public double Y;

        public PointPosition(int x, int y) {
            X = x;
            Y = y;
        }
    }
    public int Dp2Px(float dp) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public int Px2Dp(float px) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    private void swap(int index1,int index2){
        PointPosition temp = positions[index1];
        positions[index1] = positions[index2];
        positions[index2] = temp;
    }
}
