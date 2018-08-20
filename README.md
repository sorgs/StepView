
# 引言
涉及到一个签到的步骤view

需求：以七天为周天，执行当天签到需要一个动画效果；签到前灰色，签到后变为绿色；每天加的分数不一定，第三天和第七天加的比较多，分数签到完成为橙色，有up标签。

- 效果图：
![-](http://ogrop3bok.bkt.clouddn.com/stepView.gif)

# 分析
- 首先是把该绘制的东西绘制到画布上，这点没什么好说，上一遍博客差不多说了怎么去绘制。
- 先根据数据绘制出静态的东西。把未签到的东西全部绘制完毕。
- 然后开始绘制动画。处理动画的方式，利用postInvalidate()引起重绘，每次画一点点的橙色进度，后面部分绘制为未签到的灰色。每次更新增加一点点橙色的进度，这样在快速的情况下，就是一个连续的动画效果

# 封装状态bean
``` Java
public StepBean(int state, int number) {
    this.state = state;
    this.number = number;
}
```

- state：封装了3个状态，代表已完成签到，当前进行的签到，和未签到
- number：封装添加的分数

# 初始化
- 把一些具体的画笔，资源文件等初始化出来
``` Java
//已经完成的icon
mCompleteIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_sign_finish);
//正在进行的icon
mAttentionIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_sign_unfinish);
//未完成的icon
mDefaultIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_sign_unfinish);
//UP的icon
mUpIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_sign_up);
```
初始化一些paint就不再介绍，这里就说下初始化Drawable文件，利用ContextCompat.getDrawable()把资源文件引入。因为未签到和当前签到都是属于还没有签到，所以都是展示没有签到的图标。

# 测量
- onMeasure():这里没有太多操作，仅仅把值设置下
``` Java
setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
```
- onSizeChanged（）:这里操作就多了一些，主要是需要确定下来图标绘制的位置，已经线段的位置。（这里的说是线段，其实就是当矩形来绘制），注释已经写了很清楚，这里不多做说明了
``` Java
//图标的中中心Y点
mCenterY = CalcUtils.dp2px(getContext(), 28f) + mIconHeight / 2;
//获取左上方Y的位置，获取该点的意义是为了方便画矩形左上的Y位置
mLeftY = mCenterY - (mCompletedLineHeight / 2);
//获取右下方Y的位置，获取该点的意义是为了方便画矩形右下的Y位置
mRightY = mCenterY + mCompletedLineHeight / 2;
//计算图标中心点
mCircleCenterPointPositionList.clear();
//第一个点距离父控件左边14.5dp
float size = mIconWeight / 2 + CalcUtils.dp2px(getContext(), 14.5f);
mCircleCenterPointPositionList.add(size);
for (int i = 1; i < mStepNum; i++) {
    //从第二个点开始，每个点距离上一个点为图标的宽度加上线段的23dp的长度
    size = size + mIconWeight + mLineWeight;
    mCircleCenterPointPositionList.add(size);
}
```

# 传值
这里对外界暴露了一个方法，传入封装好的bean的List
``` Java
/**
 * 设置流程步数
 *
 * @param stepsBeanList 流程步数
 */
public void setStepNum(List<StepBean> stepsBeanList) {
    if (stepsBeanList == null) {
        return;
    }
    mStepBeanList = stepsBeanList;
    mStepNum = mStepBeanList.size();
    //找出最大的两个值的位置
    mMax = CalcUtils.findMax(stepsBeanList);
    //引起重绘
    postInvalidate();
}
```
值传递进来之后调用postInvalidate()方法,引起重绘，调用draw()方法，进行再次绘制。并且把List里面的最大两个值的位置找出来，在后面方便设置UP标志。
``` Java
/**
 * 寻到最大两个值的位置
 */
public static int[] findMax(List<StepBean> steps) {
    int[] value = new int[2];
    int[] position = new int[2];
    int temValue;
    int temPosition;
    for (int i = 0; i < steps.size(); i++) {
        if (steps.get(i).getNumber() > value[1]) {
            //比较出大的放到value[0]中
            value[1] = steps.get(i).getNumber();
            position[1] = i;
        }
        if (value[1] > value[0]) {
            //把最大的放到value[0]中,交换位置
            temValue = value[0];
            value[0] = value[1];
            value[1] = temValue;
            temPosition = position[0];
            position[0] = position[1];
            position[1] = temPosition;
        }
    }
    return position;
}
```
寻找最大值，我的想法是通过一次循环找出来，采用一个数组存储的方式，时间复杂度为O(n)。可能方法并非最优，如果有更好的方式的欢迎指教~！
       
# 绘制
> 绘制我这里分为了两步，第一步，是传入值之后，便绘制出签到之前的View，也就是静态的。然后提供一个方法暴露出去，待调用的时候开始执行签到动画，完成动态的绘制

- 绘制签到之前的View
``` Java
if (isAnimation) {
    drawSign(canvas);
} else {
    drawUnSign(canvas);
}
```
在onDraw()方法里面，我使用isAnimation，默认为false，调用绘制未签到状态的View，待调用执行动画方法时候为true，执行另一个方法。
 - 绘制线段
``` Java
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
``` 
我们在mCircleCenterPointPositionList里面存储了签到每个步骤的图标中心点X坐标。那么就拿出来，进行绘制。
     - 线段是比图标少一个的，那么可以少画第一条或者少画最后一条(相对图标)。我采取的是最后一条不绘制。那么每条线段就在每个步骤图标的后面，获取到图标的中线点X坐标，加上图标宽度的一般，就是该线段的X坐标。其余的根据已经固定的Y坐标和线段长度绘制便可。这里主要是根据当前状态，不是已经签到了，则绘制为灰色，已经签到才绘制为绿色。
 - 绘制图标
``` Java
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
```
对于图标的绘制，也是非常简单的计算，既然已经获取到了每个图标的中心X坐标，那么根据图标的大小计算出左上角和右下角，然后根据state绘制即可。

 - 绘制分数
``` Java
//绘制增加的分数数目
if (stepsBean.getState() == StepBean.STEP_COMPLETED) {
    //已经完成了
    if (i == mMax[0] || i == mMax[1]) {
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
        currentComplectedXPosition + CalcUtils.dp2px(getContext(), 2f),
        mCenterY - mIconHeight / 2 - CalcUtils.dp2px(getContext(), 0.5f),
        mTextNumberPaint);
```
对于分数，就依附在每个图标的上方，根据设计师给的标注，找出文本的左下角坐标(默认文本绘制是文本的左下角坐标)绘制。注意的是，要根据找出最大两个值的位置，如果是较大的两个，最需要为橙色

 - 绘制UP图标
``` Java
//绘制UP
if (i == mMax[0] || i == mMax[1]) {
    //需要UP才进行绘制
    Rect rectUp =
            new Rect((int) (currentComplectedXPosition - mUpWeight / 2),
                    (int) (mCenterY - mIconHeight / 2 - CalcUtils.dp2px(getContext(), 8f) - mUpHeight),
                    (int) (currentComplectedXPosition + mUpWeight / 2),
                    (int) (mCenterY - mIconHeight / 2 - CalcUtils.dp2px(getContext(), 8f)));
    mUpIcon.setBounds(rectUp);
    mUpIcon.draw(canvas);
}
```
Up图标的绘制依附在增加的分数上面，也是根据较大两个值的位置绘制，计算出左上角和右下角进行绘制。

- 静态绘制完毕，就已经展示出来了未签到状态的View。
``` Java
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
```
我这里暴露出执行动画的方法，将要执行动画的位置传入。（这里要传位置是因为后台数据所致，也是可以根据state位置自行找出）。将isAnimation赋值true,调用postInvalidate()，再次调用Drwa()方法进行绘制。
 - 绘制线段动画
``` Java
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
```
     - 对于未签到和已经签到的和上面的绘制没有太多变，仅仅在当前签到位置执行动画效果
     - 定义mCount为整个动画执行分段的次数记录；ANIMATION_INTERVAL为每次动画执行的时间间隔，暂定10ms；mAnimationWeight为每次间隔中增加的长度。然后每次用根据是分度绘制的第几次算出绘制橙色的长度，然后根据线段长度减去这段长度算出灰色的长度，进行绘制。

 - 绘制图标，文字，up动画
``` Java
if (i == mPosition && mCount == ANIMATION_TIME) {
    //当前需要绘制成绿色了
    mCompleteIcon.setBounds(rect);
    mCompleteIcon.draw(canvas);
}
```
对于这部分的绘制，变化不太多，因为需求是线段动画执行完毕，就把文本、图标变为绿色，如果是较大两个值的地方，则变为橙色(这部分代码没有太多粘贴，详情请见demo项目)

 - 计算动画执行的次数
``` Java
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
```
维护了一个mCount，记录动画分段执行的次数，当值达到了要求的动画执行时间，变停止重绘，否则，调用postInvalidate()进行重绘，增加mCount的值。

# 调用
- 在activity或者dialog等里面封装List，调用
``` Java
private void initData() {
    mStepBeans.add(new StepBean(StepBean.STEP_COMPLETED, 2));
    mStepBeans.add(new StepBean(StepBean.STEP_COMPLETED, 4));
    mStepBeans.add(new StepBean(StepBean.STEP_CURRENT, 10));
    mStepBeans.add(new StepBean(StepBean.STEP_UNDO, 2));
    mStepBeans.add(new StepBean(StepBean.STEP_UNDO, 4));
    mStepBeans.add(new StepBean(StepBean.STEP_UNDO, 4));
    mStepBeans.add(new StepBean(StepBean.STEP_UNDO, 30));
    mStepView.setStepNum(mStepBeans);
}

private void initListener() {
    mTvSign.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mStepView.startSignAnimation(2);
        }
    });
}
```


# 结语
- 这部分的自定义还是很简单，主要是对待动画处理上。之前拿到这个需求，完全不知道怎么去完成动画效果，请教之后才明白，就是不停的引起重绘完成。在原理上是每次多绘制一部分，但在视觉上因为快速（低于16ms）形成了动画(或许还有其他方式)。比如歌词同步也是差不多是这个原理。
- 该部分自定义View很简单，但是我感觉到自己通过不断的学习慢慢在了解到更多的方式，欢迎各位尝试！
- 代码已经放在GitHub，如果有帮助到您，希望不要忘记点颗小星星。[https://github.com/sorgs/StepView](https://github.com/sorgs/StepView "https://github.com/sorgs/StepView")