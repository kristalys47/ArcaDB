package coordinator;

import org.apache.calcite.plan.RelOptCost;
import org.apache.calcite.plan.RelOptCostImpl;

public class NodeCost extends RelOptCostImpl {

    public NodeCost(double value) {
        super(value);
    }

}
