package com.nordea.dompap.workflow;

import java.io.Serializable;

public class MyWorkflow implements Serializable {
	private static final long serialVersionUID = -8430339138038156819L;
	private String some = "Some";
	public void doStuff() {
		// do stuff
	}
	public void doStuff2()  {
		// do stuff 2
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((some == null) ? 0 : some.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MyWorkflow other = (MyWorkflow) obj;
		if (some == null) {
			if (other.some != null)
				return false;
		} else if (!some.equals(other.some))
			return false;
		return true;
	}
}
