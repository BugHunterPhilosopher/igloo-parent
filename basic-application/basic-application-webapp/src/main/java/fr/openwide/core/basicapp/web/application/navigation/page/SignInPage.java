package fr.openwide.core.basicapp.web.application.navigation.page;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import fr.openwide.core.basicapp.core.business.parameter.service.IParameterService;
import fr.openwide.core.basicapp.core.config.application.BasicApplicationConfigurer;
import fr.openwide.core.basicapp.web.application.common.template.ServiceTemplate;
import fr.openwide.core.basicapp.web.application.common.template.styles.ServiceLessCssResourceReference;
import fr.openwide.core.jpa.security.service.IAuthenticationService;
import fr.openwide.core.wicket.more.AbstractCoreSession;
import fr.openwide.core.wicket.more.application.CoreWicketAuthenticatedApplication;
import fr.openwide.core.wicket.more.markup.html.form.LabelPlaceholderBehavior;
import fr.openwide.core.wicket.more.markup.html.template.model.BreadCrumbElement;
import fr.openwide.core.wicket.more.security.page.LoginSuccessPage;

public class SignInPage extends ServiceTemplate {

	private static final long serialVersionUID = 5503959273448832421L;

	private static final Logger LOGGER = LoggerFactory.getLogger(SignInPage.class);
	
	@SpringBean
	private BasicApplicationConfigurer configurer;

	@SpringBean
	private IParameterService parameterService;
	
	@SpringBean
	private IAuthenticationService authenticationService;
	
	private FormComponent<String> userNameField;
	
	private FormComponent<String> passwordField;
	
	public SignInPage(PageParameters parameters) {
		super(parameters);
		
		addHeadPageTitlePrependedElement(new BreadCrumbElement(new ResourceModel("signIn.pageTitle")));
		
		Form<Void> signInForm = new Form<Void>("signInForm") {
			private static final long serialVersionUID = 1L;
			
			@Override
			protected void onSubmit() {
				AbstractCoreSession<?> session = AbstractCoreSession.get();
				boolean success = false;
				try {
					session.signIn(userNameField.getModelObject(), passwordField.getModelObject());
					success = true;
				} catch (BadCredentialsException e) {
					session.error(getString("signIn.error.authentication"));
				} catch (UsernameNotFoundException e) {
					session.error(getString("signIn.error.authentication"));
				} catch (DisabledException e) {
					session.error(getString("signIn.error.userDisabled"));
				} catch (Exception e) {
					LOGGER.error("Erreur inconnue lors de l'authentification de l'utilisateur", e);
					session.error(getString("signIn.error.unknown"));
				}
				
				if (success) {
					throw LoginSuccessPage.linkDescriptor().newRestartResponseException();
				} else {
					throw CoreWicketAuthenticatedApplication.get().getSignInPageLinkDescriptor()
							.newRestartResponseException();
				}
			}
		};
		add(signInForm);
		
		userNameField = new RequiredTextField<String>("userName", Model.of(""));
		userNameField.setLabel(new ResourceModel("signIn.userName"));
		userNameField.add(new LabelPlaceholderBehavior());
		userNameField.setOutputMarkupId(true);
		signInForm.add(userNameField);
		
		passwordField = new PasswordTextField("password", Model.of("")).setRequired(true);
		passwordField.setLabel(new ResourceModel("signIn.password"));
		passwordField.add(new LabelPlaceholderBehavior());
		signInForm.add(passwordField);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(Application.get().getJavaScriptLibrarySettings().getJQueryReference()));
		response.render(CssHeaderItem.forReference(ServiceLessCssResourceReference.get()));
	}

	@Override
	protected IModel<String> getTitleModel() {
		return new ResourceModel("signIn.welcomeText");
	}

}
