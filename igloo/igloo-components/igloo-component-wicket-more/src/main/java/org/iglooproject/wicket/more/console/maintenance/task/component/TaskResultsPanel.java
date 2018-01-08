package org.iglooproject.wicket.more.console.maintenance.task.component;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.iglooproject.jpa.more.business.sort.ISort;
import org.iglooproject.jpa.more.business.task.model.QueuedTaskHolder;
import org.iglooproject.jpa.more.util.binding.CoreJpaMoreBindings;
import org.iglooproject.wicket.more.console.maintenance.task.model.QueuedTaskHolderDataProvider;
import org.iglooproject.wicket.more.console.maintenance.task.page.ConsoleMaintenanceTaskDescriptionPage;
import org.iglooproject.wicket.more.markup.repeater.table.DecoratedCoreDataTablePanel;
import org.iglooproject.wicket.more.markup.repeater.table.builder.DataTableBuilder;
import org.iglooproject.wicket.more.util.DatePattern;
import org.iglooproject.wicket.more.util.model.Detachables;

public class TaskResultsPanel extends Panel {

	private static final long serialVersionUID = 5519060838663253181L;

	private final QueuedTaskHolderDataProvider dataProvider;

	private final DecoratedCoreDataTablePanel<QueuedTaskHolder, ISort<?>> dataTable;

	public TaskResultsPanel(String id, final QueuedTaskHolderDataProvider dataProvider, int itemsPerPage) {
		super(id);
		
		this.dataProvider = dataProvider;
		
		dataTable =
				DataTableBuilder.start(dataProvider)
						.addLabelColumn(new ResourceModel("console.maintenance.task.common.status"), CoreJpaMoreBindings.queuedTaskHolder().status())
						.withClass("status")
				.addLabelColumn(new ResourceModel("console.maintenance.task.common.result"), CoreJpaMoreBindings.queuedTaskHolder().result())
						.withClass("result")
				.addLabelColumn(new ResourceModel("console.maintenance.task.common.name"), CoreJpaMoreBindings.queuedTaskHolder().name())
						.withLink(ConsoleMaintenanceTaskDescriptionPage.MAPPER)
						.withClass("name")
				.addLabelColumn(new ResourceModel("console.maintenance.task.common.queue"), CoreJpaMoreBindings.queuedTaskHolder().queueId())
						.showPlaceholder(new ResourceModel("console.maintenance.task.common.queue.default"))
						.withClass("queue")
				.addLabelColumn(new ResourceModel("console.maintenance.task.common.creationDate"), CoreJpaMoreBindings.queuedTaskHolder().creationDate(), DatePattern.SHORT_DATETIME)
						.withClass("date")
				.addLabelColumn(new ResourceModel("console.maintenance.task.common.startDate"), CoreJpaMoreBindings.queuedTaskHolder().startDate(), DatePattern.SHORT_DATETIME)
						.withClass("date")
				.addLabelColumn(new ResourceModel("console.maintenance.task.common.endDate"), CoreJpaMoreBindings.queuedTaskHolder().endDate(), DatePattern.SHORT_DATETIME)
						.withClass("date")
				.withNoRecordsResourceKey("console.maintenance.task.list.empty")
				.decorate()
						.ajaxPagers()
				.build("results", itemsPerPage);
		
		add(
				dataTable
		);
	}

	public DecoratedCoreDataTablePanel<QueuedTaskHolder, ISort<?>> getDataTable() {
		return dataTable;
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		Detachables.detach(dataProvider);
	}

}