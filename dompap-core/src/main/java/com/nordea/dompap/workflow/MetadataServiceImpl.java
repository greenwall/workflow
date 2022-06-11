package com.nordea.dompap.workflow;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.nordea.dompap.jdbc.JdbcUtil;
import com.nordea.dompap.workflow.config.WorkflowConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import javax.resource.ResourceException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.Map.Entry;

@Slf4j
@RequiredArgsConstructor
public class MetadataServiceImpl implements MetadataService {
	private static final int PROPERTY_VALUE_MAX_LENGTH =200;

	private List<PropertyType> propertyTypes;
	private Map<String, PropertyType> nameTypeMap;
	private Map<Integer, PropertyType> idTypeMap;

	private final WorkflowConfig config;
	private final DataSource dataSource;

	/**
	 * Stores metadata if modified and returns a "clean" metadata instance.
	 */
	@Override
	public Metadata storeMetadata(Connection con, UUID id, Metadata metadata) throws ResourceException {
		if (metadata==null || !metadata.wasChanged()) {
			return metadata;
		}

		String insertIntoMetadata = "insert into WFLW_METADATA (WORKFLOW_ID, PROPERTY_ID, VALUE) values (?,?,?) ";
		try (PreparedStatement ps = con.prepareStatement(insertIntoMetadata)) {

			deleteMetadata(con, id);
			
			boolean checkPropertyValueLength = config.getCheckMetadata();
			List<String> mp = new ArrayList<>(); 	// The list of properties to store
			for (Map.Entry<PropertyType,String> prop: metadata.entries()) {
				// Only store non-null property values
				if (StringUtils.isNotBlank(prop.getValue())) {
					String value = prop.getValue();
					PropertyType type = prop.getKey();
					if (value.length()> PROPERTY_VALUE_MAX_LENGTH) {
						if (checkPropertyValueLength) {
							throw new IllegalArgumentException("Property value can not be longer than "+ PROPERTY_VALUE_MAX_LENGTH +" characters ("+value.length()+"): "+type.getName()+"=["+value+"]");
						} else {
							log.info("Property value longer than "+ PROPERTY_VALUE_MAX_LENGTH +" characters ("+value.length()+"): "+type.getName()+"=["+value+"]");
						}
					}
					mp.add(type.getName());
					PropertyType propType = getOrCreatePropertyType(con, type.getName(), type.getDescription());

					// Execute inserts in batch approximately inserts 10 rows of metadata in half the time
					ps.setString(1, id.toString());
					ps.setInt(2, propType.getId());
					ps.setString(3, StringUtils.left(prop.getValue(), PROPERTY_VALUE_MAX_LENGTH));
					ps.addBatch();			
				
				}
			}
			logMetadataPropertyTypesToBeUsed(id, mp);
			ps.executeBatch();			
			return new Metadata(metadata.metadata);
		} catch (SQLException e) {
			throw new ResourceException(e.toString(), e);
		}
		
	}

	private void logMetadataPropertyTypesToBeUsed(UUID id, List<String> props) {
		for (String prop : props) {
			PropertyType pt = nameTypeMap.get(prop);
			if (pt != null) {
				log.info("Workflow: {} Storing value for property type: {} {}", id.toString(), pt.getId(), pt.getName());
			} else {
				log.error("Workflow: {} Missing property type to use for storing value: {}", id.toString(), prop);
			}
		}
	}

	@Override
	public Metadata getMetadata(UUID id) throws ResourceException {
		getPropertyTypes();

		String selectMetadata = "select doc.ID, t.NAME PROP_NAME, n.VALUE from WFLW_WORKFLOW doc, WFLW_METADATA n, WFLW_PROPERTYTYPE t where doc.ID=? and n.PROPERTY_ID=t.ID and doc.ID=n.WORKFLOW_ID";
		try (Connection con = getDataSource().getConnection();
			 PreparedStatement ps = con.prepareStatement(selectMetadata)) {
			Map<String, PropertyType> typeMap = getNamedPropertyMap(con);
			ps.setObject(1, id.toString());
			
			Multimap<PropertyType, String> metadata = HashMultimap.create();
			try (ResultSet rows = ps.executeQuery()) {
				while (rows.next()) {
					// Same document - just add property
					String name = rows.getString("PROP_NAME");
					String value = rows.getString("VALUE");						
					metadata.put(typeMap.get(name), value);
				}
			}			
			return new Metadata(metadata);
		} catch (SQLException e) {
			throw new ResourceException(e + ":" + selectMetadata + " WorkflowId: " + id.toString(), e);
		}
						
	}

	@Override
	public List<PropertyType> getPropertyTypes() throws ResourceException {
		if (propertyTypes==null) {
			initPropertyTypes();
		}
		return propertyTypes;
	}

	private PropertyType createPropertyType(Connection con, String name, String description) throws ResourceException {
		String insertIntoPropertyType = "insert into WFLW_PROPERTYTYPE (ID, NAME, DESCRIPTION) values (?,?,?) ";
		try {
			// Select next id
			int nextId;
			String selectMax1FromPropertyType = "select MAX(ID)+1 from WFLW_PROPERTYTYPE";
			try (PreparedStatement ps = con.prepareStatement(selectMax1FromPropertyType)) {
				nextId = JdbcUtil.countQuery(ps);
			}
			PropertyType type;
			// Insert
			try (PreparedStatement ps = con.prepareStatement(insertIntoPropertyType)) {			
				ps.setInt(1, nextId);
				ps.setString(2, name);
				ps.setString(3, description);
				ps.execute();
				
				type = new PropertyType(nextId, name, description);
			}
			log.info("Property type created: {} {}", type.getId(), type.getName());
			// Force reload
			initPropertyTypes(con);
			return type;
		} catch (SQLIntegrityConstraintViolationException e) {
			// Property already inserted - key constraint violation - reload
			// Force reload
			log.error("Property type already inserted: {}", name);
			initPropertyTypes(con);
			throw new ResourceException(e + ":" + insertIntoPropertyType, e);
		} catch (SQLException e) {
			throw new ResourceException(e + ":" + insertIntoPropertyType, e);
		}	
	}

	@Override
	public Multimap<PropertyType, String> toProperties(Multimap<String, String> namedProperties, boolean ignoreMissingProperty) throws ResourceException {
    	Map<String, PropertyType> nameTypeMap = getNamedPropertyMap();
    	
    	Multimap<PropertyType, String> map = HashMultimap.create();
    	if (namedProperties!=null) {
	    	for (Entry<String, String> entry : namedProperties.entries()) {
	    		String propertyName = entry.getKey();
	    		
	        	PropertyType type = nameTypeMap.get(propertyName);	        	
	        	if (type==null) {
	        		if (!ignoreMissingProperty) {
	        			throw new IllegalArgumentException("Property ["+propertyName+"] does not exist.");
	        		}
	        	} else {
	        		map.put(type, entry.getValue());
	        	}
	    	}
    	}
    	return map;
	}

	private Map<String, PropertyType> getNamedPropertyMap() throws ResourceException {
		try (Connection con = getDataSource().getConnection()) {
			return getNamedPropertyMap(con);
		} catch (SQLException e) {
			throw new ResourceException(e.toString(), e);
		}
	}

	private Map<String, PropertyType> getNamedPropertyMap(Connection con) throws ResourceException {
		if (nameTypeMap==null) {
			initPropertyTypes(con);
		}
		return nameTypeMap;
	}
	
	@Override
	public Map<Integer, PropertyType> getIdPropertyTypeMap() throws ResourceException {
		if (idTypeMap==null) {
			initPropertyTypes();
		}
		return idTypeMap;
	}

	public void initPropertyTypes() throws ResourceException {
		try (Connection con = getDataSource().getConnection()) {
			initPropertyTypes(con);
		} catch (SQLException e) {
			throw new ResourceException(e.toString(), e);
		}
	}

	private synchronized void initPropertyTypes(Connection con) throws ResourceException {
    	propertyTypes = loadPropertyTypes(con);
    	HashMap<String, PropertyType> _nameTypeMap = new HashMap<>();
    	for (PropertyType type : propertyTypes) {
    		_nameTypeMap.put(type.getName(), type);
    	}		
    	nameTypeMap = _nameTypeMap; 
    	
    	HashMap<Integer, PropertyType> _idTypeMap = new HashMap<>();
    	for (PropertyType type : propertyTypes) {
    		_idTypeMap.put(type.getId(), type);
    	}	
    	idTypeMap = _idTypeMap;
	}
	
	private final String selectPropertyTypes = "select ID, NAME, DESCRIPTION from WFLW_PROPERTYTYPE";
	private List<PropertyType> loadPropertyTypes(Connection con) throws ResourceException {
//		try (Connection con = getDataSource().getConnection();
		try (PreparedStatement ps = con.prepareStatement(selectPropertyTypes)) {
			return JdbcUtil.listQuery(ps, (rows, rowNum) -> {
				int id = rows.getInt("ID");
				String name = rows.getString("NAME");
				String description = rows.getString("DESCRIPTION");
				return new PropertyType(id, name, description);
			});
		} catch (SQLException e) {
			throw new ResourceException(e + ":" + selectPropertyTypes, e);
		}
	}			
	
	@Override
	public PropertyType getOrCreatePropertyType(String propertyTypeName, String description) throws ResourceException {
		try (Connection con = getDataSource().getConnection()) {
			return getOrCreatePropertyType(con, propertyTypeName, description);
		} catch (SQLException e) {
			throw new ResourceException(e.toString(), e);
		}
	}

	private PropertyType getOrCreatePropertyType(Connection con, String propertyTypeName, String description) throws ResourceException {
		PropertyType type = getNamedPropertyMap(con).get(propertyTypeName);
		if (type==null) {
			log.info("Create metadata property type: {} {}", propertyTypeName, description);
			type = createPropertyType(con, propertyTypeName, description);
		}
		return type;
	}

	@Override
	public boolean deleteMetadata(Connection con, UUID workflowId) throws ResourceException {
		String deleteFromMetadata = "delete from WFLW_METADATA where WORKFLOW_ID = ? ";
		try (PreparedStatement ps = con.prepareStatement(deleteFromMetadata)) {
			ps.setString(1, workflowId.toString());
			return ps.execute();			
		} catch (SQLException e) {
			throw new ResourceException(e + ":" + selectPropertyTypes + " WorkflowId: " + workflowId.toString(), e);
		}		
	}	

	private DataSource getDataSource() {
		return dataSource;
	}
}
