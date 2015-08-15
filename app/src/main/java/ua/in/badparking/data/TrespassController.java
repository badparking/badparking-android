package ua.in.badparking.data;

import android.text.TextUtils;

/**
 * Created by Dima Kovalenko on 8/15/15.
 */
public enum TrespassController {
    INST;

    private Trespass trespass;

    public Trespass getTrespass() {
        return trespass;
    }

    public boolean isSenderInfoFulfilled() {
        return !TextUtils.isEmpty(trespass.getEmail()) &&
                !TextUtils.isEmpty(trespass.getName()) &&
                !TextUtils.isEmpty(trespass.getPhone());


    }
}
