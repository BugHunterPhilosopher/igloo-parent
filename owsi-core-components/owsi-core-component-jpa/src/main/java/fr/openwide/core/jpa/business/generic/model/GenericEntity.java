/*
 * Copyright (C) 2009-2010 Open Wide
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

package fr.openwide.core.jpa.business.generic.model;

import java.io.Serializable;
import java.text.Collator;
import java.util.Locale;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.Hibernate;

/**
 * <p>Entité racine pour la persistence des objets via JPA.</p>
 *
 * @author Open Wide
 *
 * @param <E> type de l'entité
 */
public abstract class GenericEntity<K extends Serializable & Comparable<K>, E extends GenericEntity<K, ?>>
		implements Serializable, Comparable<E> {

	private static final long serialVersionUID = -3988499137919577054L;
	
	public static final Collator DEFAULT_STRING_COLLATOR = Collator.getInstance(Locale.FRENCH);
	
	static {
		DEFAULT_STRING_COLLATOR.setStrength(Collator.PRIMARY);
	}
	
	/**
	 * Retourne la valeur de l'identifiant unique.
	 * 
	 * @return id
	 */
	public abstract K getId();

	/**
	 * Définit la valeur de l'identifiant unique.
	 * 
	 * @param id id
	 */
	public abstract void setId(K id);
	
	/**
	 * Indique si l'objet a déjà été persisté ou non
	 * 
	 * @return vrai si l'objet n'a pas encore été persisté
	 */
	@JsonIgnore
	public boolean isNew() {
		return getId() == null;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}
		if (object == this) {
			return true;
		}
		
		// l'objet peut être proxyfié donc on utilise Hibernate.getClass() pour sortir la vraie classe
		if (Hibernate.getClass(object) != Hibernate.getClass(this)) {
			return false;
		}

		GenericEntity<K, E> entity = (GenericEntity<K, E>) object;
		K id = getId();

		if (id == null) {
			return false;
		}

		return id.equals(entity.getId());
	}

	@Override
	public int hashCode() {
		int hash = 7;
		
		K id = getId();
		hash = 31 * hash + ((id == null) ? 0 : id.hashCode());

		return hash;
	}

	@Override
	public int compareTo(E o) {
		if (this == o) {
			return 0;
		}
		return this.getId().compareTo(o.getId());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("entity.");
		builder.append(Hibernate.getClass(this).getSimpleName());
		builder.append("<");
		builder.append(getId());
		builder.append("-");
		builder.append(getNameForToString());
		builder.append(">");
		
		return builder.toString();
	}
	
	/**
	 * Retourne l'élément de chaîne qui va être injecté dans le toString() de l'objet : faire en sorte que cela permette
	 * de l'identifier.
	 *  
	 * @return chaîne à injecter dans le toString()
	 */
	@JsonIgnore
	public abstract String getNameForToString();

	/**
	 * Retourne le nom à afficher.
	 * 
	 * @return nom à afficher
	 */
	@JsonIgnore
	public abstract String getDisplayName();

}
