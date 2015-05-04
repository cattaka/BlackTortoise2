
package net.blacktortoise.android.ai.action;

public class MoveAction implements IAction<MoveAction.MoveArgs, Void> {
    public static class MoveArgs {
        public final float forward;

        public final int time;

        public MoveArgs(float forward, int time) {
            super();
            this.forward = forward;
            this.time = time;
        }
    }

    @Override
    public void setup(IActionUtil util) {

    }

    @Override
    public Void execute(IActionUtil util, MoveArgs param) throws InterruptedException {
        try {
            util.getServiceWrapper().sendMove(param.forward, 0);
            util.updateConsole();
            Thread.sleep(param.time);
        } finally {
            util.getServiceWrapper().sendMove(0, 0);
        }

        util.updateConsole();
        return null;
    }
}
