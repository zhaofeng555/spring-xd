/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.xd.dirt.plugins.job;

import org.springframework.integration.x.bus.MessageBus;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.Assert;
import org.springframework.xd.dirt.plugins.AbstractJobPlugin;
import org.springframework.xd.module.DeploymentMetadata;
import org.springframework.xd.module.core.Module;

/**
 * Plugin to enable job partitioning.
 * 
 * @author Gary Russell
 * @author Ilayaperumal Gopinathan
 * @since 1.0
 */
public class JobPartitionerPlugin extends AbstractJobPlugin {

	private static final String JOB_PARTIONER_REQUEST_CHANNEL = "stepExecutionRequests.output";

	private static final String JOB_PARTIONER_REPLY_CHANNEL = "stepExecutionReplies.input";

	private static final String JOB_STEP_EXECUTION_REQUEST_CHANNEL = "stepExecutionRequests.input";

	private static final String JOB_STEP_EXECUTION_REPLY_CHANNEL = "stepExecutionReplies.output";

	public JobPartitionerPlugin(MessageBus messageBus) {
		super(messageBus);
	}

	@Override
	public void postProcessModule(Module module) {
		DeploymentMetadata md = module.getDeploymentMetadata();
		if (messageBus != null) {
			if (module.getComponent(JOB_PARTIONER_REQUEST_CHANNEL, MessageChannel.class) != null) {
				this.processPartitionedJob(module, md);
			}
		}
	}

	private void processPartitionedJob(Module module, DeploymentMetadata md) {
		if (logger.isDebugEnabled()) {
			logger.debug("binding job partitioning channels for " + module);
		}
		MessageChannel partitionsOut = module.getComponent(JOB_PARTIONER_REQUEST_CHANNEL, MessageChannel.class);
		Assert.notNull(partitionsOut, "Partitioned jobs must have a " + JOB_PARTIONER_REQUEST_CHANNEL);
		MessageChannel partitionsIn = module.getComponent(JOB_PARTIONER_REPLY_CHANNEL, MessageChannel.class);
		Assert.notNull(partitionsIn, "Partitioned jobs must have a " + JOB_PARTIONER_REPLY_CHANNEL);
		String name = md.getGroup() + "." + md.getIndex();
		messageBus.bindRequestor(name, partitionsOut, partitionsIn);

		MessageChannel stepExecutionsIn = module.getComponent(JOB_STEP_EXECUTION_REQUEST_CHANNEL, MessageChannel.class);
		Assert.notNull(stepExecutionsIn, "Partitioned jobs must have a " + JOB_STEP_EXECUTION_REQUEST_CHANNEL);
		MessageChannel stepExecutionResultsOut = module.getComponent(JOB_STEP_EXECUTION_REPLY_CHANNEL,
				MessageChannel.class);
		Assert.notNull(stepExecutionResultsOut, "Partitioned jobs must have a " + JOB_STEP_EXECUTION_REPLY_CHANNEL);
		messageBus.bindReplier(name, stepExecutionsIn, stepExecutionResultsOut);
	}

	private void unbindPartitionedJob(Module module) {
		if (logger.isDebugEnabled()) {
			logger.debug("unbinding job partitioning channels for " + module);
		}
		DeploymentMetadata md = module.getDeploymentMetadata();
		MessageChannel partitionsOut = module.getComponent(JOB_PARTIONER_REQUEST_CHANNEL, MessageChannel.class);
		String name = md.getGroup() + "." + md.getIndex();
		if (partitionsOut != null) {
			messageBus.unbindProducer(name, partitionsOut);
		}
		MessageChannel partitionsIn = module.getComponent(JOB_PARTIONER_REPLY_CHANNEL, MessageChannel.class);
		if (partitionsIn != null) {
			messageBus.unbindConsumer(name, partitionsIn);
		}
		MessageChannel stepExcutionsIn = module.getComponent(JOB_STEP_EXECUTION_REQUEST_CHANNEL, MessageChannel.class);
		if (stepExcutionsIn != null) {
			messageBus.unbindConsumer(name, stepExcutionsIn);
		}
		MessageChannel stepExecutionResultsOut = module.getComponent(JOB_STEP_EXECUTION_REPLY_CHANNEL,
				MessageChannel.class);
		if (stepExecutionResultsOut != null) {
			messageBus.unbindProducer(name, stepExecutionResultsOut);
		}
	}

	@Override
	public void removeModule(Module module) {
		if (module.getComponent(JOB_PARTIONER_REQUEST_CHANNEL, MessageChannel.class) != null) {
			this.unbindPartitionedJob(module);
		}
	}
}
