package com.nordea.dompap.workflow;

import com.nordea.next.dompap.domain.BranchId;
import com.nordea.next.dompap.domain.ProfitCenter;
import org.apache.commons.lang3.StringUtils;

public class BranchUtil {

    public static String toString(BranchId branchId, ProfitCenter profitCenter) {
        String branch = null;
        if (branchId!=null) {
            branch = branchId.getId();
        } else {
            if (profitCenter!=null) {
                branch = profitCenter.getId();
            }
        }
        return branch;
    }

    /**
     * Returns BranchId if branchOrProfitCenter is not a {@link ProfitCenter}. May throw exception if invalid BranchId.
     * @param branchOrProfitCenter branch id or profit center
     * @return null if branchOrProfitCenter is null
     */
    public static BranchId toBranchId(String branchOrProfitCenter) {
        if (StringUtils.isEmpty(branchOrProfitCenter)) {
            return null;
        }
        if (branchOrProfitCenter.length()<ProfitCenter.getProfitCenterLength()) {
            // Avoid knowledge of BranchId length - since it involves countryCode.
            try {
                return new BranchId(branchOrProfitCenter);
            } catch (IllegalArgumentException e) {
                // TODO BranchId should contain isValid(String).
                return null;
            }
        }
        return null;
    }

    /**
     * Returns ProfitCenter if branchOrProfitCenter has correct length. May throw exception is invalid ProfitCenter.
     * @param branchOrProfitCenter branchId or profitCenter
     * @return null if branchOrProfitCenter is null
     */
    public static ProfitCenter toProfitCenter(String branchOrProfitCenter) {
        if (branchOrProfitCenter==null) {
            return null;
        }
        if (branchOrProfitCenter.length()==ProfitCenter.getProfitCenterLength()) {
            // Avoid knowledge of BranchId length - since it involves countryCode.
            return new ProfitCenter(branchOrProfitCenter);
        }
        return null;
    }

}
