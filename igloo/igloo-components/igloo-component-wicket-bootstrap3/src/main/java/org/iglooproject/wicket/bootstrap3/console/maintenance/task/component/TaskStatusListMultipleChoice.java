package org.iglooproject.wicket.bootstrap3.console.maintenance.task.component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.lang.Classes;
import org.iglooproject.jpa.more.business.task.util.TaskStatus;

public class TaskStatusListMultipleChoice extends ListMultipleChoice<TaskStatus> {

	private static final long serialVersionUID = 3147073422245294521L;

	public TaskStatusListMultipleChoice(String id, IModel<? extends Collection<TaskStatus>> statusListModel) {
		super(id, statusListModel, new LoadableDetachableModel<List<TaskStatus>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<TaskStatus> load() {
				return Arrays.asList(TaskStatus.values());
			}
		});
		setChoiceRenderer(new TaskStatusChoiceRenderer());
	}

	private class TaskStatusChoiceRenderer  extends ChoiceRenderer<TaskStatus> {
		private static final long serialVersionUID = 1L;

		@Override
		public Object getDisplayValue(final TaskStatus object) {
			return object != null ? new StringResourceModel(Classes.simpleName(TaskStatus.class) + ".${}",
					TaskStatusListMultipleChoice.this, Model.of(object.name())).getString() : "";
		}

		@Override
		public String getIdValue(TaskStatus object, int index) {
			return object != null ? String.valueOf(index) : "-1";
		}
	}
}
