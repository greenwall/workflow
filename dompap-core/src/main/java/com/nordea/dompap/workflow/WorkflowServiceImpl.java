package com.nordea.dompap.workflow;

import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.nordea.dompap.jdbc.JdbcUtil;
import com.nordea.dompap.jdbc.SqlStatement;
import com.nordea.dompap.workflow.config.WorkflowConfig;
import com.nordea.dompap.workflow.content.DefaultWorkflowContentSerializer;
import com.nordea.dompap.workflow.content.WorkflowContentSerializer;
import com.nordea.dompap.workflow.selector.ExecutorInfo;
import com.nordea.dompap.workflow.selector.WorkflowSelector;
import com.nordea.next.dompap.domain.BranchId;
import com.nordea.next.dompap.domain.ProfitCenter;
import com.nordea.next.dompap.domain.UserId;
import com.nordea.next.dompap.workflow.annotation.SerializerAnnotationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Interval;

import javax.resource.ResourceException;
import javax.sql.DataSource;
import java.io.*;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {
	private static final String workFlowColumns = " wf.ID, wf.EXTERNAL_KEY, wf.WORKFLOW_CLASS,wf.SUB_TYPE, wf.USER_ID, wf.BRANCH_ID, wf.CREATION_TIME, wf.LAST_UPDATE_TIME, "
            + "wf.METHOD, wf.START_WHEN, wf.METHOD_STARTED, wf.METHOD_ENDED, wf.METHOD_EXCEPTION, wf.METHOD_EXCEPTION_MESSAGE, wf.REQUEST_DOMAIN, "
            + "wf.LABEL, wf.EXEC_SEQNO, wf.EVENTS_QUEUED, wf.LATEST_EVENT, wf.PROCESS_EVENTS,wf.SERVERNAME, wf.VERSION, wf.CURRENT_EVENT ";;

    private final WorkflowConfig config;
    private final DataSource dataSource;
    private final MetadataService metadataService;
    private final WorkflowSelector workFlowSelector;
    private final WorkflowStatusService workFlowStatusService;

    @Override
    public <T> Workflow<T> insertWorkFlow(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId,
                                          T workflowInstance, Method method, Date startWhen, Metadata metadata, WorkflowController controller)
            throws ResourceException, IOException {
    	String subType = null;
    	return insertWorkFlow(
    			id, externalKey, userId, requestDomain, branchId, workflowInstance, subType, method, startWhen, metadata, controller);
    }

    @Override
    public <T> Workflow<T> insertWorkFlow(WorkflowBuilder<T> workflowBuilder) throws ResourceException, IOException {
        WorkflowContentSerializer contentSerializer = workflowBuilder.contentSerializer;
        if (contentSerializer==null) {
        	contentSerializer = getSerializerForWorkFlow(workflowBuilder.workflowClassName);
        }
		if (contentSerializer==null) {
			contentSerializer = new DefaultWorkflowContentSerializer();
		}

    	return insertWorkFlow(
    			workflowBuilder.id,
    			workflowBuilder.externalKey,
    			workflowBuilder.userId,
    			workflowBuilder.requestDomain,
    			workflowBuilder.profitCenter,
                workflowBuilder.branchId,
    			workflowBuilder.workflowClassName,
    			workflowBuilder.subType,
    			contentSerializer,
    			workflowBuilder.workflow,
    			workflowBuilder.methodName,
    			workflowBuilder.startWhen,
    			workflowBuilder.metadata,
    			workflowBuilder.controller);           	
    }    

    @Override
    public <T> Workflow<T> insertWorkFlow(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId,
                                          T workflowInstance, String subType, Method method, Date startWhen, Metadata metadata, WorkflowController controller)
            throws ResourceException, IOException {
        if (workflowInstance == null) {
            throw new NullPointerException("No workflow instance.");
        }
        if (method == null) {
            throw new NullPointerException("No start method given.");
        }
        WorkflowContentSerializer contentSerializer = getSerializerForWorkFlow(workflowInstance.getClass());
		if (contentSerializer==null) {
			contentSerializer = new DefaultWorkflowContentSerializer();
		}			        
    	return insertWorkFlow(
    			id, externalKey, userId, requestDomain, branchId, 
    			workflowInstance.getClass().getName(), subType, contentSerializer, workflowInstance, 
    			method.getName(), startWhen, metadata, controller);
    }
    
    public <T> Workflow<T> insertWorkFlow(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId,
                                          String workflowClassName, WorkflowContentSerializer contentSerializer, T workflowInstance, String methodName, Date startWhen, Metadata metadata, WorkflowController controller)
            throws ResourceException, IOException {
    	// Default subType
    	String subType = null;
    	return insertWorkFlow(id, externalKey, userId, requestDomain, branchId, workflowClassName, subType, contentSerializer, workflowInstance, methodName, startWhen, metadata, controller);
    }

    @Override
    public <T> Workflow<T> insertWorkFlow(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId,
                                          String workflowClassName, String subType, WorkflowContentSerializer contentSerializer, T workflowInstance, String methodName, Date startWhen, Metadata metadata, WorkflowController controller)
            throws ResourceException, IOException {
        return insertWorkFlow(id, externalKey, userId, requestDomain, null, branchId, workflowClassName, subType, contentSerializer, workflowInstance, methodName, startWhen, metadata, controller);
    }

    /**
     * Stores the workflow including content and metadata to the database
     */
    public <T> Workflow<T> insertWorkFlow(UUID id, String externalKey, UserId userId, String requestDomain, ProfitCenter profitCenter, BranchId branchId,
                                          String workflowClassName, String subType, WorkflowContentSerializer contentSerializer, T workflowInstance, String methodName, Date startWhen, Metadata metadata, WorkflowController controller)
            throws ResourceException, IOException {
        if (workflowInstance == null) {
            throw new NullPointerException("No workflow instance.");
        }

        // If no id was given, create one.
        if (id == null) {
            id = UUID.randomUUID();
        }

        byte[] controllerBytes = serialize(controller);

        String sql = null;
        try (Connection con = getDataSource().getConnection()) {
            con.setAutoCommit(false);

            Date creationTime = new Date();
            Date lastUpdateTime = new Date();
            String branch = BranchUtil.toString(branchId, profitCenter);

            sql = "insert into WFLW_WORKFLOW (ID, EXTERNAL_KEY, WORKFLOW_CLASS, USER_ID, BRANCH_ID, CREATION_TIME, LAST_UPDATE_TIME, METHOD, START_WHEN, SUB_TYPE, REQUEST_DOMAIN) values (?,?,?,?,?,?,?,?,?,?,?) ";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, id.toString());
                ps.setString(2, externalKey);
                ps.setString(3, workflowClassName);
                ps.setString(4, userId != null ? userId.getId() : null);
                ps.setString(5, branch);
                ps.setTimestamp(6, new java.sql.Timestamp(creationTime.getTime()));
                ps.setTimestamp(7, new java.sql.Timestamp(lastUpdateTime.getTime()));
                ps.setString(8, methodName);
                ps.setTimestamp(9, startWhen != null ? new java.sql.Timestamp(startWhen.getTime()) : new java.sql.Timestamp(System.currentTimeMillis()));
                ps.setString(10, subType);
                ps.setString(11, requestDomain);
                ps.executeUpdate();
            }

            // Serialize and insert content in same transaction. If serialization fails entire transaction is rolled back.
            contentSerializer.insertContent(con, id, workflowInstance);
            
            sql = "insert into WFLW_WORKFLOW_CONTROLLER (ID, CONTROLLER) values (?,?)";
            ByteArrayInputStream bais = new ByteArrayInputStream(controllerBytes);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, id.toString());
                ps.setBinaryStream(2, bais, controllerBytes.length);
                ps.execute();
            }

            metadataService.storeMetadata(con, id, metadata);
            con.commit();
            con.setAutoCommit(true);

            Workflow<T> wf;
            if (branchId!=null) {
                wf = new Workflow<>(id, externalKey, workflowInstance.getClass().getName(), userId, branchId, creationTime, lastUpdateTime);
            } else {
                wf = new Workflow<>(id, externalKey, workflowInstance.getClass().getName(), userId, profitCenter, creationTime, lastUpdateTime);
            }
            wf.setContent(workflowInstance);
            return wf;

        } catch (SQLException e) {
            throw new ResourceException(e + ":" + sql, e);
        }
    }

    @SuppressWarnings("unchecked")
	@Override
    public <T> Workflow<T> getWorkFlow(UUID uuid, Class<T> interfaceClass) throws ResourceException {
        return getWorkFlow(uuid);
    }

	@SuppressWarnings("unchecked")
	@Override
    public <T> Workflow<T> getWorkFlow(UUID uuid) throws ResourceException {
        String sql = "select * from WFLW_WORKFLOW where ID=? ";
        try (Connection con = getDataSource().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, uuid.toString());
            return JdbcUtil.exactQuery(ps, workFlowMapperWithStacktrace);
        } catch (SQLException e) {
            throw new ResourceException(e.toString() + ":" + sql, e);
        }
    }

    @SuppressWarnings("unchecked")
	public <T> Workflow<T> findWorkFlowByExternalKey(String externalKey) throws ResourceException {
        String sql = "select * from WFLW_WORKFLOW where EXTERNAL_KEY=? ";
        try (Connection con = getDataSource().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, externalKey);
            return JdbcUtil.exactQuery(ps, workFlowMapper);
        } catch (SQLException e) {
            throw new ResourceException(e.toString() + ":" + sql, e);
        }
    }

    /**
     * Selects a list of workflows - no content and no stacktraces are included
     * in the workflows listed.
     */
    @SuppressWarnings("rawtypes")
    @Override
	public List<Workflow> getWorkFlows(UserId userId, BranchId branchId, Interval creationTime, Integer startRow,
                                       Integer maxRows) throws ResourceException {
        WorkflowSearch search = new WorkflowSearch();
        search.setUserId(userId);
        search.setBranchId(branchId);
        search.setCreationTime(creationTime);

        WorkflowSearchResult result = searchWorkFlows(search, startRow, maxRows);
        return result.workflows;
    }

    /**
     * use {@link #searchWorkFlowsAggregated}
     */
    @Override
    @Deprecated
	public WorkflowSearchResult searchWorkFlows(WorkflowSearch search, Integer startRow, Integer maxRows) throws ResourceException {
        int total = countWorkFlows(search, true);

        int minRow = startRow != null ? startRow : 0;
        int maxRow = maxRows != null ? minRow + maxRows : minRow + 100;

        List<Object> params = new ArrayList<>();
        StringBuilder select = new StringBuilder();

        // Topmost select joins workflow and metadata with the selected subset
        // of workflow ids.
        select.append("select doc.*, ");
        select.append("meta.PROPERTY_ID, meta.VALUE ");
        select.append("from WFLW_WORKFLOW doc left outer join WFLW_METADATA meta on doc.ID = meta.WORKFLOW_ID,  ");
        select.append("(");

        // Paging selects to wrap real select defining rownum twice!
        // select * from (select tmp.*, rownum rn from (
        select.append("select * from (select tmp.*, rownum rn from (");

        // Subset of workflows
        select.append("select doc.* ");

        addSearchQuery(select, params, search, true);

        // Order by needed for paging to retain same ordering
        select.append(" order by doc.CREATION_TIME desc, doc.ID ");

        // Paging wrapper
        // ) tmp where rownum<=200) where rn > 190
        select.append(") tmp where rownum<=?) where rn > ?");
        select.append(") id ");
        select.append("where id.ID = doc.ID "); // and doc.ID = meta.WORKFLOW_ID
        // ");
        select.append(" order by doc.CREATION_TIME desc, doc.ID, doc.WORKFLOW_CLASS, doc.USER_ID ");

        params.add(maxRow);
        params.add(minRow);

        return search(total, params, select);
    }

    @Override
    public WorkflowSearchResult searchWorkFlowsAggregated(WorkflowSearch search, Integer startRow, Integer maxRows, Integer totalRows) throws ResourceException {
        int total = totalRows == null ? countWorkFlows(search, true) : totalRows;

        int minRow = startRow != null ? startRow : 0;
        int maxRow = maxRows != null ? minRow + maxRows : minRow + 100;

        List<Object> params = new ArrayList<>();
        StringBuilder select = new StringBuilder();

        select.append("select ");
        select.append(workFlowColumns);
        select.append(", LISTAGG(PROPERTY_ID || ',' || VALUE , '; ') WITHIN GROUP (ORDER BY PROPERTY_ID) AS METADATA from ");
        select.append("(");

        // Paging selects to wrap real select defining rownum twice!
        select.append("select * from (select tmp.*, rownum rn from (");

        // Subset of workflows
        select.append("select doc.* ");

        addSearchQuery(select, params, search, true);

        // Order by needed for paging to retain same ordering
        select.append(" order by doc.CREATION_TIME desc, doc.ID ");

        // Paging wrapper
        select.append(") tmp where rownum<=?) where rn > ?");
        select.append(") wf ");
        select.append(" left outer join WFLW_METADATA meta on wf.ID = meta.WORKFLOW_ID ");
        select.append(" group by ");
        select.append(workFlowColumns);

        params.add(maxRow);
        params.add(minRow);

        return search(total, params, select);
    }

	@Override
    public int resumeWorkFlows(WorkflowSearch search, List<String> workFlowIdList, String methodName) throws ResourceException {
        List<Object> params = new ArrayList<>();
        StringBuilder select = new StringBuilder();

        // Update part is taken from saveWorkflow method (the one where startWhen= null & methodStarted=null)
        select.append("update WFLW_WORKFLOW set METHOD=?, START_WHEN=?, METHOD_STARTED=?, IS_PICKED=0, METHOD_ENDED=?, METHOD_EXCEPTION=?, METHOD_EXCEPTION_MESSAGE=?, STACKTRACE=?, SERVERNAME=?, LAST_UPDATE_TIME=?, PROCESS_EVENTS=? ");
        select.append("where ID in ( ");

        if (workFlowIdList != null && !workFlowIdList.isEmpty()) {
            // Resuming of a list of workflowIds
            List<String> placeHolders = new ArrayList<>();
            for (String wfId : workFlowIdList) {
                placeHolders.add("?");        // we will set as many '?' as is in the list
                params.add(wfId);            // And add wfId to parameter
            }
            select.append(StringUtils.join(placeHolders, ","));
        } else {
            select.append("select doc.ID ");
            addSearchQuery(select, params, search, true);    // This part is same as in searchWorkflowsAggregated, but without the aggregating, group by, order by and paging stuff.
        }
        select.append(" )");
        String sql = select.toString();

        String serverName = WorkflowUtil.getServerName();
        Date now = new Date();

        try (Connection con = getDataSource().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            // Set parameter for UPDATE part
            int ofs = 1;
            ps.setString(ofs++, methodName != null ? methodName : "-");
            ps.setTimestamp(ofs++, JdbcUtil.toTimestamp(now));    // START_WHEN
            ps.setTimestamp(ofs++, null);    // METHOD_STARTED
            ps.setTimestamp(ofs++, null);    // METHOD_ENDED
            ps.setString(ofs++, null);        // METHOD_EXCEPTION
            ps.setString(ofs++, null);        // METHOD_EXCEPTION_MESSAGE
            ps.setNull(ofs++, Types.CLOB);    // STACKTRACE
            ps.setString(ofs++, serverName);// SERVERNAME
            ps.setTimestamp(ofs++, JdbcUtil.toTimestamp(now));    // LAST_UPDATE_TIME
            ps.setInt(ofs++, 0);            // PROCESS_EVENTS

            // Set parameter for SELECT part
            for (Object param : params) {        // params holds either a list of wfIds or query parameters
                ps.setObject(ofs++, param);
            }

            ps.execute();
            return ps.getUpdateCount();
        } catch (SQLException e) {
            throw new ResourceException(e + ":" + sql, e);
        }
    }
    

    @SuppressWarnings("rawtypes")
	private WorkflowSearchResult search(int total, List<Object> params, StringBuilder select) throws ResourceException {
        String sql = select.toString();
        try (Connection con = getDataSource().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            int n = 1;
            for (Object param : params) {
                ps.setObject(n++, param);
            }

            Map<Integer, PropertyType> idPropertyTypeMap = metadataService.getIdPropertyTypeMap();
            List<Workflow> workflows = listQuery(ps, workFlowMapper, idPropertyTypeMap);
            return new WorkflowSearchResult(total, workflows);
        } catch (SQLException e) {
            throw new ResourceException(e + ":" + sql, e);
        }
    }
    
    @Override
    public WorkflowSearchResult searchWorkFlows(WorkflowSearch search, Integer startRow, Integer maxRows,
                                                boolean supportBackwardCompatibility) throws ResourceException {
        int total = countWorkFlows(search, supportBackwardCompatibility);

        int minRow = startRow != null ? startRow : 0;
        int maxRow = maxRows != null ? minRow + maxRows : minRow + 100;

        List<Object> params = new ArrayList<>();
        StringBuilder select = new StringBuilder();

        // Topmost select joins workflow and metadata with the selected subset
        // of workflow ids.
        select.append("select doc.*, ");
        select.append("meta.PROPERTY_ID, meta.VALUE ");
        select.append("from WFLW_WORKFLOW doc left outer join WFLW_METADATA meta on doc.ID = meta.WORKFLOW_ID,  ");
        select.append("(");

        // Paging selects to wrap real select defining rownum twice!
        // select * from (select tmp.*, rownum rn from (
        select.append("select * from (select tmp.*, rownum rn from (");

        // Subset of workflows
        select.append("select doc.* ");

        addSearchQuery(select, params, search, supportBackwardCompatibility);

        // Order by needed for paging to retain same ordering
        select.append(" order by doc.CREATION_TIME desc ");

        // Paging wrapper
        // ) tmp where rownum<=200) where rn > 190
        select.append(") tmp where rownum<=?) where rn > ?");
        select.append(") id ");
        select.append("where id.ID = doc.ID "); // and doc.ID = meta.WORKFLOW_ID
        // ");
        select.append(" order by doc.CREATION_TIME desc, doc.WORKFLOW_CLASS, doc.USER_ID ");

        params.add(maxRow);
        params.add(minRow);

        return search(total, params, select);
    }

    @Override
    public int updateWorkFlowLabel(WorkflowSearch search, WorkflowLabel label) throws ResourceException {

        List<Object> params = new ArrayList<>();
        StringBuilder select = new StringBuilder();

        // Topmost select joins workflow and metadata with the selected subset
        // of workflow ids.
        select.append("update WFLW_WORKFLOW set LABEL=? where ID in ( ");

        params.add(label.getId().toString());
        
        // Subset of workflows
        select.append("select doc.ID ");

        addSearchQuery(select, params, search,true);

        select.append(")");

        String sql = select.toString();
        try (Connection con = getDataSource().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            int n = 1;
            for (Object param : params) {
                ps.setObject(n++, param);
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new ResourceException(e + ":" + sql, e);
        }
    }
    
	@SuppressWarnings("rawtypes")
	private List<Workflow> listQuery(PreparedStatement ps, JdbcUtil.RowMapper<Workflow> mapper, Map<Integer, PropertyType> idMap)
            throws SQLException, ResourceException {
        List<Workflow> list = new ArrayList<>();
        ps.setFetchSize(1000);
        try (ResultSet rs = ps.executeQuery()) {
            int rowNum = 1;
            Workflow<?> current = null;
            Multimap<PropertyType, String> metadata = HashMultimap.create();
            Stopwatch stopWatchTotalTime = Stopwatch.createStarted();
            Stopwatch stopWatchRsNext = Stopwatch.createStarted();
            int countPropertyValue = 0;
            int countPropertyId = 0;
            while (rs.next()) {
                stopWatchRsNext.stop();
                UUID id = UUID.fromString(rs.getString("ID"));
                if (current == null || !current.getId().equals(id)) {
                    if (current != null) {
                        current.setMetadata(new Metadata(metadata));
                        metadata = HashMultimap.create();
                    }
                    // Read next
                    Workflow<?> wf = mapper.mapRow(rs, rowNum++);
                    list.add(wf);
                    current = wf;
                }
                if (hasColumn(rs, "PROPERTY_ID")) {
                    countPropertyId++;
                    int propId = rs.getInt("PROPERTY_ID");
                    String propValue = rs.getString("VALUE");
                    PropertyType prop = idMap.get(propId);
                    if (prop != null) {
                        countPropertyValue++;
                        metadata.put(prop, propValue);
                    }
                }
                if (hasColumn(rs, "METADATA")) {
                    String[] metaDataArray = rs.getString("METADATA").split(";");
                    for (String md : metaDataArray) {
                        String[] meta = md.split(",");
                        if (meta.length > 1) {
                            PropertyType prop = idMap.get(Integer.parseInt(meta[0].trim()));
                            if (prop != null) {
                                metadata.put(prop, meta[1]);
                            }
                        }
                    }
                }
                stopWatchRsNext.start();
            }
            String debugString = String.format("listQuery() - unpack workflows: TotalTime: %d ms  RsNextRime: %d  rows: %d  ids: %d values: %d", 
                    stopWatchTotalTime.elapsed(TimeUnit.MILLISECONDS), stopWatchRsNext.elapsed(TimeUnit.MILLISECONDS), rowNum, countPropertyId, countPropertyValue);
            log.debug(debugString);
            if (current != null) {
                current.setMetadata(new Metadata(metadata));
            }
        }
        return list;
    }
	
	private static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
	    ResultSetMetaData rsmd = rs.getMetaData();
	    int columns = rsmd.getColumnCount();
	    for (int x = 1; x <= columns; x++) {
	        if (columnName.equals(rsmd.getColumnName(x))) {
	            return rs.getObject(columnName) != null;
	        }
	    }
	    return false;
	}

    private int countWorkFlows(WorkflowSearch search, boolean supportBackwardCompatibility) throws ResourceException {
        List<Object> params = new ArrayList<>();
        StringBuilder select = new StringBuilder();

        select.append("select count(*) ");
        addSearchQuery(select, params, search,supportBackwardCompatibility);

        String sql = select.toString();
        try (Connection con = getDataSource().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            int n = 1;
            for (Object param : params) {
        		ps.setObject(n++, param);
            }

            return JdbcUtil.countQuery(ps);
        } catch (SQLException e) {
            throw new ResourceException(e + ":" + sql, e);
        }

    }

    private void addSearchQuery(StringBuilder select, List<Object> params, WorkflowSearch search, boolean supportBackwardCompatibility)
            throws ResourceException {

        Multimap<String, String> matchingMetadata = search.getMatchingMetadata();

        Multimap<PropertyType, String> propMatchingMetadata = metadataService.toProperties(matchingMetadata, true);
        addMetadataQuery(select, params, propMatchingMetadata);

        if (search.getUserId() != null) {
            select.append(" and doc.USER_ID=? ");
            params.add(search.getUserId().getId());
        }
        if (search.getRequestDomain() != null) {
            // check if search should be done for multiple request domains
            String[] requestDomains = search.getRequestDomain().split(",");
            if (requestDomains.length > 1) {
                List<String> placeHolders = new ArrayList<>();
                for (String requestDomain : requestDomains) {
                    placeHolders.add("?");
                    params.add(requestDomain);
                }
                if (supportBackwardCompatibility) {
                    select.append(" and (doc.REQUEST_DOMAIN IS NULL or doc.REQUEST_DOMAIN IN (")
                            .append(StringUtils.join(placeHolders, ","))
                            .append(")) ");
                } else {
                    select.append(" and doc.REQUEST_DOMAIN IN (")
                            .append(StringUtils.join(placeHolders, ","))
                            .append(")");
                }
            } else {
                if (supportBackwardCompatibility) {
                    select.append(" and (doc.REQUEST_DOMAIN IS NULL or doc.REQUEST_DOMAIN=?) ");
                } else {
                    select.append(" and doc.REQUEST_DOMAIN=?");
                }
                params.add(search.getRequestDomain());
            }
        }
        if (search.getBranchId() != null) {
            select.append(" and doc.BRANCH_ID=? ");
            params.add(search.getBranchId().getId());
        } else {
            if (search.getProfitCenter() != null) {
                select.append(" and doc.BRANCH_ID=? ");
                params.add(search.getProfitCenter().getId());
            }
        }
        if (search.getCreationTime() != null && search.getCreationTime().getStart() != null) {
            select.append(" and doc.CREATION_TIME>=? ");
            params.add(JdbcUtil.toTimestamp(search.getCreationTime().getStart().toDate()));
        }
        if (search.getCreationTime() != null && search.getCreationTime().getEnd() != null) {
            select.append(" and doc.CREATION_TIME<=? ");
            params.add(JdbcUtil.toTimestamp(search.getCreationTime().getEnd().toDate()));
        }
        if (search.getLastUpdateTime() != null && search.getLastUpdateTime().getStart() != null) {
            select.append(" and doc.LAST_UPDATE_TIME>=? ");
            params.add(JdbcUtil.toTimestamp(search.getLastUpdateTime().getStart().toDate()));
        }
        if (search.getLastUpdateTime() != null && search.getLastUpdateTime().getEnd() != null) {
            select.append(" and doc.LAST_UPDATE_TIME<=? ");
            params.add(JdbcUtil.toTimestamp(search.getLastUpdateTime().getEnd().toDate()));
        }

        // TODO Improve this approach
        if (search.getWorkFlowClasses()!=null) {
            // null means leave out criteria - empty array means no matches.
        	if (search.getWorkFlowClasses().length==0) {
        		// match null
	            select.append(" and doc.WORKFLOW_CLASS is null ");
        	} else if (search.getWorkFlowClasses().length==1) {
            	// Exactly one class
                String workflowClass = search.getWorkFlowClasses()[0];
	            if (workflowClass.contains("%")) {
	                select.append(" and doc.WORKFLOW_CLASS like ? ");
	            } else {
	                select.append(" and doc.WORKFLOW_CLASS = ? ");
	            }
	            params.add(workflowClass);
	    	} else {
	    		// Several classes
        		// TODO Should use array in one placeholder
	    		List<String> placeHolders = new ArrayList<>();
	    		for (String cls : search.getWorkFlowClasses()) {
	    			placeHolders.add("?");
		            params.add(cls);        			    			
	    		}
	            select.append(" and doc.WORKFLOW_CLASS in (")
                        .append(StringUtils.join(placeHolders, ","))
                        .append(") ");
	    	}            
        }

        if (search.getSubType() != null) {
            select.append(" and doc.SUB_TYPE=? ");
            params.add(search.getSubType());
        }
        
        if (search.getWorkFlowId() != null) {
            String workflowId = search.getWorkFlowId().toLowerCase();
            if (workflowId.length() < 32) {
                if (!workflowId.contains("%")) {
                    workflowId = workflowId + "%";
                }
                select.append(" and doc.ID like ? ");
            } else {
                select.append(" and doc.ID = ? ");
            }
            params.add(workflowId);
        }
        
        if (StringUtils.isNotBlank(search.getLabelId())) {
            select.append(" and doc.LABEL = ? ");
            params.add(search.getLabelId());
        } else if (search.getLabelChecked()) {
            select.append(" and  LABEL IS NOT NULL ");
        } else if (search.isExcludeLabels()) {
            select.append(" and  LABEL IS NULL ");
        }

        // TODO Improve this approach
        if (search.getMethods()!=null) {
            // null means leave out criteria - empty array means no matches.
        	if (search.getMethods().length==0) {
        		// match null
	            select.append(" and doc.METHOD is null ");
        	} else if (search.getMethods().length==1) {
        		// Exactly one method
	            select.append(" and doc.METHOD=? ");
	            params.add(search.getMethods()[0]);
        	} else {
        		// Several methods
        		// TODO Should use array in one placeholder
	    		List<String> placeHolders = new ArrayList<>();
	    		for (String method : search.getMethods()) {
	    			placeHolders.add("?");
		            params.add(method);        			    			
	    		}
	            select.append(" and doc.METHOD in (").append(StringUtils.join(placeHolders, ",")).append(") ");
        	}
        }
        
        // Exception
        if (search.getMethodException() != null) {
            String methodException = search.getMethodException();
            if (StringUtils.isBlank(methodException)) {
                select.append(" and doc.METHOD_EXCEPTION is not null ");
            } else {
                if (!methodException.contains("%")) {
                    methodException = "%" + methodException;
                }
                select.append(" and doc.METHOD_EXCEPTION like ? ");
                params.add(methodException);
            }
        }
        
        // Exception message
        if (StringUtils.isNotBlank(search.getExceptionMessage())) {
        	String exceptionMessage = search.getExceptionMessage();
        	if (exceptionMessage.contains("%")) {
                select.append(" and doc.METHOD_EXCEPTION_MESSAGE like ? ");        	        		
        	} else {
                select.append(" and doc.METHOD_EXCEPTION_MESSAGE = ? ");        	        		
        	}
            params.add(exceptionMessage);
        }
        
        // External key
        if (search.getExternalKey() != null) {
            String externalKey = search.getExternalKey();
            if (externalKey.contains("%")) {
                select.append(" and doc.EXTERNAL_KEY like ? ");
            } else {
                select.append(" and doc.EXTERNAL_KEY = ? ");
            }
            params.add(externalKey);
        }
        if (search.isStalled()) {
            select.append(" and IS_PICKED=1");
            select.append(" and doc.METHOD_STARTED<=sysdate-1/1440*2 ");
            select.append(" and doc.METHOD_ENDED IS NULL ");
        }
    }


	@SuppressWarnings("unused")
	private void addMetadataQuery(StringBuilder sql, List<Object> paramValues,
            Multimap<PropertyType, String> matchingMetadata) {
        final List<Entry<PropertyType, String>> properties = new ArrayList<>(matchingMetadata.entries());

        sql.append(" from WFLW_WORKFLOW doc ");
        if (!properties.isEmpty()) {
            int n = 1;
            for (Entry<PropertyType, String> property : properties) {
                String alias = "n" + n;
                n++;
                sql.append(", WFLW_METADATA ").append(alias);
            }
        }

        // hack to ensure all remaining criterias can be appended using "and ...."
        sql.append(" where 1=1 ");

        if (!properties.isEmpty()) {
            int n = 1;
            String left = "doc.ID";
            for (Entry<PropertyType, String> property : properties) {
                String alias = "n" + n;
                n++;
                String right = alias + ".WORKFLOW_ID";
                sql.append(" and ").append(left).append("=").append(right);
                left = right;
            }
        }

        if (!properties.isEmpty()) {
            int n = 1;
            for (Entry<PropertyType, String> property : properties) {
                String alias = "n" + n;
                n++;
                sql.append(" and ").append(alias).append(".PROPERTY_ID=? ");
                paramValues.add(property.getKey().getId());
                sql.append(" and ").append(alias).append(".VALUE");
                if (property.getValue().contains("%")) {
                    sql.append(" like ");
                } else {
                    sql.append("=");
                }
                sql.append("? ");
                paramValues.add(property.getValue());
            }
        }
    }

    @Override
    public <T> T loadWorkFlowContent(Workflow<T> workflow) throws ResourceException, ClassNotFoundException, IOException {
        WorkflowContentSerializer contentSerializer = getSerializerForWorkFlow(workflow.getWorkflowClassName());
		if (contentSerializer==null) {
			contentSerializer = new DefaultWorkflowContentSerializer();
		}			
		return loadWorkFlowContent(workflow, contentSerializer);
    }

    @Override
    public <T> T loadWorkFlowContent(Workflow<T> workflow, WorkflowContentSerializer contentSerializer) throws ResourceException, ClassNotFoundException, IOException {
		try (Connection con = getDataSource().getConnection()) {
			return contentSerializer.loadWorkflowContent(con, workflow);
		} catch (SQLException e) {
			throw new ResourceException(e.toString(), e);
		}
    }
    
    @Override
    public <T> Metadata loadWorkFlowMetadata(Workflow<T> workflow) throws ResourceException {
        return metadataService.getMetadata(workflow.getId());
    }

    @Override
    public <T> void saveWorkFlowMetadata(Workflow<T> workflow, Metadata metadata) throws ResourceException {
		try (Connection con = getDataSource().getConnection()) {
			con.setAutoCommit(false);
            metadataService.storeMetadata(con, workflow.getId(), metadata);
			con.commit();
			con.setAutoCommit(true);
		} catch (SQLException e) {
			throw new ResourceException(e.toString(), e);
		}
    }
    
    @Override
    public <T> void saveWorkFlowContent(Workflow<T> workflow) throws ResourceException {
        if (workflow == null) {
            throw new NullPointerException("No workflow instance.");
        }        
        WorkflowContentSerializer contentSerializer = getSerializerFor(workflow);
		saveWorkFlowContent(workflow, contentSerializer);
    }

    @Override
    public <T> void saveWorkFlowContent(Workflow<T> workflow, WorkflowContentSerializer contentSerializer) throws ResourceException {
        if (workflow == null) {
            throw new NullPointerException("No workflow instance.");
        }        
		try (Connection con = getDataSource().getConnection()) {
            contentSerializer.saveWorkflowContent(con, workflow);
            // TODO Store metadata if changed within workflow
            //metadataService.storeMetadata(con, workflow.getId(), workflow.getMetadata());
		} catch (SQLException e) {
			throw new ResourceException(e.toString(), e);
		}
    }

    private <T> WorkflowContentSerializer getSerializerFor(Workflow<T> workflow) throws ResourceException {
        WorkflowContentSerializer contentSerializer = getSerializerForWorkFlow(workflow.getWorkflowClassName());
        if (contentSerializer==null) {
            contentSerializer = new DefaultWorkflowContentSerializer();
        }
        return contentSerializer;
    }

    @Override
    public <T> WorkflowController loadWorkFlowController(Workflow<T> workflow) throws ResourceException,
            ClassNotFoundException, IOException {
        SqlStatement sql = new SqlStatement("select ID, CONTROLLER from WFLW_WORKFLOW_CONTROLLER where ID = ? ")
                .addString(workflow.getId().toString());

        try (Connection con = getDataSource().getConnection();
                PreparedStatement ps = sql.prepareStatement(con)) {

            byte[] controller = JdbcUtil.exactQuery(ps, (rs, rowNum) -> {
                Blob b = rs.getBlob("CONTROLLER");
                return b.getBytes(1, (int) b.length());
            });
            if (controller == null) {
                return null;
            } else {
                return (WorkflowController) deserialize(controller);
            }

        } catch (SQLException e) {
            throw new ResourceException(e + ":" + sql, e);
        }

    }

	@Override
    public <T> void saveWorkFlowController(Workflow<T> workflow, WorkflowController controller) throws ResourceException {
        if (workflow == null) {
            throw new NullPointerException("No workflow instance.");
        }
        if (controller == null) {
            return;
        }

        byte[] bytes;
        try {
            bytes = serialize(controller);
        } catch (IOException e1) {
            throw new ResourceException("Workflow instance cannot be serialized", e1);
        }

        SqlStatement sql = new SqlStatement("update WFLW_WORKFLOW_CONTROLLER set CONTROLLER = ? where ID = ?")
                .addBinaryStream(bytes)
                .addString(workflow.getId().toString());
        try (Connection con = getDataSource().getConnection();
                PreparedStatement ps = sql.prepareStatement(con)) {
            ps.execute();

        } catch (SQLException e) {
            throw new ResourceException(e + ":" + sql, e);
        }

    }

    @Override
    public <T> void updateWorkFlow(Workflow<T> workflow, Method method, Date startWhen, Date methodStarted,
                                   Date methodEnded, Throwable methodException, Metadata metadata) throws ResourceException {
        saveWorkFlow(workflow, method.getName(), startWhen, methodStarted, methodEnded, methodException, metadata, false);
    }

    @Override
    public <T> void updateWorkFlow(Workflow<T> workflow, String methodName, Date startWhen, Date methodStarted,
                                   Date methodEnded, Throwable methodException, Metadata metadata) throws ResourceException {
        saveWorkFlow(workflow, methodName, startWhen, methodStarted, methodEnded, methodException, metadata, false);
    }
    
    @Override
    public <T> void updateWorkFlow(Workflow<T> workflow, Method method, Date startWhen, Date methodStarted,
                                   Date methodEnded, Throwable methodException, Metadata metadata, boolean processEvents) throws ResourceException {
        saveWorkFlow(workflow, method.getName(), startWhen, methodStarted, methodEnded, methodException, metadata, processEvents);
    }

    @Override
    public <T> void updateWorkFlow(Workflow<T> workflow, String methodName, Date startWhen, Date methodStarted,
                                   Date methodEnded, Throwable methodException, Metadata metadata, boolean processEvents) throws ResourceException {
        saveWorkFlow(workflow, methodName, startWhen, methodStarted, methodEnded, methodException, metadata, processEvents);
    }

    private <T> void saveWorkFlow(Workflow<T> workflow, String methodName, Date startWhen, Date methodStarted,
                                  Date methodEnded, Throwable methodException, Metadata metadata, boolean processEvents) throws ResourceException {
        try (Connection con = getDataSource().getConnection()) {
            saveWorkFlow(con, workflow, methodName, startWhen, methodStarted, methodEnded, methodException, metadata, processEvents);
        } catch (SQLException e) {
            throw new ResourceException(e.toString(), e);
        }
    }

    @Override
    public <T> void stepExecutionCompleted(Workflow<T> workflow, Method method, Date startWhen, Date methodStarted, Date methodEnded, Throwable methodException, Metadata metadata, boolean processEvents) throws ResourceException {
        if (workflow == null) {
            throw new NullPointerException("No workflow instance.");
        }
        WorkflowContentSerializer contentSerializer = getSerializerFor(workflow);

        try (Connection con = getDataSource().getConnection()) {
            try {
                con.setAutoCommit(false);
                saveWorkFlow(con, workflow, method.getName(), startWhen, methodStarted, methodEnded, methodException, metadata, processEvents);
                contentSerializer.saveWorkflowContent(con, workflow);
                // TODO Store metadata if changed within workflow
                //metadataService.storeMetadata(con, workflow.getId(), workflow.getMetadata());

                con.commit();
            } catch (ResourceException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ResourceException(e.toString(), e);
        }
    }

    // TODO Why is metadata included when it's not written!?!?!?!?!
    private <T> void saveWorkFlow(Connection con, Workflow<T> workflow, String methodName, Date startWhen, Date methodStarted,
                                  Date methodEnded, Throwable methodException, Metadata metadata, boolean processEvents) throws ResourceException {
        StringBuilder sql = new StringBuilder();

        // If update is for storing the execution of a method then insert history
        if (methodName != null && startWhen == null && methodEnded != null) {
            insertWorkFlowHistory(con, workflow, methodName, methodStarted, methodEnded, methodException);
        }

        String serverName = WorkflowUtil.getServerName();
        String version = WorkflowUtil.getVersionFromWorkflow(getClass(), workflow);
        Date lastUpdateTime = new Date();

        if (startWhen == null) {
        	// Don't update it - keep current value
	        if (methodStarted == null) // Set IS_PICKED flag to 0
	        	sql.append("update WFLW_WORKFLOW set METHOD=?, METHOD_STARTED=?, IS_PICKED=0, METHOD_ENDED=?, METHOD_EXCEPTION=?, METHOD_EXCEPTION_MESSAGE=?, STACKTRACE=?, SERVERNAME=?, VERSION=?, LAST_UPDATE_TIME=?, PROCESS_EVENTS=? where ID = ? ");
	        else
	        	sql.append("update WFLW_WORKFLOW set METHOD=?, METHOD_STARTED=?, METHOD_ENDED=?, METHOD_EXCEPTION=?, METHOD_EXCEPTION_MESSAGE=?, STACKTRACE=?, SERVERNAME=?, VERSION=?, LAST_UPDATE_TIME=?, PROCESS_EVENTS=? where ID = ? ");
        } else {
            if (methodStarted == null) // Set IS_PICKED flag to 0
            	sql.append("update WFLW_WORKFLOW set METHOD=?, START_WHEN=?, METHOD_STARTED=?, IS_PICKED=0, METHOD_ENDED=?, METHOD_EXCEPTION=?, METHOD_EXCEPTION_MESSAGE=?, STACKTRACE=?, SERVERNAME=?, VERSION=?, LAST_UPDATE_TIME=?, PROCESS_EVENTS=? where ID = ? ");
            else
            	sql.append("update WFLW_WORKFLOW set METHOD=?, START_WHEN=?, METHOD_STARTED=?, METHOD_ENDED=?, METHOD_EXCEPTION=?, METHOD_EXCEPTION_MESSAGE=?, STACKTRACE=?, SERVERNAME=?, VERSION=?, LAST_UPDATE_TIME=?, PROCESS_EVENTS=? where ID = ? ");

        }
	        
        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {

        	int ofs = 1;
        	
            ps.setString(ofs++, methodName != null ? methodName : "-");
            if (startWhen != null)
            	ps.setTimestamp(ofs++, JdbcUtil.toTimestamp(startWhen));
            ps.setTimestamp(ofs++, JdbcUtil.toTimestamp(methodStarted));
            ps.setTimestamp(ofs++, JdbcUtil.toTimestamp(methodEnded));
            ps.setString(ofs++, methodException != null ? StringUtils.left(methodException.getClass().getName(), 200) : null);
            // TODO since String is stored as UTF-8 in VARCHAR2(2000) it holds 2000 BYTES not CHARS
            ps.setString(ofs++, methodException != null ? StringUtils.left(methodException.getMessage(), 2000-20) : null);

            String stacktrace = null;
            if (methodException != null) {
                stacktrace = exceptionToString(methodException);
                ps.setCharacterStream(ofs++, new StringReader(stacktrace), stacktrace.length());
            } else {
            	ps.setNull(ofs++, Types.CLOB);
            }

            ps.setString(ofs++, StringUtils.left(serverName, 30));
            ps.setString(ofs++, StringUtils.left(version, 30));
            ps.setTimestamp(ofs++, JdbcUtil.toTimestamp(lastUpdateTime));
            ps.setInt(ofs++, processEvents ? 1 : 0);
            ps.setString(ofs++, workflow.getId().toString());
            ps.execute();

            if (methodName != null) {
                workflow.setMethodName(methodName);
            }
            workflow.setMethodStarted(methodStarted);
            workflow.setMethodEnded(methodEnded);
            workflow.setExceptionMessage(methodException != null ? methodException.getMessage() : null);
            workflow.setExceptionName(methodException != null ? methodException.getClass().getName() : null);
            workflow.setStacktrace(stacktrace);
            workflow.setServerName(serverName);
            workflow.setVersion(version);


        } catch (SQLException e) {
            throw new ResourceException(e + ":" + sql, e);
        }
    }

    private <T> void insertWorkFlowHistory(Workflow<T> workflow, String methodName, Date methodStarted, Date methodEnded,
                                           Throwable methodException) throws ResourceException {
        try (Connection con = getDataSource().getConnection()) {
            insertWorkFlowHistory(con, workflow, methodName, methodStarted, methodEnded, methodException);
        } catch (SQLException e) {
            throw new ResourceException(e.toString(), e);
        }
    }

    private <T> void insertWorkFlowHistory(Connection con, Workflow<T> workflow, String methodName, Date methodStarted, Date methodEnded,
                                           Throwable methodException) throws ResourceException {
        if (workflow == null) {
            throw new NullPointerException("No workflow instance.");
        }

        String sql;
        if (methodException != null) {
            sql = "insert into WFLW_WORKFLOW_HISTORY (ID, METHOD, METHOD_STARTED, METHOD_ENDED, SERVERNAME, VERSION, METHOD_EXCEPTION, METHOD_EXCEPTION_MESSAGE, STACKTRACE) "
                    + " values (?, ?, ?, ?, ?, ?, ?, ?, ?) ";
        } else {
            sql = "insert into WFLW_WORKFLOW_HISTORY (ID, METHOD, METHOD_STARTED, METHOD_ENDED, SERVERNAME, VERSION) "
                    + " values (?, ?, ?, ?, ?, ?) ";
        }

        String serverName = WorkflowUtil.getServerName();
        String version = WorkflowUtil.getVersionFromWorkflow(getClass(), workflow);

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, workflow.getId().toString());
            ps.setString(2, methodName);
            ps.setTimestamp(3, JdbcUtil.toTimestamp(methodStarted));
            ps.setTimestamp(4, JdbcUtil.toTimestamp(methodEnded));
            ps.setString(5, serverName);
            ps.setString(6, version);

            if (methodException != null) {
                ps.setString(7, StringUtils.left(methodException.getClass().getName(), 200));
                // TODO since String is stored as UTF-8 in VARCHAR2(2000) it holds 2000 BYTES not CHARS
                ps.setString(8, StringUtils.left(methodException.getMessage(), 2000-20));

                String stacktrace = exceptionToString(methodException);

                ps.setCharacterStream(9, new StringReader(stacktrace), stacktrace.length());
            }
            ps.execute();

        } catch (SQLException e) {
            throw new ResourceException(e + ":" + sql, e);
        }
    }

    public static String exceptionToString(final Throwable throwable) {
        final CharArrayWriter caw = new CharArrayWriter();
        final PrintWriter printWriter = new PrintWriter(caw);
        throwable.printStackTrace(printWriter);
        return caw.toString();
    }

    /**
     * Returns a list of methods with result that this workflow has executed.
     */
	public <T> List<WorkflowStepResult> getWorkFlowHistory(Workflow<T> workflow) throws ResourceException {
        if (workflow == null) {
            throw new NullPointerException("No workflow instance.");
        }

        String sql = "select ID, METHOD, METHOD_STARTED, METHOD_ENDED, METHOD_EXCEPTION, METHOD_EXCEPTION_MESSAGE, STACKTRACE, SERVERNAME, VERSION from WFLW_WORKFLOW_HISTORY "
                + " where ID=? order by METHOD_STARTED desc ";

        try (Connection con = getDataSource().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, workflow.getId().toString());

            return JdbcUtil.listQuery(ps, workFlowStepresultMapper);
        } catch (SQLException e) {
            throw new ResourceException(e + ":" + sql, e);
        }
    }

    /**
     * Selects a workflow ready for execution.
     * Ensures that only one worker picks a ready workflow, by selecting and
     * updating the selection using METHOD_STARTED as a lock.
     */
	@Override
	public <T> Workflow<T> pickReadyWorkFlow(String workflowClassName, String subType) throws ResourceException {
		List<Workflow<T>> result = pickReadyWorkFlows(workflowClassName, subType, 1);
		if (result == null || result.isEmpty()) {
            return null;
        }
		return result.get(0);
	}
	
    @Override
    public <T> List<Workflow<T>> pickReadyWorkFlows(String workflowClassName, String subType, int maxCount) throws ResourceException {
        String serverName = WorkflowUtil.getServerName();
        String version = WorkflowUtil.getVersionFromWorkflow(this.getClass(), workflowClassName);
        ExecutorInfo executorInfo = new ExecutorInfo(serverName, version);
        return workFlowSelector.pickReadyWorkFlows(workflowClassName, subType, executorInfo, maxCount);
    }

    @Override
    public void updateWorkFlow(UUID workflowId, String externalKey) throws ResourceException {
        SqlStatement sql = new SqlStatement("update WFLW_WORKFLOW set EXTERNAL_KEY=? where ID=?")
                .addString(externalKey)
                .addString(workflowId.toString());

        try (Connection con = getDataSource().getConnection();
                PreparedStatement ps = sql.prepareStatement(con)) {
            ps.execute();

        } catch (SQLException e) {
            throw new ResourceException(e + ":" + sql, e);
        }
    }

    /**
     * TODO Consider if queuing an event should be done as part of event creation.
     */
    @Override
	public <T> void queueEvent(Workflow<T> workflow, UUID eventId) throws ResourceException {
    	
    	Date latestEvent = new Date();
    	SqlStatement sql = new SqlStatement("update WFLW_WORKFLOW set EVENTS_QUEUED = EVENTS_QUEUED+1, LATEST_EVENT=?, CURRENT_EVENT=? where ID=?")
            .addTimestamp(JdbcUtil.toTimestamp(latestEvent))
            .addString(eventId.toString())
    		.addString(workflow.getId().toString());
    	
        try (Connection con = getDataSource().getConnection(); PreparedStatement ps = sql.prepareStatement(con)) {
            ps.execute();
        } catch (SQLException e) {
            throw new ResourceException(e + ":" + sql, e);
        }    	
    }
    
	/**
	 * @deprecated use WorkFlowStatusService
	 */
    @Override
    public List<WorkflowMethodCount> getWorkflowStatus(int[] periods) throws ResourceException {
    	return workFlowStatusService.getWorkflowStatus(periods, null);
    }
    
	/**
	 * @deprecated use WorkFlowStatusService
	 */
    @Override
    public List<WorkflowMethodCount> getWorkflowStatus(int[] periods, String workflowClass) throws ResourceException {
        return workFlowStatusService.getWorkflowStatus(periods, workflowClass);
    }
    
	/**
	 * @deprecated use WorkFlowStatusService
	 */
    @Override
    public List<WorkflowMethodCount> getWorkflowStatus(WorkflowStatusQuery query) throws ResourceException {
    	return workFlowStatusService.getWorkflowStatus(query);
    }

    public <T> void archiveWorkFlow(Workflow<T> workflow) throws ResourceException {
        if (workflow == null) {
            throw new NullPointerException("No workflow instance.");
        }

        SqlStatement sql = null;

        try (Connection con = getDataSource().getConnection()) {
            con.setAutoCommit(false);

            // Copy WORKFLOW
            String workflowColumns = "ID, EXTERNAL_KEY, WORKFLOW_CLASS, SUB_TYPE, USER_ID, BRANCH_ID, CREATION_TIME, LAST_UPDATE_TIME, METHOD, METHOD_STARTED, METHOD_ENDED, SERVERNAME, VERSION, REQUEST_DOMAIN, EXEC_SEQNO, EVENTS_QUEUED, LATEST_EVENT, PROCESS_EVENTS ";
            sql = new SqlStatement("INSERT INTO WFLW_ARCHIVED_WORKFLOW ( " + workflowColumns + " ) ");
            sql.append("SELECT " + workflowColumns + " FROM WFLW_WORKFLOW where ID=?").addString(
                    workflow.getId().toString());
            sql.execute(con);

            WorkflowContentSerializer contentSerializer = getSerializerForWorkFlow(workflow.getWorkflowClassName());
    		if (contentSerializer==null) {
    			contentSerializer = new DefaultWorkflowContentSerializer();
    		}			
    		contentSerializer.archiveWorkflowContent(con, workflow);
            
            // Copy HISTORY
            String historyColumns = "ID, METHOD, METHOD_STARTED, METHOD_ENDED, METHOD_EXCEPTION, METHOD_EXCEPTION_MESSAGE, STACKTRACE";
            sql = new SqlStatement("INSERT INTO WFLW_ARCHIVED_WORKFLOW_HISTORY ( " + historyColumns + " ) ");
            sql.append("SELECT " + historyColumns + " FROM WFLW_WORKFLOW_HISTORY where ID=?").addString(workflow.getId().toString());
            sql.execute(con);

            // COPY EVENTS
            String eventColumns = "ID, CREATION_TIME, CONTENT, WORKFLOW_ID, EVENT_TYPE, EVENT_NAME, PROCESSED_TIME";
			sql = new SqlStatement("INSERT INTO WFLW_ARCHIVED_EVENT ("+eventColumns+") ");
			sql.append("SELECT "+eventColumns+" FROM WFLW_EVENT where WORKFLOW_ID=? ").addString(workflow.getId().toString());
			sql.execute(con);

            // Delete NDS_EVENTS
            sql = new SqlStatement("DELETE FROM WFLW_EVENT where WORKFLOW_ID=?").addString(workflow.getId().toString());
            sql.execute(con);
			
            // Delete HISTORY
            sql = new SqlStatement("DELETE FROM WFLW_WORKFLOW_HISTORY where ID=?").addString(workflow.getId().toString());
            sql.execute(con);

            // Delete CONTROLLER
            sql = new SqlStatement("DELETE FROM WFLW_WORKFLOW_CONTROLLER where ID=?").addString(workflow.getId().toString());
            sql.execute(con);

            // Delete METADATA
            metadataService.deleteMetadata(con, workflow.getId());

            // Delete WORKFLOW
            sql = new SqlStatement("DELETE FROM WFLW_WORKFLOW where ID=?").addString(workflow.getId().toString());
            sql.execute(con);
            
            con.commit();

        } catch (SQLException e) {
            throw new ResourceException(e + ":" + sql, e);
        }
    }

	// TODO Move to WorkFlowManager
	private WorkflowContentSerializer getSerializerForWorkFlow(Class<?> workflowClass) throws ResourceException {
		return getSerializerForWorkFlow(workflowClass.getName());
	}

	// TODO Move to WorkFlowManager
	private WorkflowContentSerializer getSerializerForWorkFlow(final String workflowClassName) throws ResourceException {
		try {
			String serializerClassName = config.getSerializer(workflowClassName);
			if (serializerClassName==null) {
				serializerClassName = SerializerAnnotationUtil.getSerializer(workflowClassName);
			}
            if (StringUtils.isNotBlank(serializerClassName)) {
				Class<?> clazz = Class.forName(serializerClassName.trim());
				Object instance = clazz.newInstance();
				if (instance instanceof WorkflowContentSerializer) {
					return (WorkflowContentSerializer) instance;
				} else {
					throw new ResourceException("Serializer is not instance of WorkflowContentSerializer: "
							+ serializerClassName.trim());
				}
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new ResourceException(e.toString(), e);
		}
		return null;
	}
    
	private byte[] serialize(Object workflowInstance) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(workflowInstance);
        } catch (IOException e) {
            // log.error("Unable to serialize workflow instance", e);
            throw e;
        } catch (Throwable e) {
            // Ignore
        }
        return bout.toByteArray();
    }

    private Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(bytes));
        return oin.readObject();
    }

    private static final JdbcUtil.RowMapper<WorkflowStepResult> workFlowStepresultMapper = (rs, rowNum) -> {
        UUID id = UUID.fromString(rs.getString("ID"));
        String methodName = rs.getString("METHOD");
        Date methodStarted = rs.getTimestamp("METHOD_STARTED");
        Date methodEnded = rs.getTimestamp("METHOD_ENDED");
        String exceptionName = rs.getString("METHOD_EXCEPTION");
        String exceptionMessage = rs.getString("METHOD_EXCEPTION_MESSAGE");
        String stacktrace = rs.getString("STACKTRACE");
        String serverName = rs.getString("SERVERNAME");
        String version = rs.getString("VERSION");
        return new WorkflowStepResult(id, methodName, methodStarted, methodEnded, exceptionName,
                exceptionMessage, stacktrace, serverName, version);
    };

    @SuppressWarnings("rawtypes")
	private static final JdbcUtil.RowMapper<Workflow> workFlowMapper = new JdbcUtil.RowMapper<Workflow>() {
    	private final WorkflowMapper mapper = new WorkflowMapper();
        @Override
        public Workflow mapRow(ResultSet rs, int rowNum) throws SQLException {
        	Workflow<?> wf = mapper.mapRow(rs, rowNum);
        	
            String serverName = rs.getString("SERVERNAME");
            String version = rs.getString("VERSION");
            wf.setServerName(serverName);
            wf.setVersion(version);

            return wf; 
        }
    };

    @SuppressWarnings("rawtypes")
	private static final JdbcUtil.RowMapper<Workflow> workFlowMapperWithStacktrace = new JdbcUtil.RowMapper<Workflow>() {
    	private final WorkflowMapper mapper = new WorkflowMapper();
    	
        @Override
        public Workflow<?> mapRow(ResultSet rs, int rowNum) throws SQLException {
        	Workflow<?> wf = mapper.mapRow(rs, rowNum);
            String stacktrace = rs.getString("STACKTRACE");            
            wf.setStacktrace(stacktrace);
            return wf;
        }
    };

    private DataSource getDataSource() {
        return dataSource;
    }
}
