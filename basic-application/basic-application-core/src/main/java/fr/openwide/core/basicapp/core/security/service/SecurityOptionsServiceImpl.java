package fr.openwide.core.basicapp.core.security.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import com.google.common.collect.Maps;

import fr.openwide.core.basicapp.core.business.user.model.User;
import fr.openwide.core.basicapp.core.security.model.SecurityOptions;
import fr.openwide.core.jpa.security.business.person.model.GenericUser;

public class SecurityOptionsServiceImpl implements ISecurityOptionsService {

	private static Map<Class<? extends GenericUser<?, ?>>, SecurityOptions> OPTIONS_BY_USER = Maps.newHashMap();

	private static SecurityOptions DEFAULT_OPTIONS = SecurityOptions.defaultOptions();

	@Override
	public SecurityOptionsServiceImpl setOptions(Class<? extends User> clazz, SecurityOptions options) {
		checkNotNull(clazz);
		checkNotNull(options);
		
		OPTIONS_BY_USER.put(clazz, options);
		
		return this;
	}

	@Override
	public SecurityOptionsServiceImpl setDefaultOptions(SecurityOptions options) {
		checkNotNull(options);
		
		DEFAULT_OPTIONS = options;
		
		return this;
	}

	@Override
	public SecurityOptions getOptions(Class<? extends User> clazz) {
		if (OPTIONS_BY_USER.containsKey(clazz)) {
			return OPTIONS_BY_USER.get(clazz);
		}
		return DEFAULT_OPTIONS;
	}

}
