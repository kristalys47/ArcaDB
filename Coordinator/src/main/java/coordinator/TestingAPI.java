package coordinator;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class TestingAPI {

    @Test
    public void functions() throws SQLException {
        Controller.handleRequest("select * from mytable where mytable.id > 20;");
    }

    @Test
    public void insertToContainer() throws SQLException {
        Controller.handleRequest("select * from customer inner join product using(id) where price>500 or price<12;");
    }

    @Test
    public void querrrryyyy() throws SQLException {
        Controller.handleRequest("select * from product where price>500 or price<12;");
    }

}
