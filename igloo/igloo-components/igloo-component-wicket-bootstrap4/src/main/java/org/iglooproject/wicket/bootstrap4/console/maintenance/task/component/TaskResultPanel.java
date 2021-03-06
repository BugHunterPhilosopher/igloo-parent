package org.iglooproject.wicket.bootstrap4.console.maintenance.task.component;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Classes;
import org.iglooproject.jpa.more.business.task.util.TaskResult;
import org.iglooproject.wicket.more.condition.Condition;
import org.iglooproject.wicket.more.markup.html.basic.ComponentBooleanProperty;
import org.iglooproject.wicket.more.markup.html.basic.ComponentBooleanPropertyBehavior;
import org.iglooproject.wicket.more.markup.html.basic.impl.AbstractConfigurableComponentBooleanPropertyBehavior.Operator;

public class TaskResultPanel extends Panel {

	private static final long serialVersionUID = -4365862293922374762L;

	private boolean hideIfEmpty;
	
	private String faSize = "fa-lg";
	
	public TaskResultPanel(String id, final IModel<TaskResult> resultModel) {
		super(id, resultModel);
		
		add(
				new WebMarkupContainer("result") {
					private static final long serialVersionUID = 1L;
					
					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						String iconClass = faSize;
						TaskResult result = resultModel.getObject();
						if (result != null) {
							switch (result) {
							case SUCCESS:
								iconClass += " fa-check-circle text-success";
								break;
							case WARN:
								iconClass += " fa-exclamation-triangle text-warning";
								break;
							case ERROR:
								iconClass += " fa-exclamation-circle text-danger";
								break;
							case FATAL:
								iconClass += " fa-times-circle text-danger";
								break;
							}
							tag.append("class", iconClass, " ");
							tag.put("title", getString(Classes.simpleName(TaskResult.class) + "." + result.name()));
						}
					}
				}
				.add(Condition.modelNotNull(resultModel).thenShow())
		);
		
		add(
				new ComponentBooleanPropertyBehavior(ComponentBooleanProperty.VISIBILITY_ALLOWED, Operator.WHEN_ANY_TRUE) {
					private static final long serialVersionUID = 1L;
					
					@Override
					public boolean isEnabled(Component component) {
						return hideIfEmpty;
					}
					
				}.condition(Condition.modelNotNull(resultModel))
		);
	}
	
	public TaskResultPanel hideIfEmpty() {
		hideIfEmpty = true;
		return this;
	}
	
	public TaskResultPanel faSize(String faSize) {
		this.faSize = faSize;
		return this;
	}
}