package com.lockscreen.api.liberal.data;

/**
 * Created by song on 15/11/4.
 */
public class IntType extends DataType<Integer> {
    public IntType(String key, int value) {
        super(key, value);
    }

    @Override
    protected void save() {
        sp.edit().putInt(key, value).apply();
    }

    @Override
    protected void read() {
        value = sp.getInt(key, value);
    }
}
