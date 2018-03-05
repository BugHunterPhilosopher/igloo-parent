package org.iglooproject.basicapp.web.application.administration.component;

import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.iglooproject.basicapp.core.business.user.model.User;
import org.iglooproject.basicapp.web.application.administration.form.UserAjaxDropDownSingleChoice;
import org.iglooproject.basicapp.web.application.administration.form.UserGroupDropDownSingleChoice;
import org.iglooproject.basicapp.web.application.administration.model.AbstractUserDataProvider;
import org.iglooproject.basicapp.web.application.administration.template.AdministrationUserDetailTemplate;
import org.iglooproject.basicapp.web.application.common.typedescriptor.user.UserTypeDescriptor;
import org.iglooproject.wicket.markup.html.form.PageableSearchForm;
import org.iglooproject.wicket.more.ajax.SerializableListener;
import org.iglooproject.wicket.more.common.behavior.UpdateOnChangeAjaxEventBehavior;
import org.iglooproject.wicket.more.link.descriptor.IPageLinkDescriptor;
import org.iglooproject.wicket.more.link.model.ComponentPageModel;
import org.iglooproject.wicket.more.markup.html.form.LabelPlaceholderBehavior;
import org.iglooproject.wicket.more.model.GenericEntityModel;

public class UserListSearchPanel<U extends User> extends Panel {
	
	private static final long serialVersionUID = -6224313886789870489L;
	
	public UserListSearchPanel(String id, IPageable pageable, UserTypeDescriptor<U> typeDescriptor, AbstractUserDataProvider<U> dataProvider) {
		super(id);
		
		IModel<U> quickAccessModel = new GenericEntityModel<Long, U>();
		
		add(
				new PageableSearchForm<Void>("form", pageable)
						.add(
								new TextField<String>("name", dataProvider.getNameModel())
										.setLabel(new ResourceModel("business.user.name"))
										.add(new LabelPlaceholderBehavior()),
								new UserGroupDropDownSingleChoice("userGroup", dataProvider.getGroupModel())
										.setLabel(new ResourceModel("business.user.group"))
										.add(new LabelPlaceholderBehavior()),
								new CheckBox("active", dataProvider.getIncludeInactivesModel())
										.setLabel(new ResourceModel("administration.user.list.search.includeInactives"))
										.setOutputMarkupId(true),
								new UserAjaxDropDownSingleChoice<>("quickAccess", quickAccessModel, typeDescriptor.getEntityClass())
										.setLabel(new ResourceModel("common.quickAccess"))
										.add(new LabelPlaceholderBehavior())
										.add(
												new UpdateOnChangeAjaxEventBehavior()
														.onChange(new SerializableListener() {
															private static final long serialVersionUID = 1L;
															@Override
															public void onBeforeRespond(Map<String, Component> map, AjaxRequestTarget target) {
																IPageLinkDescriptor linkDescriptor = AdministrationUserDetailTemplate.<U>mapper()
																		.setParameter2(new ComponentPageModel(UserListSearchPanel.this))
																		.map(new GenericEntityModel<>(quickAccessModel.getObject()));
																
																quickAccessModel.setObject(null);
																quickAccessModel.detach();
																
																if (linkDescriptor.isAccessible()) {
																	throw linkDescriptor.newRestartResponseException();
																}
															}
														})
										)
						)
		);
	}

}
