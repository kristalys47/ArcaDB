package coordinator;

import com.google.common.collect.ImmutableList;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.plan.Contexts;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.RelWriter;
import org.apache.calcite.rel.externalize.RelWriterImpl;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.pretty.SqlPrettyWriter;
import org.apache.calcite.sql.type.OperandTypes;
import org.apache.calcite.sql.type.ReturnTypes;
import org.apache.calcite.sql.type.SqlReturnTypeInference;
import org.apache.calcite.sql.util.ListSqlOperatorTable;
import org.apache.calcite.sql.util.SqlOperatorTables;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.tools.*;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Objects;
import java.util.Properties;

import static coordinator.Commons.*;
import static coordinator.Commons.POSTGRES_DB_NAME;

public class TestingAPI {
    static public int imageClassifier(int a){
        return a+2;
    }

    @Test
    public void calciteStufftest() throws Exception {
        POSTGRES_PASSWORD = "mypassword";
        POSTGRES_USERNAME = "myusername";
        POSTGRES_HOST = "136.145.77.83";
        POSTGRES_PORT = 5434;
        POSTGRES_DB_NAME = "test";
//        String QUERY = "select product from SCHEMA.\"orders\"";

//        String QUERY = "select * from SCHEMA.\"part\", SCHEMA.\"lineitem\" where \"lineitem\".\"01\" = \"part\".\"00\"";
        String QUERY = "select imageClassifier(12) from SCHEMA.\"part\"";

        POSTGRES_JDBC = "jdbc:postgresql://" + POSTGRES_HOST + ":" + POSTGRES_PORT + "/" + POSTGRES_DB_NAME;

        String dbUrl = POSTGRES_JDBC;
        System.out.println(dbUrl);

        Connection connection = DriverManager.getConnection("jdbc:calcite:caseSensitive=true");
        CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
        SchemaPlus rootSchema = calciteConnection.getRootSchema();

        SqlFunction udf  = new SqlFunction("imageClassifier",
                SqlKind.OTHER_FUNCTION,
                ReturnTypes.INTEGER,
                null,
                OperandTypes.INTEGER,
                SqlFunctionCategory.USER_DEFINED_FUNCTION);

        SqlStdOperatorTable operatorTables = SqlStdOperatorTable.instance();
        operatorTables.register(udf);

        DataSource ds = JdbcSchema.dataSource(dbUrl, "org.postgresql.Driver", "myusername", "mypassword");
        rootSchema.add("SCHEMA", JdbcSchema.create(rootSchema, "test", ds, null, null));
        rootSchema.add("imageClassifier", ScalarFunctionImpl.create(TestingAPI.class, "imageClassifier"));
        for (String tableName : rootSchema.getSubSchemaNames()) {
            System.out.println(tableName);
        }
        for (String tableName : rootSchema.getSubSchema("SCHEMA").getTableNames()) {
            System.out.println(tableName);
        }
        SqlParser.Config parserConfig = SqlParser.config();
        parserConfig.withCaseSensitive(true);
        parserConfig.withUnquotedCasing(Casing.UNCHANGED);
        parserConfig.withQuotedCasing(Casing.UNCHANGED);


        Frameworks.ConfigBuilder config = Frameworks.newConfigBuilder()
                .defaultSchema(rootSchema)
                .parserConfig(parserConfig)
                .operatorTable(operatorTables)
                .context(Contexts.of(calciteConnection.config()));

        Planner planner = Frameworks.getPlanner(config.build());
        SqlNode node = planner.parse(QUERY);
//        SqlNode validateNode = planner.validate(node);
        SqlParser parser = SqlParser.create(QUERY, parserConfig);
        node = parser.parseQuery();
        SqlNode validateNode = planner.validate(node);
        System.out.println(node.toString());
        System.out.println();
        RelNode relationalPlan = planner.rel(validateNode).project();
        System.out.println(relationalPlan.explain());
        System.out.println(relationalPlan.getRelTypeName());

//        SqlToRelConverter converter;


//        SqlWriter writer = new SqlPrettyWriter();
//        validateNode.unparse(writer, 0,0);
//
//        // Print out our formatted SQL to the console
//        System.out.println(ImmutableList.of(writer.toSqlString().getSql()));
    }


    @Test
    public void testing() throws Exception {
        POSTGRES_PASSWORD = "mypassword";
        POSTGRES_USERNAME = "myusername";
        POSTGRES_HOST = "136.145.77.83";
        POSTGRES_PORT = 5434;
        POSTGRES_DB_NAME = "test";

        POSTGRES_JDBC = "jdbc:postgresql://" + POSTGRES_HOST + ":" + POSTGRES_PORT + "/" + POSTGRES_DB_NAME;

        String dbUrl = POSTGRES_JDBC;
        System.out.println(dbUrl);
        Properties properties = new Properties();
        properties.setProperty(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), Boolean.TRUE.toString());
        properties.setProperty(CalciteConnectionProperty.QUOTED_CASING.camelName(), Casing.UNCHANGED.name());
        properties.setProperty(CalciteConnectionProperty.UNQUOTED_CASING.camelName(), Casing.UNCHANGED.name());
        CalciteConnectionConfig config = new CalciteConnectionConfigImpl(properties);


        Connection con = DriverManager.getConnection(dbUrl, "myusername", "mypassword");
        Statement stmt1 = con.createStatement();
        con.close();

        Connection connection = DriverManager.getConnection("jdbc:calcite:");

        CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
        SchemaPlus rootSchema = calciteConnection.getRootSchema();
        DataSource ds = JdbcSchema.dataSource(dbUrl, "org.postgresql.Driver", "myusername", "mypassword");
        rootSchema.add("SCHEMA", JdbcSchema.create(rootSchema, "test", ds, null, null));

        for (String tableName : rootSchema.getSubSchemaNames()) {
            System.out.println(tableName);
        }
        for (String tableName : rootSchema.getSubSchema("SCHEMA").getTableNames()) {
            System.out.println(tableName);
        }

//        SqlParser.Config caseSensitiveParser = SqlParser.configBuilder().setUnquotedCasing(Casing.UNCHANGED).setQuotedCasing(Casing.UNCHANGED).setCaseSensitive(true).build();
//        FrameworkConfig frameworkConfig = Frameworks.newConfigBuilder()
//                .parserConfig(caseSensitiveParser)
//                .defaultSchema(rootSchema)
//                .build();
//        Planner planner = Frameworks.getPlanner(frameworkConfig);
//
//        SimpleQueryPlanner queryPlanner = new SimpleQueryPlanner(connection.getRootSchema().getSubSchema(connection.getSchema()));
//        RelNode loginalPlan = queryPlanner.getLogicalPlan("select product from orders");
//        System.out.println(RelOptUtil.toString(loginalPlan));
//
//        String query = "select * from SCHEMA.lineitem";
//        SqlNode sqlNode = planner.parse(query);
//        SqlNode sqlNodeValidated = planner.validate(sqlNode);
//        RelRoot relRoot = planner.rel(sqlNodeValidated);
//        RelNode relNode = relRoot.project();
//        final RelWriter relWriter = new RelWriterImpl(new PrintWriter(System.out), SqlExplainLevel.ALL_ATTRIBUTES, false);
//        relNode.explain(relWriter);
//
////        PreparedStatement run = RelRunners.run(relNode);
//        RelRunner runner = connection.unwrap(RelRunner.class);
//        ResultSet resultSet = runner.prepareStatement(relNode).executeQuery();
//
////        Statement stmt3 = connection.createStatement();
////        ResultSet rs = stmt3.executeQuery("select * from SCHEMA.lineitem");
    }

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
