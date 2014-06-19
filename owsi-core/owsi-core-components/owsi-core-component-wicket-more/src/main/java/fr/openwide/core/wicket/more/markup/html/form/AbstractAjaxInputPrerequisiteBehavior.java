package fr.openwide.core.wicket.more.markup.html.form;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxRequestTarget.AbstractListener;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.StringValue;
import org.odlabs.wiquery.core.events.StateEvent;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Performs abstract actions on the attached component according to the actual, client-side content of a given {@link FormComponent}.<br>
 * This Behavior takes into account the fact that, in some cases, the model of a FormComponent may not reflect
 * the actual value entered by the user on the client side (especially when form validation fails).<br>
 * Upon the attached component's {@link #onConfigure(Component) configuration}, the Behavior perform one of two set of (abstract) actions
 * based on the current value for the <code>prerequisiteField</code>, which is the {@link {@link FormComponent#getConvertedInput() converted input}
 * if there was a user input, or the {@link FormComponent#getModelObject() model object} otherwise.<br>
 * <br>
 * This Behavior will automatically add an {@link AjaxEventBehavior} to the <code>prerequisiteField</code> to enable Ajax
 * updates on the attached component when the <code>prerequisiteField</code> {@link StateEvent#CHANGE changes}. You can override {@link #getAjaxTarget(Component)}
 * in order to specify which component must be added to the {@link AjaxRequestTarget target}
 * (this can be useful for example when the attached component is a Select2 drop-down choice).<br>
 * <br>
 * The <code>prerequisiteField</code> model object is guaranteed NOT to be updated upon client-side ajax calls.
 * If this is needed in your case, this Behavior is clearly overkill for you, since you do not need to inspect the <code>prerequisiteField</code>'s input.<br>
 * <br>
 * The sets of actions that will be performed upon the attached component's configuration are the following (each one can be overriden) :
 * <ul>
 * <li>{@link #setUpAttachedComponent(Component)} when {@link #shouldSetUpAttachedComponent()} is true and
 * {@link #isConvertedInputSatisfyingRequirements(FormComponent, Object)} or {@link #isCurrentModelSatisfyingRequirements(FormComponent, IModel)}
 * is true (which one is called depends on whether there was a user input or not).
 * <li>{@link #cleanDefaultModelObject(Component)} followed by {@link #takeDownAttachedComponent(Component)} otherwise
 * </ul>
 * 
 *
 * @param <T> The model object type of the <code>prerequisiteField</code>.
 */
public abstract class AbstractAjaxInputPrerequisiteBehavior<T> extends Behavior {

	private static final long serialVersionUID = 4689707482303046984L;

	private final FormComponent<T> prerequisiteField;
	
	private final Collection<Component> attachedComponents = Lists.newArrayList();
	
	private boolean useWicketValidation = false;
	
	private boolean updatePrerequisiteModel = false;
	
	private boolean resetAttachedModel = false;
	
	private boolean refreshParent = false;
	
	private final Collection<AbstractListener> onChangeListeners = Lists.newArrayList();
	
	private Predicate<? super T> objectValidPredicate = Predicates.notNull();

	public AbstractAjaxInputPrerequisiteBehavior(FormComponent<T> prerequisiteField) {
		super();
		Args.notNull(prerequisiteField, "prerequisiteField");
		this.prerequisiteField = prerequisiteField;
	}

	/**
	 * Sets whether wicket validation ({@link FormComponent#validate()} method) is to be taken into account before
	 * trying to apply the {@code #setObjectValidPredicate(Predicate) objectValidPredicate}.
	 * <p><strong>WARNING:</strong> The wicket validation relies solely on input, and not on the model object. Thus any wicket validation
	 * will systematically deem a field invalid when there is no input. In particular, this means that the prerequisite field will systematically be deemed invalid on
	 * the first page rendering, which means this feature is not suitable for "editing" forms, where the form is filled before the first rendering.
	 */
	public void setUseWicketValidation(boolean validatePrerequisiteInput) {
		this.useWicketValidation = validatePrerequisiteInput;
	}
	
	/**
	 * Sets whether the prerequisite field model is to be updated when the prerequisite field input changes.
	 */
	public AbstractAjaxInputPrerequisiteBehavior<T> setUpdatePrerequisiteModel(boolean updatePrerequisiteModel) {
		this.updatePrerequisiteModel = updatePrerequisiteModel;
		return this;
	}
	
	/**
	 * Sets whether the attached component's models are to be set to null when the prerequisite model changes.
	 */
	public AbstractAjaxInputPrerequisiteBehavior<T> setResetAttachedModel(boolean resetAttachedModel) {
		this.resetAttachedModel = resetAttachedModel;
		return this;
	}
	
	/**
	 * Sets the predicate used to determine whether the prerequisite field value is valid.
	 * <strong>Note:</strong> the predicate must be {@link Serializable}.
	 */
	public AbstractAjaxInputPrerequisiteBehavior<T> setObjectValidPredicate(Predicate<? super T> objectValidPredicate) {
		this.objectValidPredicate = objectValidPredicate;
		return this;
	}
	
	/**
	 * Sets whether the ajax refresh should target the attached component's parent instead of the component itself.
	 * <p>This is useful for components that use javascript to generate siblings in the DOM tree, such as Select2.
	 */
	public AbstractAjaxInputPrerequisiteBehavior<T> setRefreshParent(boolean refreshParent) {
		this.refreshParent = refreshParent;
		return this;
	}
	
	public AbstractAjaxInputPrerequisiteBehavior<T> onChange(AbstractListener listener) {
		this.onChangeListeners.add(listener);
		return this;
	}
	
	private static class InputPrerequisiteAjaxEventBehavior extends AjaxEventBehavior {
		private static final long serialVersionUID = -2099510409333557398L;
		
		private final Collection<AbstractAjaxInputPrerequisiteBehavior<?>> listeners = Sets.newHashSet();
		
		public InputPrerequisiteAjaxEventBehavior() {
			super(StateEvent.CHANGE.getEventLabel());
		}
		
		public void register(AbstractAjaxInputPrerequisiteBehavior<?> inputPrerequisiteBehavior) {
			listeners.add(inputPrerequisiteBehavior);
		}
		
		public void unregister(AbstractAjaxInputPrerequisiteBehavior<?> inputPrerequisiteBehavior) {
			listeners.remove(inputPrerequisiteBehavior);
		}
		
		@Override
		public boolean isEnabled(Component component) {
			return super.isEnabled(component) && !listeners.isEmpty();
		}
		
		@Override
		protected void onEvent(AjaxRequestTarget target) {
			notify(target);
		}
		
		public void notify(AjaxRequestTarget target) {
			for (AbstractAjaxInputPrerequisiteBehavior<?> listener : listeners) {
				listener.prerequisiteFieldChange(target);
			}
		}
	}
	
	private InputPrerequisiteAjaxEventBehavior getExistingAjaxEventBehavior() {
		return getExistingAjaxEventBehavior(prerequisiteField);
	}
	
	private InputPrerequisiteAjaxEventBehavior getExistingAjaxEventBehavior(Component component) {
		Collection<InputPrerequisiteAjaxEventBehavior> ajaxEventBehaviors = component.getBehaviors(InputPrerequisiteAjaxEventBehavior.class);
		if (ajaxEventBehaviors.isEmpty()) {
			return null;
		} else if (ajaxEventBehaviors.size() > 1) {
			throw new IllegalStateException("There should not be more than ONE InputPrerequisiteAjaxEventBehavior attached to " + component);
		} else {
			return ajaxEventBehaviors.iterator().next();
		}
	}
	
	@Override
	public final void bind(Component component) {
		if (attachedComponents.isEmpty()) {
			// Make sure that the attached component will be updated when the content of the prerequisiteField changes.
			// The following makes sure that only one ajaxEventBehavior will be attached to the prerequisiteField
			// even if multiple components are attached to different InputPrerequisiteBehavior pointing to the same prerequisiteField.
			InputPrerequisiteAjaxEventBehavior ajaxEventBehavior = getExistingAjaxEventBehavior();
			if (ajaxEventBehavior == null) {
				ajaxEventBehavior = new InputPrerequisiteAjaxEventBehavior();
				prerequisiteField.add(ajaxEventBehavior);
			}
			ajaxEventBehavior.register(this);
		}
		
		attachedComponents.add(component);
	}
	
	@Override
	public final void unbind(Component component) {
		attachedComponents.remove(component);
		
		if (attachedComponents.isEmpty()) {
			InputPrerequisiteAjaxEventBehavior ajaxEventBehavior = getExistingAjaxEventBehavior();
			
			// We no longer need ajax events anymore
			if (ajaxEventBehavior != null) {
				ajaxEventBehavior.unregister(this);
			}
		}
	}
	
	private void prerequisiteFieldChange(AjaxRequestTarget target) {
		for (Component attachedComponent : attachedComponents) {
			target.add(getAjaxTarget(attachedComponent));
			if (resetAttachedModel) {
				attachedComponent.setDefaultModelObject(null);
				// Handle chained prerequisites
				InputPrerequisiteAjaxEventBehavior behavior = getExistingAjaxEventBehavior(attachedComponent);
				if (behavior != null) {
					behavior.notify(target);
				}
			}
		}
		for (AbstractListener onChangeListener : onChangeListeners) {
			target.addListener(onChangeListener);
		}
		onPrerequisiteFieldChange(target, prerequisiteField, Collections.unmodifiableCollection(attachedComponents));
	}
	
	@Override
	public void detach(Component component) {
		super.detach(component);
	}
	
	private Component getAjaxTarget(Component componentToRender) {
		if (refreshParent) {
			return componentToRender.getParent();
		} else {
			return componentToRender;
		}
	}
	
	/**
	 * Ajax call triggered by a change on the prerequisite field.
	 * Called after adding the attached components to the target, and before generating the response.
	 * @deprecated Use {@link #onChange(AbstractListener)} instead.
	 */
	@Deprecated
	protected void onPrerequisiteFieldChange(AjaxRequestTarget target, FormComponent<T> prerequisiteField, Collection<Component> attachedComponents) {
		
	}
	
	private static boolean hasInputChanged(FormComponent<?> formComponent) {
		List<StringValue> input = formComponent.getRequest().getRequestParameters().getParameterValues(formComponent.getInputName());
		return input != null && !input.isEmpty();
	}

	@Override
	public final void onConfigure(Component component) {
		super.onConfigure(component);
		
		if (prerequisiteField.determineVisibility()) {
			if (shouldSetUpAttachedComponent()) {
				if (hasInputChanged(prerequisiteField)) {
					// The prerequisiteField input has changed : the rendering of the attached component was triggered either by our
					// InputPrerequisiteAjaxEventBehavior or by a form submit.
					// We will decide whether the attached component should be set up or taken down based on the prerequisiteField's input.
					prerequisiteField.inputChanged();
					prerequisiteField.validate(); // Performs input conversion
					updateModelIfNecessary(prerequisiteField);
					
					if (isConvertedInputSatisfyingRequirements(prerequisiteField, prerequisiteField.getConvertedInput())) {
						setUpAttachedComponent(component);
					} else {
						cleanDefaultModelObject(component);
						takeDownAttachedComponent(component);
					}
					
					// Clearing the input seems useless here, and may harm if the input is used when rendering the form component.
	//				prerequisiteField.clearInput();
				} else {
					prerequisiteField.validate(); // Performs input conversion
					
					if (isCurrentModelSatisfyingRequirements(prerequisiteField, prerequisiteField.getModel())) {
						setUpAttachedComponent(component);
					} else {
						cleanDefaultModelObject(component);
						takeDownAttachedComponent(component);
					}
				}
				
				// We need to clear the message that may have been added during the validation, since they are not relevant to the user (no form was submitted)
				prerequisiteField.getFeedbackMessages().clear();
			} else {
				cleanDefaultModelObject(component);
				takeDownAttachedComponent(component);
			}
		}
	}

	private void updateModelIfNecessary(FormComponent<T> prerequisiteField) {
		if (updatePrerequisiteModel) {
			prerequisiteField.updateModel();
		}
	}
	
	/** Allows to restrict whether the attached component should be set up or not based on external, <code>prerequisiteField</code>-independent conditions.
	 * @return True if we can proceed inspecting the <code>prerequisiteField</code> value.
	 *         False if the attached component should be taken down regardless of the <code>prerequisiteField</code> status.
	 */
	protected boolean shouldSetUpAttachedComponent() {
		return true;
	}
	
	protected final boolean isConvertedInputSatisfyingRequirements(FormComponent<T> prerequisiteField, T convertedInput) {
		return (!useWicketValidation || prerequisiteField.isValid()) && isObjectValid(convertedInput);
	}
	
	protected final boolean isCurrentModelSatisfyingRequirements(FormComponent<T> prerequisiteField, IModel<T> currentModel) {
		return (!useWicketValidation || prerequisiteField.isValid()) && isObjectValid(currentModel.getObject());
	}
	
	protected final boolean isObjectValid(T object) {
		return objectValidPredicate.apply(object);
	}

	protected void cleanDefaultModelObject(Component attachedComponent) {
		IModel<?> model = attachedComponent.getDefaultModel();
		// It is not necessary to set the model object to null if it already is.
		// Furthermore, in case of a PropertyModel it can cause a WicketRuntimeException if its innermost object is null.
		if (model != null && model.getObject() != null) {
			model.setObject(null);
		}
	}

	protected abstract void setUpAttachedComponent(Component attachedComponent);

	protected abstract void takeDownAttachedComponent(Component attachedComponent);
}