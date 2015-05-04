
package net.blacktortoise.android.fragment;

import net.blacktortoise.android.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class MainMenuFragment extends BaseFragment implements OnClickListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_menu, null);

        // Bind event listeners
        view.findViewById(R.id.controllerButton).setOnClickListener(this);
        view.findViewById(R.id.serverModeButton).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.controllerButton) {
            ControllerFragment nextFragment = new ControllerFragment();
            replacePrimaryFragment(nextFragment, true);
        } else if (v.getId() == R.id.serverModeButton) {
            ServerModeFragment nextFragment = new ServerModeFragment();
            replacePrimaryFragment(nextFragment, true);
        }
    }
}
