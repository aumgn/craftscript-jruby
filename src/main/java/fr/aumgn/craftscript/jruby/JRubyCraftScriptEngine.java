/* 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * (c) Copyright aumgn 2012
 */

package fr.aumgn.craftscript.jruby;

import java.io.IOException;
import java.io.Writer;

import javax.script.ScriptException;

import org.jruby.RubyException;
import org.jruby.embed.ScriptingContainer;
import org.jruby.exceptions.RaiseException;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.scripting.CraftScriptEngine;
import com.sk89q.worldedit.scripting.CraftScriptContext;

public class JRubyCraftScriptEngine implements CraftScriptEngine {

    @Override
    public boolean acceptFilename(String filename) {
        return filename.endsWith(".rb");
    }

    @Override
    public Object evaluate(CraftScriptContext context, final String script)
            throws ScriptException {
        final ScriptingContainer container = new ScriptingContainer();

        container.setClassLoader(WorldEdit.class.getClassLoader());
        container.setError(new NullWriter());

        container.put("context", context);
        container.put("player", context.getPlayer());
        // FIXME: Seems broken
        container.setArgv(context.getArgv());
        container.put("argv", context.getArgv());

        JRubyCraftScriptEvaluation jrubyEvaluation =
                new JRubyCraftScriptEvaluation(container, context.getTimeLimit());
        jrubyEvaluation.run(context.getFilename(), script);

        if (jrubyEvaluation.hasException()) {
            Throwable cause = jrubyEvaluation.getEvalException().getCause();

            String message;
            if (cause instanceof RaiseException) {
                RubyException exc = ((RaiseException) cause).getException();
                message = exc.message.toString();
            } else {
                message = cause.getMessage();
            }

            throw new ScriptException(message, context.getFilename(), 0);
        }

        return jrubyEvaluation.getValue();
    }

    private static class NullWriter extends Writer {

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }
    }
}
