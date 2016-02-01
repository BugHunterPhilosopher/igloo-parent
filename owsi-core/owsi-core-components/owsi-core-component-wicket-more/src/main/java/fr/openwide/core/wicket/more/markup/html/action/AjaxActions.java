package fr.openwide.core.wicket.more.markup.html.action;

import java.util.Objects;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;

import fr.openwide.core.wicket.more.condition.Condition;

public class AjaxActions {

	public static <T> IOneParameterAjaxAction<T> ignoreParameter(final IAjaxAction action) {
		Objects.requireNonNull(action);
		return new IOneParameterAjaxAction<T>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void detach() {
				action.detach();
			}
			@Override
			public void execute(AjaxRequestTarget target, T parameter) {
				action.execute(target);
			}
			@Override
			public void updateAjaxAttributes(AjaxRequestAttributes attributes, T parameter) {
				action.updateAjaxAttributes(attributes);
			}
			@Override
			public Condition getActionAvailableCondition(T parameter) {
				return action.getActionAvailableCondition();
			}
		};
	}

}
