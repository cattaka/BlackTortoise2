
package net.blacktortoise.android;

import net.blacktortoise.androidlib.data.BtPacket;
import net.blacktortoise.androidlib.data.BtPacketFactory;
import net.cattaka.libgeppa.ActiveGeppaService;
import net.cattaka.libgeppa.data.DeviceInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class BlackTortoiseService extends ActiveGeppaService<BtPacket> {
    private static final int NOTIFICATION_CONNECTED_ID = 1;

    public BlackTortoiseService() {
        super(new BtPacketFactory());
    }

    @Override
    protected void handleConnectedNotification(boolean connected, DeviceInfo deviceInfo) {
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if (connected) {
            Intent intent = new Intent(this, SelectDeviceActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Builder builder = new Notification.Builder(this);
            builder.setContentIntent(pIntent);
            builder.setContentTitle(getText(R.string.notification_title_connected));
            builder.setContentText(deviceInfo.getLabel());
            builder.setSmallIcon(R.drawable.ic_launcher);
            @SuppressWarnings("deprecation")
            Notification nortification = builder.getNotification();
            manager.notify(NOTIFICATION_CONNECTED_ID, nortification);
        } else {
            manager.cancel(NOTIFICATION_CONNECTED_ID);
        }
    }

}
