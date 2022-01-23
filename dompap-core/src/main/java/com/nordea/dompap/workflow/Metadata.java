package com.nordea.dompap.workflow;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

public class Metadata implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int PROPERTY_VALUE_MAX_LEN = 200;

    Multimap<PropertyType, String> metadata;
    // Only for storing named properties until stored.
    private transient Multimap<String, String> namedMetadata;
    private boolean isDirty = false;

    public Metadata(Multimap<PropertyType, String> metadata) {
        this.metadata = metadata != null ? metadata : HashMultimap.create();
    }

    public Metadata() {
        this.metadata = HashMultimap.create();
    }

    /**
     * @deprecated
     * Use MetadataService.getOrCreateProperty
     */
    @Deprecated
    public void putProperty(String type, String value) throws ResourceException {
        throw new NotSupportedException("Use MetadataService.getOrCreateProperty");
/*
		try {
			PropertyType propertyType = ServiceFactory.getService(MetadataService.class).getOrCreatePropertyType(type, "");
	        putProperty(propertyType, value);
		} catch (ServiceException e) {
			throw new ResourceException(e);
		}
 */
    }

    public void putProperty(PropertyType type, String value) {
        // Ignore null value
        if (value==null) {
            return;
        }
        if (value.length() > PROPERTY_VALUE_MAX_LEN) {
            throw new IllegalArgumentException("Property value can not be longer than " + PROPERTY_VALUE_MAX_LEN
                    + " characters (" + value.length() + "): " + type.getName() + "=[" + value + "]");
        }
        isDirty = true;
        metadata.put(type, value);
    }

    public Collection<Entry<PropertyType, String>> getEntries() {
        // TODO Ensure that entries are immutable.
        return new ArrayList<>(metadata.entries());
    }

    public List<Entry<String, String>> getNamedEntries() {
        List<Entry<String, String>> entries = new ArrayList<>();
        for (final Entry<PropertyType, String> entry : metadata.entries()) {
            entries.add(new PropertyTypeValueEntry(entry.getKey(), entry.getValue()));
        }
        return entries;
    }

    private static final class PropertyTypeValueEntry implements Map.Entry<String, String> {
        private final String key;
        private final String value;

        PropertyTypeValueEntry(PropertyType propertyType, String value) {
            this.key = propertyType.getName();
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String setValue(String value) {
            throw new IllegalStateException("Modifying metadata is only possible using Metadata.putProperty");
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }
    }

    public Set<PropertyType> getPropertyTypes() {
        return ImmutableSet.copyOf(metadata.keySet());
    }

    public Set<String> getPropertyNames() {
        Set<String> propNames = new HashSet<>();
        for (PropertyType type : metadata.keySet()) {
            propNames.add(type.getName());
        }
        return propNames;
    }

    public Collection<String> getProperties(PropertyType type) {
        if (namedMetadata == null) {
            namedMetadata = toNamedProperties(metadata);
        }
        return ImmutableList.copyOf(metadata.get(type));
    }

    public Collection<String> getProperties(String propertyName) {
        if (namedMetadata == null) {
            namedMetadata = toNamedProperties(metadata);
        }
        return ImmutableList.copyOf(namedMetadata.get(propertyName));
    }

    private Multimap<String, String> toNamedProperties(Multimap<PropertyType, String> namedProperties) {
        Multimap<String, String> map = HashMultimap.create();
        for (Entry<PropertyType, String> entry : namedProperties.entries()) {
            PropertyType propertyType = entry.getKey();
            map.put(propertyType.getName(), entry.getValue());
        }
        return map;
    }

    public String getFirstProperty(String propertyName) {
        Collection<String> props = getProperties(propertyName);
        if (props == null) {
            return null;
        } else {
            Object[] array = props.toArray();
            if (array.length == 0) {
                return null;
            } else {
                return (String) props.toArray()[0];
            }
        }
    }

    /**
     * @deprecated Use MetadataService.getOrCreatePropertyType to lookup PropertyType then call removeAll
     */
    @Deprecated
    public Collection<String> removeAll(String type) throws ResourceException {
        throw new NotSupportedException("Use MetadataService.getOrCreatePropertyType to lookup PropertyType then call removeAll");
/*
    	try {
	        PropertyType propertyType = ServiceFactory.getService(MetadataService.class).getOrCreatePropertyType(type, "");
	        return removeAll(propertyType);
    	} catch (ServiceException e) {
    		throw new ResourceException(e);
    	}
 */
    }

    public Collection<String> removeAll(PropertyType type) {
        isDirty = true;
        return metadata.removeAll(type);
    }

    public Collection<Entry<PropertyType, String>> entries() {
        return metadata.entries();
    }

    public boolean wasChanged() {
        return isDirty;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
            return true;
        }
		if (obj == null) {
            return false;
        }
		if (getClass() != obj.getClass()) {
            return false;
        }
		Metadata other = (Metadata) obj;
		if (metadata == null) {
            return other.metadata == null;
		} else {
		    return metadata.equals(other.metadata);
        }
    }

	@Override
	public String toString() {
		return "Metadata [metadata=" + metadata + ", namedMetadata=" + namedMetadata + ", isDirty=" + isDirty + "]";
	}

}
