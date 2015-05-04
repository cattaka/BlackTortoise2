
package net.blacktortoise.android.ai.action;

public abstract class AbsAction<Param, Result> implements IAction<Param, Result> {
    protected void checkInterrupt() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }
}
