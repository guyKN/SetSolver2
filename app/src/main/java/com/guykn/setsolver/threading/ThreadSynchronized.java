package com.guykn.setsolver.threading;

public class ThreadSynchronized<T> {
    private T value;
    public void set(T t){
        synchronized (this){
            this.value = t;
        }
    }

    public T get(){
        synchronized (this){
            return value;
        }
    }
}
