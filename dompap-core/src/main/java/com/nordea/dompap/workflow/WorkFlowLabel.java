package com.nordea.dompap.workflow;

import com.nordea.next.dompap.domain.UserId;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Created by WorkFlowLabelServiceImpl.rowMapper
 */
@Data
@AllArgsConstructor
public class WorkFlowLabel implements Serializable {
	private static final long serialVersionUID = 1L;

	private UUID id;
	private final UserId createdBy;
	private final Date creationTime;
	private Date expireTime;
	private String name;
	private int ignoreWorkflows;
	private String description;
}
