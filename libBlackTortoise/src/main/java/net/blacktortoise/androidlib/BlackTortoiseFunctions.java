
package net.blacktortoise.androidlib;

import android.content.Intent;

public class BlackTortoiseFunctions {
    public static Intent createServiceIntent() {
        Intent intent = new Intent();
        intent.setClassName(Constants.SERVICE_PACKAGE, Constants.SERVICE_CLASS);
        return intent;
    }

    public static Intent createSelectDeviceActivityIntent() {
        Intent intent = new Intent();
        intent.setClassName(Constants.SELECT_DEVICE_ACTIVITY_PACKAGE,
                Constants.SELECT_DEVICE_ACTIVITY_CLASS);
        return intent;
    }
}
