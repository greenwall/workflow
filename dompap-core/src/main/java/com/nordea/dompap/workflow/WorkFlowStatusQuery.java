package com.nordea.dompap.workflow;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

/**
 * WorkFlowStatusQuery contains the criterias for summarizing the status of workflows.
 * 
 * periods - The periods in minutes to divide count of workflows in. 
 * 	So giving 100,10 will create 3 columns: one for workflows executed more than 100 minutes ago, one for 10-100 minutes and one for less than 10 minutes ago.
 * workflowClass - shows only status for this specific workflowClass
 * showMethods - whether to divide rows per method, meaning that counting will group both class & method or only class.
 * includeForArchive - whether to include workflows that are waiting for archival.
 * includeLastDays - restrict the status summary to workflows that was executed in these last days - 0 means unrestricted
 * includeRecentMinutes - if non zero then only rows with activity (methods, failed or retry) within these recent minutes are included
 * excludeLabelled - excludes workflows that have a non-expired label marked as excluding from summary.
 * 
 * Implements Serializable to allow storing in user session
 * @author G93283
 */
@Data
public class WorkFlowStatusQuery implements Serializable {
	private static final long serialVersionUID = 1L;

	private int[] periods;
    private String workflowClass;
    private boolean showMethods = true;
    private boolean includeForArchive = true;
    private int includeLastDays = 0; // unlimited - if positive only include workflows updated within last x days.
    private int includeRecentMinutes = 60;
    private boolean excludeLabelled = false;
    private UUID labelId = null;
}
