package org.iglooproject.wicket.more.markup.html.form;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.iglooproject.wicket.more.condition.Condition;

public enum FormMode {

	ADD,
	EDIT;

	public Condition condition(IModel<FormMode> formModeModel) {
		return Condition.isEqual(Model.of(this), formModeModel);
	}

}
