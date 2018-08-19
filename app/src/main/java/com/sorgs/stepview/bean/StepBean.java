package com.sorgs.stepview.bean;

/**
 * description: 签到bean.
 *
 * @author Sorgs.
 * @date 2018/8/17.
 */
public class StepBean {
    /**
     * 未完成
     */
    public static final int STEP_UNDO = -1;
    /**
     * 正在进行
     */
    public static final int STEP_CURRENT = 0;
    /**
     * 已完成
     */
    public static final int STEP_COMPLETED = 1;

    private int state;
    private int number;

    public StepBean(int state, int number) {
        this.state = state;
        this.number = number;
    }


    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
