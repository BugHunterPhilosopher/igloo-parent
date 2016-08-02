package fr.openwide.core.wicket.more.link.util;

import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;

import fr.openwide.core.wicket.more.condition.Condition;
import fr.openwide.core.wicket.more.link.descriptor.AbstractDynamicBookmarkableLink;
import fr.openwide.core.wicket.more.link.descriptor.DynamicImage;
import fr.openwide.core.wicket.more.link.descriptor.LinkInvalidTargetRuntimeException;
import fr.openwide.core.wicket.more.link.descriptor.generator.IImageResourceLinkGenerator;
import fr.openwide.core.wicket.more.link.descriptor.generator.IPageLinkGenerator;
import fr.openwide.core.wicket.more.link.descriptor.parameter.injector.LinkParameterInjectionRuntimeException;
import fr.openwide.core.wicket.more.link.descriptor.parameter.validator.ConditionLinkParameterValidator;
import fr.openwide.core.wicket.more.link.descriptor.parameter.validator.LinkParameterValidationRuntimeException;
import fr.openwide.core.wicket.more.markup.html.template.model.NavigationMenuItem;
import fr.openwide.core.wicket.more.util.model.Models;

class InvalidLinkGenerator implements IPageLinkGenerator, IImageResourceLinkGenerator {
	private static final long serialVersionUID = 1L;
	
	private static LinkInvalidTargetRuntimeException invalidException() {
		return new LinkInvalidTargetRuntimeException("This link will always be invalid.");
	}

	@Override
	public void detach() {
		// Nothing to do
	}

	@Override
	public AbstractDynamicBookmarkableLink link(String wicketId) {
		return new AbstractDynamicBookmarkableLink(wicketId) {
			private static final long serialVersionUID = 1L;
			@Override
			protected boolean isValid() {
				return false;
			}
			@Override
			protected CharSequence getRelativeURL() throws LinkInvalidTargetRuntimeException, LinkParameterValidationRuntimeException {
				throw invalidException();
			}
			@Override
			protected CharSequence getAbsoluteURL() throws LinkInvalidTargetRuntimeException,
					LinkParameterValidationRuntimeException {
				throw invalidException();
			}
		};
	}

	@Override
	public String url() throws LinkInvalidTargetRuntimeException, LinkParameterInjectionRuntimeException,
			LinkParameterValidationRuntimeException {
		throw invalidException();
	}

	@Override
	public String url(RequestCycle requestCycle) throws LinkInvalidTargetRuntimeException,
			LinkParameterInjectionRuntimeException, LinkParameterValidationRuntimeException {
		throw invalidException();
	}

	@Override
	public String fullUrl() throws LinkInvalidTargetRuntimeException, LinkParameterInjectionRuntimeException,
			LinkParameterValidationRuntimeException {
		throw invalidException();
	}

	@Override
	public String fullUrl(RequestCycle requestCycle) throws LinkInvalidTargetRuntimeException,
			LinkParameterInjectionRuntimeException, LinkParameterValidationRuntimeException {
		throw invalidException();
	}

	@Override
	public InvalidLinkGenerator wrap(Component component) {
		return this;
	}
	
	@Override
	public boolean isAccessible() {
		return false;
	}

	@Override
	public AbstractDynamicBookmarkableLink link(String wicketId, String anchor) {
		return link(wicketId);
	}

	@Override
	public void setResponsePage() throws LinkInvalidTargetRuntimeException,
			LinkParameterValidationRuntimeException, LinkParameterInjectionRuntimeException {
		throw invalidException();
	}

	@Override
	public RestartResponseException newRestartResponseException() throws LinkInvalidTargetRuntimeException,
			LinkParameterValidationRuntimeException, LinkParameterInjectionRuntimeException {
		throw invalidException();
	}

	@Override
	public RestartResponseAtInterceptPageException newRestartResponseAtInterceptPageException()
			throws LinkInvalidTargetRuntimeException, LinkParameterValidationRuntimeException,
			LinkParameterInjectionRuntimeException {
		throw invalidException();
	}

	@Override
	public RedirectToUrlException newRedirectToUrlException() throws LinkInvalidTargetRuntimeException,
			LinkParameterValidationRuntimeException, LinkParameterInjectionRuntimeException {
		throw invalidException();
	}

	@Override
	public RedirectToUrlException newRedirectToUrlException(String anchor)
			throws LinkInvalidTargetRuntimeException, LinkParameterValidationRuntimeException,
			LinkParameterInjectionRuntimeException {
		throw invalidException();
	}

	@Override
	public NavigationMenuItem navigationMenuItem(IModel<String> labelModel)
			throws LinkInvalidTargetRuntimeException, LinkParameterValidationRuntimeException {
		throw invalidException();
	}

	@Override
	public NavigationMenuItem navigationMenuItem(IModel<String> labelModel,
			Collection<NavigationMenuItem> subMenuItems) throws LinkInvalidTargetRuntimeException,
			LinkParameterValidationRuntimeException {
		throw invalidException();
	}

	@Override
	public boolean isActive(Class<? extends Page> selectedPage) {
		return false;
	}

	@Override
	public PageProvider newPageProvider() throws LinkInvalidTargetRuntimeException,
			LinkParameterValidationRuntimeException {
		throw invalidException();
	}

	@Override
	public DynamicImage image(String wicketId) {
		return new DynamicImage(
				wicketId, Models.<ResourceReference>placeholder(),
				Models.<PageParameters>placeholder(),
				new ConditionLinkParameterValidator(Condition.alwaysFalse())
		);
	}
}