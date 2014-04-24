/*
 * Copyright (C) 2009-2011 Open Wide
 * Contact: contact@openwide.fr
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.openwide.core.jpa.more.business.generic.dao;

import java.util.Comparator;
import java.util.List;

import javax.persistence.NonUniqueResultException;
import javax.persistence.metamodel.SingularAttribute;

import fr.openwide.core.jpa.more.business.generic.model.EnabledFilter;
import fr.openwide.core.jpa.more.business.generic.model.GenericListItem;

/**
 * Note : les définitions de generics n'ont pas été choisies par hasard. Elles permettent
 * de répondre à des besoins concrets sur des projets.
 */
public interface IGenericListItemDao {

	<E extends GenericListItem<?>> E getEntity(Class<E> clazz, Long id);

	<E extends GenericListItem<?>> E getById(Class<E> clazz, Long id);
	
	<E extends GenericListItem<?>> E getByNaturalId(Class<E> clazz, String naturalId);

	<E extends GenericListItem<?>> void update(E entity);

	<E extends GenericListItem<?>> void save(E entity);

	<E extends GenericListItem<?>> void delete(E entity);

	<E extends GenericListItem<?>> E refresh(E entity);

	<E extends GenericListItem<?>> List<E> list(Class<E> clazz, EnabledFilter enabledFilter, Comparator<? super E> comparator);

	<E extends GenericListItem<?>> List<E> list(Class<E> clazz, EnabledFilter enabledFilter);

	<E extends GenericListItem<?>> List<E> list(Class<E> clazz);

	<E extends GenericListItem<?>, V> List<E> listByField(Class<E> clazz, SingularAttribute<? super E, V> field, V fieldValue,
			EnabledFilter enabledFilter, Comparator<? super E> comparator);

	<E extends GenericListItem<?>, V> List<E> listByField(Class<E> clazz, SingularAttribute<? super E, V> field, V fieldValue,
			EnabledFilter enabledFilter);

	<E extends GenericListItem<?>, V> List<E> listByField(Class<E> clazz, SingularAttribute<? super E, V> field, V fieldValue);

	<E extends GenericListItem<?>> Long count(Class<E> clazz, EnabledFilter enabledFilter);

	<E extends GenericListItem<?>> Long count(Class<E> clazz);

	<E extends GenericListItem<?>, V> Long countByField(Class<E> clazz, SingularAttribute<? super E, V> attribute, V fieldValue,
			EnabledFilter enabledFilter);

	<E extends GenericListItem<?>, V> Long countByField(Class<E> clazz, SingularAttribute<? super E, V> field, V fieldValue);

	/**
	 * Obtient un objet par la condition attribut = valeur
	 * 
	 * @param <V>
	 * @param clazz
	 * @param attribute
	 * @param fieldValue
	 * @return
	 * @throws NonUniqueResultException
	 */
	<E extends GenericListItem<?>, V>  E getByField(Class<E> clazz, SingularAttribute<? super E, V> attribute, V fieldValue);
	
	/**
	 * Obtient un objet par la condition lower(attribut) = lower(valeur).
	 * 
	 * @param clazz
	 * @param attribute
	 * @param fieldValue
	 * @return
	 * @throws NonUniqueResultException
	 */
	<E extends GenericListItem<?>>  E getByFieldIgnoreCase(Class<E> clazz, SingularAttribute<? super E, String> attribute, String fieldValue);

}
