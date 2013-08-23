package fr.openwide.core.jpa.business.generic.util;

import java.io.Serializable;
import java.util.Comparator;

import org.springframework.util.Assert;

import com.google.common.collect.Ordering;

import fr.openwide.core.commons.util.ordering.AbstractNullSafeComparator;
import fr.openwide.core.jpa.business.generic.model.GenericEntity;

/**
 * A {@link GenericEntity} comparator that compares IDs.
 * <p>The {@link #GenericEntityComparator() default implementation}, returned by {@link #get()}, mimics the behavior of {@link GenericEntity#compareTo(GenericEntity)},
 * except that it is null-safe, both regarding entities and regarding their IDs. It assumes a null entity is <em>more</em>
 * than a non-null one, and that an entity with a null ID is <em>more</em> than an entity with a non-null ID. This behavior is consistent with
 * {@link GenericEntity#isNew()}: an entity without an id is new, hence more recent than an entity with an id, which is not new.
 * <p>This comparator is consistent with equals.
 * <p><strong>WARNING:</strong> trying to compare two not-null entities with null IDs which are different according to
 * {@link #equalsNotNullObjects(GenericEntity, GenericEntity)} will result in an {@link IllegalArgumentException}
 * being thrown, since such objects are inherently incomparable. This behavior may be overridden in subclasses by
 * comparing other attributes of the entity.
 */
public class GenericEntityComparator<K extends Comparable<K> & Serializable, E extends GenericEntity<K,?>> extends AbstractNullSafeComparator<E> {

	private static final long serialVersionUID = -5933751018161012653L;
	
	@SuppressWarnings("rawtypes")
	private static final GenericEntityComparator DEFAULT_INSTANCE = new GenericEntityComparator();
	
	/**
	 * Named getGeneric() because naming it get() would mess with the get() static methods of potential subclasses, which would probably not be generic.
	 */
	@SuppressWarnings("unchecked")
	public static <K extends Comparable<K> & Serializable, E extends GenericEntity<K, ?>> Comparator<E> getGeneric() {
		return DEFAULT_INSTANCE;
	}
	
	private final Comparator<? super K> KEY_COMPARATOR;

	public GenericEntityComparator() {
		this(false, Ordering.natural().nullsLast());
	}

	/**
	 * @param nullIsLow Whether a null entity is lower than a non-null entity. This only applies to the entity object, not to its id.
	 * @param keyComparator The key comparator.
	 * 		Must be null-safe in order for this comparator to be null-safe.
	 * 		Must be serializable in order for this comparator to be serializable.
	 */
	public GenericEntityComparator(boolean nullIsLow, Comparator<? super K> keyComparator) {
		super(nullIsLow);
		Assert.notNull(keyComparator);
		this.KEY_COMPARATOR = keyComparator;
	}
	
	@Override
	protected boolean equalsNotNullObjects(E left, E right) {
		return left.equals(right);
	}

	/**
	 * @throws IllegalArgumentException if these entities may not be compared, i.e. they are different, but their respective IDs are null and there is no other way to compare them.
	 */
	@Override
	protected int compareNotNullObjects(E left, E right) {
		K leftId = left.getId();
		K rightId = right.getId();
		if (leftId == null && rightId == null) {
			throw new IllegalArgumentException("Cannot compare two different entities will non-null IDs");
		}
		return KEY_COMPARATOR.compare(leftId, rightId);
	}

}
