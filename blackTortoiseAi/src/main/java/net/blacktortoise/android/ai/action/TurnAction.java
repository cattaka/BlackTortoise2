
package net.blacktortoise.android.ai.action;

public class TurnAction implements IAction<TurnAction.TurnArgs, Void> {
    public static class TurnArgs {
        public final float turn;

        public final int time;

        public TurnArgs(float turn, int time) {
            super();
            this.turn = turn;
            this.time = time;
        }

    }

    @Override
    public void setup(IActionUtil util) {

    }

    @Override
    public Void execute(IActionUtil util, TurnArgs param) throws InterruptedException {
        try {
            if (param.turn > 0) {
                util.getServiceWrapper().sendMove(1, 1);
            } else {
                util.getServiceWrapper().sendMove(1, -1);
            }
            util.updateConsole();
            Thread.sleep(param.time);
        } finally {
            util.getServiceWrapper().sendMove(0, 0);
        }

        util.updateConsole();
        return null;
    }
}
