
package net.blacktortoise.android.ai.action;

public class HeadAction implements IAction<HeadAction.HeadArgs, Void> {
    public static class HeadArgs {
        public final float yaw;

        public final float pitch;

        public final int sleep;

        public HeadArgs(float yaw, float pitch, int sleep) {
            super();
            this.yaw = yaw;
            this.pitch = pitch;
            this.sleep = 200;
        }
    }

    @Override
    public void setup(IActionUtil util) {

    }

    @Override
    public Void execute(IActionUtil util, HeadArgs param) throws InterruptedException {
        util.getServiceWrapper().sendHead(param.yaw, param.pitch);
        util.updateConsole();
        Thread.sleep(param.sleep);

        return null;
    }
}
