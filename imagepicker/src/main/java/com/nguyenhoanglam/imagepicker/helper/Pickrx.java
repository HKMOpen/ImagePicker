package com.nguyenhoanglam.imagepicker.helper;

import com.hwangjr.rxbus.Bus;

/**
 * Created by hesk on 16/10/19.
 */
public final class Pickrx {
    private static Bus sBus;

    public static synchronized Bus get() {
        if (sBus == null) {
            sBus = new Bus();
        }
        return sBus;
    }
}
