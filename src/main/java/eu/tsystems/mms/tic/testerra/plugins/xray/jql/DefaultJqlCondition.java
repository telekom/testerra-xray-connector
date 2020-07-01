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

import eu.tsystems.mms.tic.testerra.plugins.xray.jql.predefined.Operator;
import org.apache.commons.lang3.StringUtils;


public class DefaultJqlCondition implements JqlCondition {

    private final String field;
    private final Operator operator;
    private final JqlOperand operand;

    @Override
    public String getField() {
        return field;
    }

    @Override
    public Operator getOperator() {
        return operator;
    }

    @Override
    public JqlOperand getOperand() {
        return operand;
    }

    public DefaultJqlCondition(final String field, final Operator operator, final JqlOperand operand) {
        this.field = field;
        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public String createJql() {
        final StringBuilder sb = new StringBuilder();
        final String join = StringUtils.join(new String[] {field, operator.createJql(), operand.createJql()}, ' ');
        sb.append(join);
        return sb.toString();
    }

}
