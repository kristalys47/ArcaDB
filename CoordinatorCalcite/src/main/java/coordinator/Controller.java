package coordinator;

import org.apache.calcite.rel.RelNode;
import coordinator.plan.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class Controller {
    private static final Logger logger = LogManager.getLogger(Controller.class);
    public static boolean handleRequest(String query, Integer aCase, int buckets) throws Exception {

        RelNode tree = CalciteOptimizer.run(query);

        System.out.println("test 1");
        BinaryTreePlan btp = new BinaryTreePlan(tree, aCase, buckets);
        System.out.println("test 2");
        btp.headNode.run();
        System.out.println("test 3");
        return true;
    }

}
