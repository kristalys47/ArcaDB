package coordinator;

import org.apache.calcite.plan.RelOptRule;
import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.plan.RelOptRuleOperand;

public class SiteAllocation extends RelOptRule {

    protected SiteAllocation(RelOptRuleOperand operand) {
        super(operand);
    }

    @Override
    public void onMatch(RelOptRuleCall relOptRuleCall) {

    }
}
