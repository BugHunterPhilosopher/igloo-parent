package test.jpa.more.business.task.config;

import java.util.Collection;

import org.apache.commons.lang3.EnumUtils;
import org.iglooproject.config.bootstrap.spring.annotations.ConfigurationLocations;
import org.iglooproject.jpa.more.business.task.model.IQueueId;
import org.iglooproject.jpa.more.config.spring.AbstractTaskManagementConfig;
import org.springframework.context.annotation.Configuration;

import test.jpa.more.business.task.model.TestQueueId;

@Configuration
@ConfigurationLocations(locations = "jpa-more-test-task-management.properties")
public class TestTaskManagementConfig extends AbstractTaskManagementConfig {

	@Override
	public Collection<? extends IQueueId> queueIds() {
		return EnumUtils.getEnumList(TestQueueId.class);
	}

}
