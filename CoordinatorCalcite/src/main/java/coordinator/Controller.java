package coordinator;

import coordinator.plan.BinaryTreePlan;
import coordinator.plan.UserdefinedFunction;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Controller {
    private static final Logger logger = LogManager.getLogger(Controller.class);
    public static boolean handleRequest(String query, Integer aCase, int buckets) throws Exception {

        CalciteOptimizer plan= new CalciteOptimizer(query);

        System.out.println("test 1");
        if(plan.optimizedPlan.explain().contains("EXPR")){
            UserdefinedFunction function = new UserdefinedFunction(plan);
            function.run();
        } else {
            BinaryTreePlan btp = new BinaryTreePlan(plan, aCase, buckets);
            System.out.println("test 2");
            btp.headNode.run();
            System.out.println("test 3");
        }
        return true;
    }

}
