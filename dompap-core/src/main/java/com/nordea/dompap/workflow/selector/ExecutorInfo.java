package com.nordea.dompap.workflow.selector;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Contains information on the thread/application picking/locking a ready workflow for execution.
 */
@Data
@AllArgsConstructor
public class ExecutorInfo {
    private String serverName;
    private String version;
}
