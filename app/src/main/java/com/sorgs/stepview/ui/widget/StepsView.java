package com.sorgs.stepview.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import com.sorgs.stepview.R;
import com.sorgs.stepview.bean.StepBean;
import com.sorgs.stepview.utils.SizeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * description: 自定义签到View.
 *
 * @author Sorgs.
 * @date 2018/8/17.
 */
public class StepsView extends View {

    /**
     * 动画执行的时间 230毫秒
     */
    private final static int ANIMATION_TIME = 230;
    /**
     * 动画执行的间隔次数
     */
    private final static int ANIMATION_INTERVAL = 10;

    /**
     * 线段的高度
     */
    private float mCompletedLineHeight = SizeUtils.dp2px(getContext(), 2f);

    /**
     * 图标宽度
     */
    private float mIconWeight = SizeUtils.dp2px(getContext(), 21.5f);
    /**
     * 图标的高度
     */
    private float mIconHeight = SizeUtils.dp2px(getContext(), 24f);
    /**
     * UP宽度
     */
    private float mUpWeight = SizeUtils.dp2px(getContext(), 20.5f);
    /**
     * up的高度
     */
    private float mUpHeight = SizeUtils.dp2px(getContext(), 12f);

    /**
     * 线段长度
     */
    private float mLineWeight = SizeUtils.dp2px(getContext(), 23f);

    /**
     * 已经完成的图标
     */
    private Drawable mCompleteIcon;
    /**
     * 正在进行的图标
     */
    private Drawable mAttentionIcon;
    /**
     * 默认的图标
     */
    private Drawable mDefaultIcon;
    /**
     * UP图标
     */
    private Drawable mUpIcon;
    /**
     * 图标中心点Y
     */
    private float mCenterY;
    /**
     * 线段的左上方的Y
     */
    private float mLeftY;
    /**
     * 线段的右下方的Y
     */
    private float mRightY;

    /**
     * 数据源
     */
    private List<StepBean> mStepBeanList;
    private int mStepNum = 0;

    private List<Float> mCircleCenterPointPositionList;
    /**
     * 未完成的线段Paint
     */
    private Paint mUnCompletedPaint;
    /**
     * 完成的线段paint
     */
    private Paint mCompletedPaint;
    /**
     * 未完成颜色
     */
    private int mUnCompletedLineColor = ContextCompat.getColor(getContext(), R.color.c_999999);
    /**
     * 天数颜色
     */
    private int mUnCompletedTextColor = ContextCompat.getColor(getContext(), R.color.c_cccccc);
    /**
     * up魅力值颜色
     */
    private int mCurrentTextColor = ContextCompat.getColor(getContext(), R.color.c_f7b93c);
    /**
     * 完成的颜色
     */
    private int mCompletedLineColor = ContextCompat.getColor(getContext(), R.color.c_41c961);

    private Paint mTextNumberPaint;

    /**
     * 是否执行动画
     */
    private boolean isAnimation = false;

    /**
     * 记录重绘次数
     */
    private int mCount = 0;

    /**
     * 执行动画线段每次绘制的长度，线段的总长度除以总共执行的时间乘以每次执行的间隔时间
     */
    private float mAnimationWeight = (mLineWeight / ANIMATION_TIME) * ANIMATION_INTERVAL;

    /**
     * 执行动画的位置
     */
    private int mPosition;

    public StepsView(Context context) {
        this(context, null);
    }

    public StepsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StepsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * init
     */
    private void init() {
        mStepBeanList = new ArrayList<>();

        mCircleCenterPointPositionList = new ArrayList<>();

        mUnCompletedPaint = new Paint();
        mUnCompletedPaint.setAntiAlias(true);
        mUnCompletedPaint.setColor(mUnCompletedLineColor);
        mUnCompletedPaint.setStrokeWidth(2);
        mUnCompletedPaint.setStyle(Paint.Style.FILL);

        mCompletedPaint = new Paint();
        mCompletedPaint.setAntiAlias(true);
        mCompletedPaint.setColor(mCompletedLineColor);
        mCompletedPaint.setStrokeWidth(2);
        mCompletedPaint.setStyle(Paint.Style.FILL);

        //魅力值paint
        mTextNumberPaint = new Paint();
        mTextNumberPaint.setAntiAlias(true);
        mTextNumberPaint.setColor(mUnCompletedTextColor);
        mTextNumberPaint.setStyle(Paint.Style.FILL);
        mTextNumberPaint.setTextSize(SizeUtils.sp2px(getContext(), 8f));

        //已经完成的icon
        mCompleteIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_sign_finish);
        //正在进行的icon
        mAttentionIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_sign_unfinish);
        //未完成的icon
        mDefaultIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_sign_unfinish);
        //UP的icon
        mUpIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_sign_up);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //图标的中中心Y点
        mCenterY = SizeUtils.dp2px(getContext(), 28f) + mIconHeight / 2;
        //获取左上方Y的位置，获取该点的意义是为了方便画矩形左上的Y位置
        mLeftY = mCenterY - (mCompletedLineHeight / 2);
        //获取右下方Y的位置，获取该点的意义是为了方便画矩形右下的Y位置
        mRightY = mCenterY + mCompletedLineHeight / 2;

        //计算图标中心点
        mCircleCenterPointPositionList.clear();
        //第一个点距离父控件左边14.5dp
        float size = mIconWeight / 2 + SizeUtils.dp2px(getContext(), 14.5f);
        mCircleCenterPointPositionList.add(size);
        for (int i = 1; i < mStepNum; i++) {
            //从第二个点开始，每个点距离上一个点为图标的宽度加上线段的23dp的长度
            size = size + mIconWeight + mLineWeight;
            mCircleCenterPointPositionList.add(size);
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isAnimation) {
            drawSign(canvas);
        } else {
            drawUnSign(canvas);
        }
    }

    /**
     * 绘制签到(伴随签到动画)
     */
    @SuppressLint("DrawAllocation")
    private void drawSign(Canvas canvas) {
        for (int i = 0; i < mCircleCenterPointPositionList.size(); i++) {
            //绘制线段
            float preComplectedXPosition = mCircleCenterPointPositionList.get(i) + mIconWeight / 2;
            if (i != mCircleCenterPointPositionList.size() - 1) {
                //最后一条不需要绘制

                if (mStepBeanList.get(i + 1).getState() == StepBean.STEP_COMPLETED) {
                    //下一个是已完成，当前才需要绘制绿色
                    canvas.drawRect(preComplectedXPosition, mLeftY, preComplectedXPosition + mLineWeight,
                            mRightY, mCompletedPaint);
                } else {
                    //其余绘制灰色

                    //当前位置执行动画
                    if (i == mPosition - 1) {
                        //绿色开始绘制的地方,
                        float endX = preComplectedXPosition + mAnimationWeight * (mCount / ANIMATION_INTERVAL);
                        //绘制绿色
                        canvas.drawRect(preComplectedXPosition, mLeftY, endX, mRightY, mCompletedPaint);
                        //绘制灰色
                        canvas.drawRect(endX, mLeftY, preComplectedXPosition + mLineWeight,
                                mRightY, mUnCompletedPaint);
                    } else {
                        canvas.drawRect(preComplectedXPosition, mLeftY, preComplectedXPosition + mLineWeight,
                                mRightY, mUnCompletedPaint);
                    }
                }
            }

            //绘制图标
            float currentComplectedXPosition = mCircleCenterPointPositionList.get(i);
            Rect rect = new Rect((int) (currentComplectedXPosition - mIconWeight / 2),
                    (int) (mCenterY - mIconHeight / 2),
                    (int) (currentComplectedXPosition + mIconWeight / 2),
                    (int) (mCenterY + mIconHeight / 2));

            StepBean stepsBean = mStepBeanList.get(i);

            if (i == mPosition && mCount == ANIMATION_TIME) {
                //当前需要绘制成绿色了
                mCompleteIcon.setBounds(rect);
                mCompleteIcon.draw(canvas);
            } else {
                if (stepsBean.getState() == StepBean.STEP_UNDO) {
                    mDefaultIcon.setBounds(rect);
                    mDefaultIcon.draw(canvas);
                } else if (stepsBean.getState() == StepBean.STEP_CURRENT) {
                    mAttentionIcon.setBounds(rect);
                    mAttentionIcon.draw(canvas);
                } else if (stepsBean.getState() == StepBean.STEP_COMPLETED) {
                    mCompleteIcon.setBounds(rect);
                    mCompleteIcon.draw(canvas);
                }
            }

            if (stepsBean.getState() == StepBean.STEP_COMPLETED || (i == mPosition
                    && mCount == ANIMATION_TIME)) {
                //已经完成了或者是当前动画完成并且需要当前位置需要改变
                if (stepsBean.isUp()) {
                    //是up的需要橙色
                    mTextNumberPaint.setColor(mCurrentTextColor);
                } else {
                    //普通完成的颜色
                    mTextNumberPaint.setColor(mCompletedLineColor);
                }
            } else {
                //还没签到的，颜色均为灰色
                mTextNumberPaint.setColor(mUnCompletedLineColor);
            }

            canvas.drawText("+" + stepsBean.getNumber(),
                    currentComplectedXPosition + SizeUtils.dp2px(getContext(), 2f),
                    mCenterY - mIconHeight / 2 - SizeUtils.dp2px(getContext(), 0.5f),
                    mTextNumberPaint);

            //绘制UP
            if (stepsBean.isUp()) {
                //需要UP才进行绘制
                Rect rectUp =
                        new Rect((int) (currentComplectedXPosition - mUpWeight / 2),
                                (int) (mCenterY - mIconHeight / 2 - SizeUtils.dp2px(getContext(), 8f) - mUpHeight),
                                (int) (currentComplectedXPosition + mUpWeight / 2),
                                (int) (mCenterY - mIconHeight / 2 - SizeUtils.dp2px(getContext(), 8f)));
                mUpIcon.setBounds(rectUp);
                mUpIcon.draw(canvas);
            }
        }

        //记录重绘次数
        mCount = mCount + ANIMATION_INTERVAL;
        if (mCount <= ANIMATION_TIME) {
            //引起重绘
            postInvalidate();
        } else {
            //重绘完成
            isAnimation = false;
            mCount = 0;
        }
    }

    /**
     * 绘制初始状态的view
     */
    @SuppressLint("DrawAllocation")
    private void drawUnSign(Canvas canvas) {
        for (int i = 0; i < mCircleCenterPointPositionList.size(); i++) {
            //绘制线段
            float preComplectedXPosition = mCircleCenterPointPositionList.get(i) + mIconWeight / 2;
            if (i != mCircleCenterPointPositionList.size() - 1) {
                //最后一条不需要绘制
                if (mStepBeanList.get(i + 1).getState() == StepBean.STEP_COMPLETED) {
                    //下一个是已完成，当前才需要绘制绿色
                    canvas.drawRect(preComplectedXPosition, mLeftY, preComplectedXPosition + mLineWeight,
                            mRightY, mCompletedPaint);
                } else {
                    //其余绘制灰色
                    canvas.drawRect(preComplectedXPosition, mLeftY, preComplectedXPosition + mLineWeight,
                            mRightY, mUnCompletedPaint);
                }
            }

            //绘制图标
            float currentComplectedXPosition = mCircleCenterPointPositionList.get(i);
            Rect rect = new Rect((int) (currentComplectedXPosition - mIconWeight / 2),
                    (int) (mCenterY - mIconHeight / 2),
                    (int) (currentComplectedXPosition + mIconWeight / 2),
                    (int) (mCenterY + mIconHeight / 2));

            StepBean stepsBean = mStepBeanList.get(i);

            if (stepsBean.getState() == StepBean.STEP_UNDO) {
                mDefaultIcon.setBounds(rect);
                mDefaultIcon.draw(canvas);
            } else if (stepsBean.getState() == StepBean.STEP_CURRENT) {
                mAttentionIcon.setBounds(rect);
                mAttentionIcon.draw(canvas);
            } else if (stepsBean.getState() == StepBean.STEP_COMPLETED) {
                mCompleteIcon.setBounds(rect);
                mCompleteIcon.draw(canvas);
            }

            //绘制增加的魅力值数目
            if (stepsBean.getState() == StepBean.STEP_COMPLETED) {
                //已经完成了
                if (stepsBean.isUp()) {
                    //是up的需要橙色
                    mTextNumberPaint.setColor(mCurrentTextColor);
                } else {
                    //普通完成的颜色
                    mTextNumberPaint.setColor(mCompletedLineColor);
                }
            } else {
                //还没签到的，颜色均为灰色
                mTextNumberPaint.setColor(mUnCompletedLineColor);
            }
            canvas.drawText("+" + stepsBean.getNumber(),
                    currentComplectedXPosition + SizeUtils.dp2px(getContext(), 2f),
                    mCenterY - mIconHeight / 2 - SizeUtils.dp2px(getContext(), 0.5f),
                    mTextNumberPaint);

            //绘制UP
            if (stepsBean.isUp()) {
                //需要UP才进行绘制
                Rect rectUp =
                        new Rect((int) (currentComplectedXPosition - mUpWeight / 2),
                                (int) (mCenterY - mIconHeight / 2 - SizeUtils.dp2px(getContext(), 8f) - mUpHeight),
                                (int) (currentComplectedXPosition + mUpWeight / 2),
                                (int) (mCenterY - mIconHeight / 2 - SizeUtils.dp2px(getContext(), 8f)));
                mUpIcon.setBounds(rectUp);
                mUpIcon.draw(canvas);
            }
        }
    }

    /**
     * 设置流程步数
     *
     * @param stepsBeanList 流程步数
     */
    public void setStepNum(List<StepBean> stepsBeanList) {
        if (stepsBeanList == null) {
            return;
        }
        this.mStepBeanList = stepsBeanList;
        mStepNum = mStepBeanList.size();
        //引起重绘
        postInvalidate();
    }

    /**
     * 执行签到动画
     *
     * @param position 执行的位置
     */
    public void startSignAnimation(int position) {
        //线条从灰色变为绿色
        isAnimation = true;
        mPosition = position;
        //引起重绘
        postInvalidate();
    }

}
