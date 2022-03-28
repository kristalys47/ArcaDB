import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class TestingAPI {

    @Test
    public void functions() throws SQLException {
        Controller.handleRequest();
    }
}
