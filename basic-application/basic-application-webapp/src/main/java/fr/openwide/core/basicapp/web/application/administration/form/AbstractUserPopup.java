package fr.openwide.core.basicapp.web.application.administration.form;

import java.util.Collections;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicates;

import fr.openwide.core.basicapp.core.business.user.model.User;
import fr.openwide.core.basicapp.core.business.user.service.IUserService;
import fr.openwide.core.basicapp.core.util.binding.Bindings;
import fr.openwide.core.basicapp.web.application.BasicApplicationSession;
import fr.openwide.core.basicapp.web.application.administration.util.AdministrationTypeUser;
import fr.openwide.core.basicapp.web.application.common.validator.EmailUnicityValidator;
import fr.openwide.core.basicapp.web.application.common.validator.UsernamePatternValidator;
import fr.openwide.core.basicapp.web.application.common.validator.UsernameUnicityValidator;
import fr.openwide.core.wicket.more.condition.Condition;
import fr.openwide.core.wicket.more.markup.html.basic.EnclosureContainer;
import fr.openwide.core.wicket.more.markup.html.feedback.FeedbackUtils;
import fr.openwide.core.wicket.more.markup.html.form.FormPanelMode;
import fr.openwide.core.wicket.more.markup.html.form.LocaleDropDownChoice;
import fr.openwide.core.wicket.more.markup.html.template.js.jquery.plugins.bootstrap.modal.component.AbstractAjaxModalPopupPanel;
import fr.openwide.core.wicket.more.markup.html.template.js.jquery.plugins.bootstrap.modal.component.DelegatedMarkupPanel;
import fr.openwide.core.wicket.more.model.BindingModel;
import fr.openwide.core.wicket.more.model.GenericEntityModel;

public abstract class AbstractUserPopup<U extends User> extends AbstractAjaxModalPopupPanel<U> {

	private static final long serialVersionUID = -3575009149241618972L;

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractUserPopup.class);

	private static final UsernamePatternValidator USERNAME_PATTERN_VALIDATOR =
			new UsernamePatternValidator() {
				private static final long serialVersionUID = -6755079891321764827L;
				
				@Override
				protected ValidationError decorate(ValidationError error, IValidatable<String> validatable) {
					error.setKeys(Collections.singletonList("administration.user.form.userName.malformed"));
					return error;
				}
			};

	@SpringBean
	private IUserService userService;

	private final FormPanelMode mode;

	private final AdministrationTypeUser<U> type;

	protected Form<?> userForm;

	private final IModel<String> newPasswordModel = Model.of();

	private final IModel<String> confirmPasswordModel = Model.of();

	public AbstractUserPopup(String id, IModel<U> userModel, AdministrationTypeUser<U> type) {
		this(id, userModel, FormPanelMode.EDIT, type);
	}

	public AbstractUserPopup(String id, AdministrationTypeUser<U> type) {
		this(id, new GenericEntityModel<Long, U>(), FormPanelMode.ADD, type);
	}

	private AbstractUserPopup(String id, IModel<U> userModel, FormPanelMode mode, AdministrationTypeUser<U> type) {
		super(id, userModel);
		setStatic();
		
		this.mode = mode;
		this.type = type;
	}

	@Override
	protected Component createHeader(String wicketId) {
		if (isAddMode()) {
			return new Label(wicketId, new ResourceModel("administration.user.add.title"));
		} else {
			return new Label(wicketId, new StringResourceModel("administration.user.update.title", getModel()));
		}
	}

	protected final Component createStandardUserFields(String wicketId) {
		DelegatedMarkupPanel standardFields = new DelegatedMarkupPanel(wicketId, "standardFields", AbstractUserPopup.class);
		
		standardFields.add(
				new RequiredTextField<String>("firstName", BindingModel.of(getModel(), Bindings.user().firstName()))
						.setLabel(new ResourceModel("business.user.firstName")),
				new RequiredTextField<String>("lastName", BindingModel.of(getModel(), Bindings.user().lastName()))
						.setLabel(new ResourceModel("business.user.lastName")),
				new RequiredTextField<String>("userName", BindingModel.of(getModel(), Bindings.user().userName()))
						.setLabel(new ResourceModel("business.user.userName"))
						.add(USERNAME_PATTERN_VALIDATOR)
						.add(new UsernameUnicityValidator(getModel())),
				new EnclosureContainer("addContainer")
						.condition(Condition.predicate(Model.of(mode), Predicates.equalTo(FormPanelMode.ADD)))
						.add(
								new PasswordTextField("newPassword", newPasswordModel)
										.setLabel(new ResourceModel("business.user.password"))
										.setRequired(true),
								new PasswordTextField("confirmPassword", confirmPasswordModel)
										.setLabel(new ResourceModel("business.user.confirmPassword"))
										.setRequired(true),
								new CheckBox("active", BindingModel.of(getModel(), Bindings.user().active()))
										.setLabel(new ResourceModel("business.user.active"))
						),
				new EmailTextField("email", BindingModel.of(getModel(), Bindings.user().email()))
						.setLabel(new ResourceModel("business.user.email"))
						.add(EmailAddressValidator.getInstance())
						.add(new EmailUnicityValidator(getModel())),
				new LocaleDropDownChoice("locale", BindingModel.of(getModel(), Bindings.user().locale()))
						.setLabel(new ResourceModel("business.user.locale"))
		);
		
		return standardFields;
	}

	@Override
	protected Component createFooter(String wicketId) {
		DelegatedMarkupPanel footer = new DelegatedMarkupPanel(wicketId, AbstractUserPopup.class);
		
		// Validate button
		AjaxButton validate = new AjaxButton("save", userForm) {
			private static final long serialVersionUID = 1L;
			
			@Override
			protected void onAfterSubmit(AjaxRequestTarget target, Form<?> form) {
				User user = AbstractUserPopup.this.getModelObject();
				
				try {
					if (isAddMode()) {
						String newPasswordValue = newPasswordModel.getObject();
						String confirmPasswordValue = confirmPasswordModel.getObject();
						
						if (newPasswordValue != null && confirmPasswordValue != null) {
							if (confirmPasswordValue.equals(newPasswordValue)) {
								if (newPasswordValue.length() >= User.MIN_PASSWORD_LENGTH &&
										newPasswordValue.length() <= User.MAX_PASSWORD_LENGTH) {
									userService.create(user);
									userService.setPasswords(user, newPasswordValue);
									
									getSession().success(getString("administration.user.add.success"));
									throw type.fiche(AbstractUserPopup.this.getModel())
											.newRestartResponseException();
								} else {
									LOGGER.warn("Password does not fit criteria.");
									form.error(getString("administration.user.form.password.malformed"));
								}
							} else {
								LOGGER.warn("Password confirmation does not match.");
								form.error(getString("administration.user.form.password.wrongConfirmation"));
							}
						}
					} else {
						if (user.getLocale() != null) {
							BasicApplicationSession.get().setLocale(user.getLocale());
						}
						userService.update(user);
						getSession().success(getString("administration.user.update.success"));
						closePopup(target);
						target.add(getPage());
					}
				} catch (RestartResponseException e) { // NOSONAR
					throw e;
				} catch (Exception e) {
					if (isAddMode()) {
						LOGGER.error("Error occured while creating user", e);
					} else {
						LOGGER.error("Error occured while updating user", e);
					}
					Session.get().error(getString("common.error.unexpected"));
				}
				FeedbackUtils.refreshFeedback(target, getPage());
			}
			
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				FeedbackUtils.refreshFeedback(target, getPage());
			}
		};
		Label validateLabel;
		if (isAddMode()) {
			validateLabel = new Label("validateLabel", new ResourceModel("common.action.create"));
		} else {
			validateLabel = new Label("validateLabel", new ResourceModel("common.action.save"));
		}
		validate.add(validateLabel);
		footer.add(validate);
		
		// Cancel button
		AbstractLink cancel = new AbstractLink("cancel") {
			private static final long serialVersionUID = 1L;
		};
		addCancelBehavior(cancel);
		footer.add(cancel);
		
		return footer;
	}

	protected boolean isEditMode() {
		return FormPanelMode.EDIT.equals(mode);
	}

	protected boolean isAddMode() {
		return FormPanelMode.ADD.equals(mode);
	}

	@Override
	public IModel<String> getCssClassNamesModel() {
		return Model.of("modal-user");
	}

	@Override
	protected void onShow(AjaxRequestTarget target) {
		super.onShow(target);
		if (isAddMode()) {
			getModel().setObject(type.newInstance());
		}
	}
	
	@Override
	protected void onDetach() {
		super.onDetach();
		newPasswordModel.detach();
		confirmPasswordModel.detach();
	};
}
