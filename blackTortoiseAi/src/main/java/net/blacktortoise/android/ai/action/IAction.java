
package net.blacktortoise.android.ai.action;

public interface IAction<Param, Result> {
    public void setup(IActionUtil util);

    public Result execute(IActionUtil util, Param param) throws InterruptedException;
}
