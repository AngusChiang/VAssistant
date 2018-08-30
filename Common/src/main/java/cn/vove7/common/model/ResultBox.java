package cn.vove7.common.model;

/**
 * Created by Vove on 2018/7/5
 */
public class ResultBox<T> {
    private T mValue;

    public ResultBox() {
    }

    public ResultBox(T mValue) {
        this.mValue = mValue;
    }

    public T get() {
        return mValue;
    }

    public void set(T value) {
        this.mValue = value;
    }

    public void setAndNotify(T value) {
        mValue = value;
        synchronized (this) {
            notify();
        }
    }

    //等待结果
    public T blockedGet() {
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return mValue;
    }


}
