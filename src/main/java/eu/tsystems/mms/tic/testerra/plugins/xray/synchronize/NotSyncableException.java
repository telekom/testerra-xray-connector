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

package eu.tsystems.mms.tic.testerra.plugins.xray.synchronize;

@Deprecated
public class NotSyncableException extends RuntimeException {

    private final boolean expected;

    public NotSyncableException(final String message) {
        super(message);
        this.expected = false;
    }

    public NotSyncableException(final String message, final boolean expected) {
        super(message);
        this.expected = expected;
    }

    public NotSyncableException(final Throwable cause) {
        super(cause);
        this.expected = false;
    }

    public NotSyncableException(final Throwable cause, boolean expected) {
        super(cause);
        this.expected = expected;
    }

    public boolean isExpected() {
        return expected;
    }
}
