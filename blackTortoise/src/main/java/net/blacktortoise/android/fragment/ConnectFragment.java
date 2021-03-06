
package net.blacktortoise.android.fragment;

import net.blacktortoise.android.R;
import net.blacktortoise.android.SelectDeviceActivity;
import net.blacktortoise.androidlib.BlackTortoiseServiceWrapper;
import net.cattaka.libgeppa.data.DeviceEventCode;
import net.cattaka.libgeppa.data.DeviceInfo;
import net.cattaka.libgeppa.data.DeviceState;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class ConnectFragment extends BaseFragment implements OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connect, null);

        // Binds event listener
        view.findViewById(R.id.startButton).setOnClickListener(this);
        view.findViewById(R.id.goToSelectDeviceButton).setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        BlackTortoiseServiceWrapper wrapper = getServiceWrapper();
        int v = (wrapper != null && wrapper.getCurrentDeviceInfo() != null) ? View.VISIBLE
                : View.INVISIBLE;
        getView().findViewById(R.id.startButton).setVisibility(v);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.startButton) {
            connectToService();
        } else if (v.getId() == R.id.goToSelectDeviceButton) {
            Intent intent = new Intent(getContext(), SelectDeviceActivity.class);
            startActivity(intent);
        }
    }

    private void connectToService() {
        MainMenuFragment nextFragment = new MainMenuFragment();
        replacePrimaryFragment(nextFragment, false);
    }

    @Override
    public void onDeviceStateChanged(DeviceState state, DeviceEventCode code, DeviceInfo deviceInfo) {
        super.onDeviceStateChanged(state, code, deviceInfo);

        int v = (state == DeviceState.CONNECTED) ? View.VISIBLE : View.INVISIBLE;
        getView().findViewById(R.id.startButton).setVisibility(v);
    }
}
