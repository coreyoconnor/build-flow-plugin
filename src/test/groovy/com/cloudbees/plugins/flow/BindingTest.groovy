/*
 * Copyright (C) 2011 CloudBees Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 */

package com.cloudbees.plugins.flow

import static hudson.model.Result.SUCCESS
import static hudson.model.Result.FAILURE
import hudson.model.Job
import jenkins.model.Jenkins
import org.junit.Ignore
import hudson.slaves.EnvironmentVariablesNodeProperty
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry
import hudson.model.ParametersDefinitionProperty
import hudson.model.StringParameterDefinition

class BindingTest extends DSLTestCase {

    public void testBindings() {
        Job job1 = createJob("job1")
        def flow = run("""
            out.println("yeah")
            build("job1", param1: build.number)
        """)
        def build = assertSuccess(job1)
        assertHasParameter(build, "param1", "1")
        assert SUCCESS == flow.result
    }

    public void testEnvBinding() {
        jenkins.globalNodeProperties.add(new EnvironmentVariablesNodeProperty(new Entry("someGlobalProperty", "expectedGlobalPropertyValue")))
        jenkins.nodeProperties.add(new EnvironmentVariablesNodeProperty(new Entry("someNodeProperty", "expectedNodePropertyValue")))

        Job job1 = createJob("job1")
        def flow = run("""
            out.println("yeah")
            build("job1", param1: env["someGlobalProperty"], param2: env["someNodeProperty"])
        """)
        def build = assertSuccess(job1)
        assertHasParameter(build, "param1", "expectedGlobalPropertyValue")
        assertHasParameter(build, "param2", "expectedNodePropertyValue")
        assert SUCCESS == flow.result
    }

    public void testNoBindingsMeansDefaults() {
        def job1 = createJob("job1")
        def param1 = new StringParameterDefinition("param1", "expectDefaultValue", "")
        ParametersDefinitionProperty param_defs = new ParametersDefinitionProperty(param1)
        job1.addProperty(param_defs)

        def flow = run("""
            build("job1")
        """)
        def build = assertSuccess(job1);
        assertHasParameter( build, "param1", "expectDefaultValue")
        assert SUCCESS == flow.result
    }
}
