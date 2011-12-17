package fr.openwide.core.wicket.more.markup.html.form;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.odlabs.wiquery.core.IWiQueryPlugin;
import org.odlabs.wiquery.core.javascript.JsQuery;
import org.odlabs.wiquery.core.javascript.JsScope;
import org.odlabs.wiquery.core.javascript.JsScopeContext;
import org.odlabs.wiquery.core.javascript.JsStatement;

public abstract class AutocompleteAjaxComponent<T> extends org.odlabs.wiquery.ui.autocomplete.AutocompleteAjaxComponent<T>
		implements IWiQueryPlugin {
	
	private static final long serialVersionUID = 2543997784221712556L;
	
	private WebMarkupContainer cleanLink;

	public AutocompleteAjaxComponent(String id, IModel<T> model, IChoiceRenderer<? super T> choiceRenderer) {
		super(id, model, choiceRenderer);
		
		cleanLink = new WebMarkupContainer("cleanLink");
		cleanLink.setOutputMarkupId(true);
		
		add(cleanLink);
	}

	public AutocompleteAjaxComponent(String id, IModel<T> model) {
		this(id, model, null);
	}
	
	@Override
	public JsStatement statement() {
		JsScope jsScope = new JsScope() {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected void execute(JsScopeContext scopeContext) {
				// cette suite d'événement permet de simuler de manière correcte le vidage du champ, et permet
				// en particulier de déclencher le autoUpdate (appel Ajax) vers Wicket.
				// focus du champ -> on vide -> on quitte le champ
				scopeContext.append(new JsQuery(getAutocompleteField()).$().chain("trigger", "'focus'").render());
				scopeContext.append(new JsStatement().$(getAutocompleteField()).chain("val", "''").render());
				scopeContext.append(new JsStatement().$(getAutocompleteHidden()).chain("val", "''").render());
				scopeContext.append(new JsQuery(getAutocompleteField()).$().chain("trigger", "'blur'").render());
			}
		};
		return new JsStatement().$(cleanLink).chain("bind", "'click'", jsScope.render());
	}

}
