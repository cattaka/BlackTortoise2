
package net.blacktortoise.android.ai.thread;

import net.blacktortoise.android.ai.action.IActionUtil;
import net.blacktortoise.android.ai.action.TrackTagAction;
import net.blacktortoise.android.ai.action.TrackTagAction.TrackTagArgs;

public class ActionThread extends Thread {
    private IActionUtil mActionUtil;

    public ActionThread(IActionUtil actionUtil) {
        super();
        mActionUtil = actionUtil;
    }

    @Override
    public void run() {
        super.run();
        TrackTagAction action = new TrackTagAction();
        action.setup(mActionUtil);

        mActionUtil.getServiceWrapper().sendMove(0, 0);
        mActionUtil.getServiceWrapper().sendHead(0, 0);
        try {
            int targetid = mActionUtil.getTagDetector().getTagItemIdAt(0);
            while (!Thread.interrupted()) {
                action.execute(mActionUtil, new TrackTagArgs(targetid, 15000));
            }
        } catch (InterruptedException e) {
            // OK
        }
    }

    public void stopSafety() {
        try {
            interrupt();
            join();
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }
    }
}
