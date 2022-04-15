package com.nordea.dompap.workflow;

import com.google.common.collect.Multimap;

import javax.resource.ResourceException;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for workflow metadata. 
 * Caches the property types to maintain a unique sequential id for all metadata types.
 * Used by WorkFlowService and from Metadata.
 */
public interface MetadataService {

	Metadata storeMetadata(Connection con, UUID id, Metadata metadata) throws ResourceException;

	Metadata getMetadata(UUID id) throws ResourceException;

	List<PropertyType> getPropertyTypes() throws ResourceException;

	Multimap<PropertyType, String> toProperties(Multimap<String, String> namedProperties, boolean ignoreMissingProperty) throws ResourceException;

	Map<Integer, PropertyType> getIdPropertyTypeMap() throws ResourceException;

	PropertyType getOrCreatePropertyType(String propertyTypeName, String description) throws ResourceException;

	boolean deleteMetadata(Connection con, UUID documentId) throws ResourceException;

}
