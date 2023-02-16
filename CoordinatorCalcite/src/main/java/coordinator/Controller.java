package coordinator;

import coordinator.plan.BinaryTreePlan;
import org.apache.calcite.rel.RelNode;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Controller {
    private static final Logger logger = LogManager.getLogger(Controller.class);
    public static boolean handleRequest(String query, Integer aCase, int buckets) throws Exception {

        CalciteOptimizer plan= new CalciteOptimizer(query);
        plan.run();
        System.out.println(plan.optimizedPlan.getInputs().get(0).getRelTypeName());
        System.out.println("Hellooooooooo");

        System.out.println("test 1");
        BinaryTreePlan btp = new BinaryTreePlan(plan.optimizedPlan.getInputs().get(0), aCase, buckets);
        System.out.println("test 2");
        btp.headNode.run();
        System.out.println("test 3");
        return true;
    }

}
