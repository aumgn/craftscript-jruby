/* 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * (c) Copyright aumgn 2012
 */

package fr.aumgn.craftscript.jruby;

import org.jruby.embed.EvalFailedException;
import org.jruby.embed.ScriptingContainer;

public class JRubyCraftScriptEvaluation {

    private final ScriptingContainer container;
    private final int timeLimit;

    private Object value;
    private EvalFailedException exception;

    public JRubyCraftScriptEvaluation(ScriptingContainer container, int timeLimit) {
        this.container = container;
        this.timeLimit = timeLimit;
        value = null;
        exception = null;
    }

    public void run(String filename, final String script) throws Error {
        container.setScriptFilename(filename);

        Thread evaluation = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    value = container.runScriptlet(script);
                } catch (EvalFailedException exc) {
                    exception = exc;
                }
            }
        });

        evaluation.start();
        try {
            evaluation.join(timeLimit);
        } catch (InterruptedException exc) {
            throw new Error("Unexpected interruption");
        }

        if (evaluation.isAlive()) {
            container.terminate();
            evaluation.interrupt();
            throw new Error("Script timed out (" + timeLimit + "ms)");
        }
    }

    public boolean hasException() {
        return exception != null;
    }

    public EvalFailedException getEvalException() {
        return exception;
    }

    public Object getValue() {
        return value;
    }
}
