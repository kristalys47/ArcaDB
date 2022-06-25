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
        Controller.handleRequest("select * from mytable where mytable.id > 20;");
    }

}
