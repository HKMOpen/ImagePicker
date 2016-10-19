package com.nguyenhoanglam.imagepicker.helper;

import com.hwangjr.rxbus.Bus;

/**
 * Created by hesk on 16年10月19日.
 */

public final class RxBux {
    private static Bus sBus;

    public static synchronized Bus get() {
        if (sBus == null) {
            sBus = new Bus();
        }
        return sBus;
    }
}
