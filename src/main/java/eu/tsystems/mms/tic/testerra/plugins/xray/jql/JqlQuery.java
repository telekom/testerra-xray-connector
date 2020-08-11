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

import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;


public class JqlQuery implements JqlCreator {

    private final Set<JqlCondition> jqlConditions = new LinkedHashSet<>();

    public JqlQuery(final Collection<JqlCondition> jqlConditions) {
        this.jqlConditions.addAll(jqlConditions);
    }

    public static JqlQueryBuilder create() {
        return new JqlQueryBuilder();
    }

    public static JqlQuery combine(final JqlQuery firstQuery, final JqlQuery secondQuery) {
        final Set<JqlCondition> union = Sets.union(firstQuery.jqlConditions, secondQuery.jqlConditions);
        return new JqlQuery(union);
    }

    @Override
    public String createJql() {
        //TODO: make nice if Java8 is finally used
        return StringUtils.join(Collections2.transform(jqlConditions, JqlUtils.CreateJqlFunc), " AND ");
    }

    public static class JqlQueryBuilder {

        private JqlQueryBuilder() {
        }

        private final Set<JqlCondition> jqlConditions = new LinkedHashSet<>();

        public JqlQueryBuilder addCondition(final JqlCondition jqlCondition) {
            jqlConditions.add(jqlCondition);
            return this;
        }

        public JqlQuery build() {
            return new JqlQuery(jqlConditions);
        }
    }
}
