
package net.blacktortoise.android.ai.action;

import net.blacktortoise.android.ai.tagdetector.TagDetectResult;

public class LookAtAction implements IAction<LookAtAction.LookAtArgs, TagDetectResult> {
    public static class LookAtArgs {
        public final int tagKey;

        public final int time;

        public LookAtArgs(int tagKey, int time) {
            super();
            this.tagKey = tagKey;
            this.time = time;
        }
    }

    private FollowTagAction mFollowTagAction;

    @Override
    public void setup(IActionUtil util) {
        mFollowTagAction = new FollowTagAction();
        mFollowTagAction.setup(util);
        mFollowTagAction.setEnableMove(false);
        mFollowTagAction.setFinishOnCenter(true);
    }

    @Override
    public TagDetectResult execute(IActionUtil util, LookAtArgs param) throws InterruptedException {
        return mFollowTagAction.execute(util, new FollowTagAction.FollowTagArgs(param.tagKey,
                param.time));
    }
}
