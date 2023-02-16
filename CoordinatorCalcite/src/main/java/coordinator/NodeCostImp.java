package coordinator;

import org.apache.calcite.plan.RelOptCost;
import org.apache.calcite.plan.RelOptCostFactory;
import org.apache.calcite.plan.RelOptCostImpl;
import org.apache.calcite.plan.RelOptUtil;

import javax.annotation.Nullable;

public class NodeCostImp implements RelOptCost {

    public static final RelOptCostFactory FACTORY = new Factory();

    //~ Instance fields --------------------------------------------------------

    private final double value;

    //~ Constructors -----------------------------------------------------------

    public NodeCostImp(double value) {
        this.value = value;
    }

    //~ Methods ----------------------------------------------------------------

    // implement RelOptCost
    @Override public double getRows() {
        return value;
    }

    // implement RelOptCost
    @Override public double getIo() {
        return 0;
    }

    // implement RelOptCost
    @Override public double getCpu() {
        return 0;
    }

    // implement RelOptCost
    @Override public boolean isInfinite() {
        return Double.isInfinite(value);
    }

    // implement RelOptCost
    @Override public boolean isLe(RelOptCost other) {
        return getRows() <= other.getRows();
    }

    // implement RelOptCost
    @Override public boolean isLt(RelOptCost other) {
        return getRows() < other.getRows();
    }

    @Override public int hashCode() {
        return Double.hashCode(getRows());
    }

    // implement RelOptCost
    @SuppressWarnings("NonOverridingEquals")
    @Override public boolean equals(RelOptCost other) {
        return getRows() == other.getRows();
    }

    @Override public boolean equals(@Nullable Object obj) {
        if (obj instanceof RelOptCostImpl) {
            return equals((RelOptCost) obj);
        }
        return false;
    }

    // implement RelOptCost
    @Override public boolean isEqWithEpsilon(RelOptCost other) {
        return Math.abs(getRows() - other.getRows()) < RelOptUtil.EPSILON;
    }

    // implement RelOptCost
    @Override public RelOptCost minus(RelOptCost other) {
        return new RelOptCostImpl(getRows() - other.getRows());
    }

    // implement RelOptCost
    @Override public RelOptCost plus(RelOptCost other) {
        return new RelOptCostImpl(getRows() + other.getRows());
    }

    // implement RelOptCost
    @Override public RelOptCost multiplyBy(double factor) {
        return new RelOptCostImpl(getRows() * factor);
    }

    @Override public double divideBy(RelOptCost cost) {
        RelOptCostImpl that = (RelOptCostImpl) cost;
        return this.getRows() / that.getRows();
    }

    // implement RelOptCost
    @Override public String toString() {
        if (value == Double.MAX_VALUE) {
            return "huge";
        } else {
            return Double.toString(value);
        }
    }

    /** Implementation of {@link RelOptCostFactory} that creates
     * {@link RelOptCostImpl}s. */
    private static class Factory implements RelOptCostFactory {
        // implement RelOptPlanner
        @Override public RelOptCost makeCost(
                double dRows,
                double dCpu,
                double dIo) {
            return new RelOptCostImpl(dRows);
        }

        // implement RelOptPlanner
        @Override public RelOptCost makeHugeCost() {
            return new RelOptCostImpl(Double.MAX_VALUE);
        }

        // implement RelOptPlanner
        @Override public RelOptCost makeInfiniteCost() {
            return new RelOptCostImpl(Double.POSITIVE_INFINITY);
        }

        // implement RelOptPlanner
        @Override public RelOptCost makeTinyCost() {
            return new RelOptCostImpl(1.0);
        }

        // implement RelOptPlanner
        @Override public RelOptCost makeZeroCost() {
            return new RelOptCostImpl(0.0);
        }
    }
}
