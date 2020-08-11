/*
 * Testerra Xray-Connector
 *
 * (C) 2020, Martin Cölln, T-Systems Multimedia Solutions GmbH, Deutsche Telekom AG
 *
 * Deutsche Telekom AG and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package eu.tsystems.mms.tic.testerra.plugins.xray.jql;

import com.google.common.base.Function;


public final class JqlUtils {

    public static final Function<JqlCondition, String> CreateJqlFunc = new Function<JqlCondition, String>() {
        public String apply(final JqlCondition condition) {
            return condition.createJql();
        }
    };

    private JqlUtils() {
    }

    public static String wrapInHyphens(final String s) {
        return '"' + s + '"';
    }

    public static String wrapInEscapedHyphens(final String s) {
        return "\\" + '"' + s + '\\' + '"';
    }

    public static String wrapIn(final String before, final String after, final String s) {
        return before + s + after;
    }

    public static String wrapIn(final char before, final char after, final String s) {
        return before + s + after;
    }

    public static String wrapIn(final char wrapper, final String s) {
        return wrapper + s + wrapper;
    }

    public static String wrapIn(final String wrapper, final String s) {
        return wrapper + s + wrapper;
    }


    public static String wrapInBrackets(final String s) {
        return "(" + s + ")";
    }
}
