package coordinator;

import org.apache.calcite.adapter.enumerable.EnumerableRules;
import org.apache.calcite.adapter.jdbc.JdbcConvention;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.rel.RelCollationTraitDef;
import org.apache.calcite.rel.RelDistributionTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.metadata.DefaultRelMetadataProvider;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.dialect.PostgresqlSqlDialect;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;


import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.plan.*;
import org.apache.calcite.rel.rules.CoreRules;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.type.OperandTypes;
import org.apache.calcite.sql.type.ReturnTypes;
import org.apache.calcite.tools.*;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import javax.swing.text.html.StyleSheet;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;

import static coordinator.Commons.*;
import static coordinator.Commons.POSTGRES_DB_NAME;

public class TestingAPI {
    static public int imageClassifier(int a){
        return a+2;
    }

    @Test
    public void calciteWithClass() throws Exception {
        TestingVolcano.run();
    }

    @Test
    public void calciteStufftest2() throws Exception {
        POSTGRES_PASSWORD = "mypassword";
        POSTGRES_USERNAME = "myusername";
        POSTGRES_HOST = "136.145.77.83";
        POSTGRES_PORT = 5434;
        POSTGRES_DB_NAME = "test";
//        String QUERY = "select product from SCHEMA.\"orders\"";
        String QUERY = "select * from SCHEMA.\"orders\"";
//        String QUERY = "select * from SCHEMA.\"part\", SCHEMA.\"lineitem\" where \"lineitem\".\"01\" = \"part\".\"00\"";
//        String QUERY = "select imageClassifier(\"01\") from SCHEMA.\"part\"";
//        String QUERY = "select * from SCHEMA.\"part\" where imageClassifier(\"01\")>0 ";

        POSTGRES_JDBC = "jdbc:postgresql://" + POSTGRES_HOST + ":" + POSTGRES_PORT + "/" + POSTGRES_DB_NAME;

        String dbUrl = POSTGRES_JDBC;

        Connection connection = DriverManager.getConnection("jdbc:calcite:caseSensitive=true");
        CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
        SchemaPlus rootSchema = calciteConnection.getRootSchema();

        DataSource ds = JdbcSchema.dataSource(dbUrl, "org.postgresql.Driver", "myusername", "mypassword");
        SqlDialect jdbcDialect = JdbcSchema.createDialect(SqlDialectFactoryImpl.INSTANCE, ds);
        String dialectName = jdbcDialect.getClass().getName();
        System.out.println(dialectName);

        rootSchema.add("SCHEMA", JdbcSchema.create(rootSchema, "test", ds, null, null));


//        rootSchema.add("imageClassifier", ScalarFunctionImpl.create(TestingAPI.class, "imageClassifier"));
        for (String tableName : rootSchema.getSubSchemaNames()) {
            System.out.println(tableName);
        }
        for (String tableName : rootSchema.getSubSchema("SCHEMA").getTableNames()) {
            System.out.println(tableName);
        }


        final FrameworkConfig config = Frameworks.newConfigBuilder()
                .parserConfig(SqlParser.Config.DEFAULT)
                .defaultSchema(rootSchema)
                .build();
        Planner planner = Frameworks.getPlanner(config);
        SqlNode parse1 = planner.parse(QUERY);

        SqlNode validate = planner.validate(parse1);

        RelRoot root = planner.rel(validate);


//        SqlToRelConverter.Config sqlToRelConverterConfig = SqlToRelConverter.configBuilder(0)


        VolcanoPlanner  vplanner = new VolcanoPlanner();
        vplanner.addRelTraitDef(RelDistributionTraitDef.INSTANCE);
        vplanner.addRelTraitDef(ConventionTraitDef.INSTANCE);
        vplanner.addRelTraitDef(RelCollationTraitDef.INSTANCE);
        vplanner.addRule(CoreRules.JOIN_COMMUTE);
        vplanner.addRule(CoreRules.FILTER_TO_CALC);
        vplanner.addRule(CoreRules.PROJECT_TO_CALC);
        vplanner.addRule(CoreRules.FILTER_CALC_MERGE);
        vplanner.addRule(CoreRules.PROJECT_TO_CALC);
//        vplanner.addRule(EnumerableRules.ENUMERABLE_TABLE_SCAN_RULE);
//        vplanner.addRule(CoreRules.FILTER_TO_CALC);
//        vplanner.addRule(CoreRules.FILTER_TO_CALC);
//        vplanner.addRule(CoreRules.FILTER_TO_CALC);
//        vplanner.addRule(CoreRules.FILTER_TO_CALC);

        RelOptCluster relOptCluster = RelOptCluster.create(vplanner, new RexBuilder(new SqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT)));
        relOptCluster.setMetadataProvider(DefaultRelMetadataProvider.INSTANCE);


        RelNode relNode = root.rel;
        vplanner.setRoot(relNode);
        RelNode finalNode = vplanner.findBestExp();
        System.out.println(finalNode);


//
//        RelNode result = vplanner.chooseDelegate().findBestExp();







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
//        String QUERY = "select imageClassifier(\"01\") from SCHEMA.\"part\"";
        String QUERY = "select * from SCHEMA.\"part\" where imageClassifier(\"01\")>0 ";

        POSTGRES_JDBC = "jdbc:postgresql://" + POSTGRES_HOST + ":" + POSTGRES_PORT + "/" + POSTGRES_DB_NAME;
        // Rules
        RuleSet rules = RuleSets.ofList(CoreRules.JOIN_ASSOCIATE, CoreRules.JOIN_COMMUTE, CoreRules.JOIN_COMMUTE, CoreRules.JOIN_COMMUTE_OUTER, CoreRules.MULTI_JOIN_OPTIMIZE_BUSHY, CoreRules.JOIN_EXTRACT_FILTER);

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

        //SQL PARSER
        SqlParser.Config parserConfig = SqlParser.config();
        parserConfig.withCaseSensitive(true);
        parserConfig.withUnquotedCasing(Casing.UNCHANGED);
        parserConfig.withQuotedCasing(Casing.UNCHANGED);

        Frameworks.ConfigBuilder config = Frameworks.newConfigBuilder()
                .defaultSchema(rootSchema)
                .ruleSets(rules)
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

        Properties configProperties = new Properties();

        configProperties.put(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), Boolean.TRUE.toString());
        configProperties.put(CalciteConnectionProperty.UNQUOTED_CASING.camelName(), Casing.UNCHANGED.toString());
        configProperties.put(CalciteConnectionProperty.QUOTED_CASING.camelName(), Casing.UNCHANGED.toString());

        CalciteConnectionConfig configOpti = new CalciteConnectionConfigImpl(configProperties);

        VolcanoPlanner optiPlanner = new VolcanoPlanner();
        optiPlanner.addRelTraitDef(ConventionTraitDef.INSTANCE);
        optiPlanner.addRelTraitDef(RelDistributionTraitDef.INSTANCE);
        // add rules
        optiPlanner.addRule(CoreRules.JOIN_ASSOCIATE);
        optiPlanner.addRule(CoreRules.JOIN_COMMUTE);
        optiPlanner.addRule(CoreRules.JOIN_COMMUTE_OUTER);
        optiPlanner.addRule(CoreRules.MULTI_JOIN_OPTIMIZE_BUSHY);
        optiPlanner.addRule(CoreRules.JOIN_EXTRACT_FILTER);
        optiPlanner.addRule(CoreRules.JOIN_ASSOCIATE);

        SqlTypeFactoryImpl factorySTFI = new SqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT);


        // add ConverterRule

        //CoreRules.JOIN_ASSOCIATE, CoreRules.JOIN_COMMUTE, CoreRules.JOIN_COMMUTE, CoreRules.JOIN_COMMUTE_OUTER, CoreRules.MULTI_JOIN_OPTIMIZE_BUSHY, CoreRules.JOIN_EXTRACT_FILTER

//        VolcanoPlanner optiPlanner = new VolcanoPlanner(RelOptCostImpl.FACTORY, Contexts.of(configOpti));
        optiPlanner.addRelTraitDef(ConventionTraitDef.INSTANCE);
        RelNode finalls = optiPlanner.findBestExp();



//        runOptimizer.run(optiPlanner, relationalPlan, relationalPlan.getTraitSet(), Collections.emptyList(), Collections.emptyList());








        
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
