package org.iglooproject.basicapp.web.application.console.notification.demo.page;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.iglooproject.basicapp.core.business.user.model.User;
import org.iglooproject.basicapp.web.application.BasicApplicationSession;
import org.iglooproject.basicapp.web.application.administration.form.UserAjaxDropDownSingleChoice;
import org.iglooproject.basicapp.web.application.console.notification.demo.template.ConsoleNotificationDemoTemplate;
import org.iglooproject.spring.notification.exception.NotificationContentRenderingException;
import org.iglooproject.spring.notification.model.INotificationContentDescriptor;
import org.iglooproject.wicket.markup.html.basic.CoreLabel;
import org.iglooproject.wicket.more.ajax.AjaxListeners;
import org.iglooproject.wicket.more.common.behavior.UpdateOnChangeAjaxEventBehavior;
import org.iglooproject.wicket.more.model.GenericEntityModel;
import org.iglooproject.wicket.more.util.model.Detachables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

public class ConsoleNotificationDemoPage extends ConsoleNotificationDemoTemplate {
	
	private static final long serialVersionUID = -1929048481327622623L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleNotificationDemoPage.class);
	
	private final IModel<INotificationContentDescriptor> descriptorModel;
	
	private final IModel<User> recipientModel;
	private final IModel<INotificationContentDescriptor> descriptorWithContextModel;
	private final IModel<String> subjectModel;
	private final IModel<String> bodyModel;
	
	public ConsoleNotificationDemoPage(PageParameters parameters, final IModel<INotificationContentDescriptor> descriptorModel) throws NotificationContentRenderingException {
		super(parameters);
		
		this.descriptorModel = descriptorModel;
		
		recipientModel = new GenericEntityModel<>(BasicApplicationSession.get().getUser());
		
		descriptorWithContextModel =
				new AbstractReadOnlyModel<INotificationContentDescriptor>() {
					private static final long serialVersionUID = 1L;
					@Override
					public INotificationContentDescriptor getObject() {
						return descriptorModel.getObject().withContext(recipientModel.getObject());
					}
				};
		
		subjectModel = new AbstractReadOnlyModel<String>() {
			private static final long serialVersionUID = 1L;
			@Override
			public String getObject() {
				try {
					return descriptorWithContextModel.getObject().renderSubject();
				} catch (NotificationContentRenderingException|RuntimeException e) {
					LOGGER.error("Erreur lors du rendu du sujet", e);
					return Throwables.getStackTraceAsString(e);
				}
			}
		};
		
		bodyModel = new AbstractReadOnlyModel<String>() {
			private static final long serialVersionUID = 1L;
			@Override
			public String getObject() {
				try {
					return descriptorWithContextModel.getObject().renderHtmlBody();
				} catch (NotificationContentRenderingException|RuntimeException e) {
					LOGGER.error("Erreur lors du rendu du corps", e);
					return "<pre>" + Throwables.getStackTraceAsString(e) + "</pre>";
				}
			}
		};
		
		Form<?> form = new Form<>("form");
		add(form);
		
		form.add(
				new UserAjaxDropDownSingleChoice<>("recipient", recipientModel, User.class)
						.setLabel(new ResourceModel("console.notifications.demo.recipient"))
						.setRequired(true)
						.add(
								new UpdateOnChangeAjaxEventBehavior()
										.onChange(AjaxListeners.refreshPage())
						),
				
				new CoreLabel("subject", subjectModel),
				
				new CoreLabel("body", bodyModel)
						.setEscapeModelStrings(false)
		);
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		Detachables.detach(descriptorModel, recipientModel, descriptorWithContextModel, subjectModel, bodyModel);
	}

	@Override
	protected Class<? extends WebPage> getSecondMenuPage() {
		return ConsoleNotificationDemoPage.class;
	}

}
