
package net.blacktortoise.android.ai.action;

import net.blacktortoise.android.ai.tagdetector.TagDetectResult;

public class TrackTagAction implements IAction<TrackTagAction.TrackTagArgs, TagDetectResult> {
    public static class TrackTagArgs {
        public final int tagKey;

        public final int time;

        public TrackTagArgs(int tagKey, int time) {
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
        mFollowTagAction.setEnableMove(true);
        mFollowTagAction.setEnableTurn(true);
        mFollowTagAction.setFinishOnCenter(false);
    }

    @Override
    public TagDetectResult execute(IActionUtil util, TrackTagArgs param)
            throws InterruptedException {
        return mFollowTagAction.execute(util, new FollowTagAction.FollowTagArgs(param.tagKey,
                param.time));
    }
}
