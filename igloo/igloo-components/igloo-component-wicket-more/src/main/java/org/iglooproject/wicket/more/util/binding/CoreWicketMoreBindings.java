package org.iglooproject.wicket.more.util.binding;

import org.bindgen.java.util.ListBinding;
import org.iglooproject.commons.util.mime.MediaTypeBinding;
import org.iglooproject.jpa.security.business.person.model.GenericUserBinding;
import org.iglooproject.wicket.more.console.maintenance.ehcache.model.EhCacheCacheInformationBinding;
import org.iglooproject.wicket.more.model.IBindableDataProviderBinding;

public final class CoreWicketMoreBindings {

	@SuppressWarnings("rawtypes")
	public static final GenericUserBinding GENERIC_USER = new GenericUserBinding<>();

	private static final EhCacheCacheInformationBinding EH_CACHE_CACHE_INFORMATION = new EhCacheCacheInformationBinding();

	private static final IBindableDataProviderBinding IBINDABLE_DATA_PROVIDER = new IBindableDataProviderBinding();

	private static final ListBinding<?> LIST = new ListBinding<Void>();

	private static final MediaTypeBinding MEDIA_TYPE = new MediaTypeBinding();

	public static EhCacheCacheInformationBinding ehCacheCacheInformation() {
		return EH_CACHE_CACHE_INFORMATION;
	}

	public static IBindableDataProviderBinding iBindableDataProvider() {
		return IBINDABLE_DATA_PROVIDER;
	}

	public static ListBinding<?> list() {
		return LIST;
	}

	public static MediaTypeBinding mediaType() {
		return MEDIA_TYPE;
	}

	private CoreWicketMoreBindings() {
	}

}
