package com.nordea.dompap.workflow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class PropertyType implements Serializable {
	private static final long serialVersionUID = 1L;

	int id;
	@EqualsAndHashCode.Exclude String name;
	@EqualsAndHashCode.Exclude String description;

	public PropertyType(String name, String description) {
		this.id = 0; // Indicates that property is not stored
		this.name = name;
		this.description = description;
	}

}	

