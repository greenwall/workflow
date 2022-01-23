package com.nordea.dompap.util;

public enum WorkflowProcessingStatus {
    idle,       // Not more work for this workflow class
    hasMore,    // workflow has more to do
    done        // workflow is done.
}
