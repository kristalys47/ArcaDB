package coordinator;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class TestingAPI {

    @Test
    public void functions() throws Exception {
        Controller.handleRequest("select * from product inner join customer using(id);", 3, 5);
    }

    @Test
    public void insertToContainer() throws Exception {
        Controller.handleRequest("select * from customer inner join product using(id) where price>500 or price<12;", null, 5);
    }

    @Test
    public void querrrryyyy() throws Exception {
        Controller.handleRequest("select * from product where price>500 or price<12;", null, 5);
    }

}
