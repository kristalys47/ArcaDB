package coordinator;

import org.apache.calcite.adapter.enumerable.EnumerableRules;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.hep.HepPlanner;
import org.apache.calcite.plan.hep.HepProgram;
import org.apache.calcite.plan.hep.HepProgramBuilder;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.prepare.PlannerImpl;
import org.apache.calcite.prepare.Prepare;
import org.apache.calcite.rel.RelCollationTraitDef;
import org.apache.calcite.rel.RelDistributionTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.metadata.DefaultRelMetadataProvider;
import org.apache.calcite.rel.rules.*;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.dialect.HiveSqlDialect;
import org.apache.calcite.sql.dialect.PostgresqlSqlDialect;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.calcite.sql.util.SqlOperatorTables;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.sql2rel.StandardConvertletTable;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;


import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.plan.*;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.type.OperandTypes;
import org.apache.calcite.sql.type.ReturnTypes;
import org.apache.calcite.tools.*;
import org.apache.hadoop.security.SaslOutputStream;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class TestingAPI {
    static public int imageClassifier(int a){
        return a+2;
    }

    @Test
    public void calciteWithClass() throws Exception {
        Commons.POSTGRES_PASSWORD = "mypassword";
        Commons.POSTGRES_USERNAME = "myusername";
        Commons.POSTGRES_HOST = "136.145.77.83";
        Commons.POSTGRES_PORT = 5434;
        Commons.POSTGRES_DB_NAME = "test";
        Commons.REDIS_HOST = "136.145.77.83";
        Commons.REDIS_PORT = 6379;

        Commons.POSTGRES_JDBC = "jdbc:postgresql://" + Commons.POSTGRES_HOST + ":" + Commons.POSTGRES_PORT + "/" + Commons.POSTGRES_DB_NAME;

        //        String QUERY = "select product from SCHEMA.\"orders\"";
//        String QUERY = "select * from SCHEMA.\"orders\"";
//        String QUERY = "select p.\"05\" from SCHEMA.\"part\" as p, SCHEMA.\"lineitem\" as l where l.\"01\" = p.\"00\" and p.\"05\" > 250 ";
//        String QUERY = "select * from SCHEMA.\"part\" as p, SCHEMA.\"lineitem\" as l where l.\"01\" = p.\"00\" and p.\"05\" > 250 ";
//        String QUERY = "select * from SCHEMA.\"part\", SCHEMA.\"lineitem\" where \"lineitem\".\"01\" = \"part\".\"00\"";
//        String QUERY = "select * from SCHEMA.\"images\" where imageClassifier(\"gender\")=0";

        String QUERY = "select \"gender\", \"id\" from Schema.\"images\" as a inner join Schema.\"customer\" as b on (a.\"id\"=b.\"01\") where a.\"gender\"=0";
//        String QUERY = "select \"gender\", \"id\" from Schema.\"images\" as a where a.\"gender\"=0";

//        String QUERY = "select \'gender\', \'id\' from Schema.\"images\" as a where a.\"gender\"=0";

//        String QUERY = "select imageClassifier(\"gender\") from SCHEMA.\"images\" where \"gender\"=0";
//        String QUERY = "select * from SCHEMA.\"part\" where imageClassifier(\"01\")>0 ";
//        CalciteOptimizer.run(QUERY);
        System.out.println(QUERY);
        boolean hello = Controller.handleRequest(QUERY, 3, 60);
    }

    @Test
    public void calciteStufftest2() throws Exception {
        Commons.POSTGRES_PASSWORD = "mypassword";
        Commons.POSTGRES_USERNAME = "myusername";
        Commons.POSTGRES_HOST = "136.145.77.83";
        Commons.POSTGRES_PORT = 5434;
        Commons.POSTGRES_DB_NAME = "test";
//        String QUERY = "select product from SCHEMA.\"orders\"";
//        String QUERY = "select * from SCHEMA.\"orders\"";
        String QUERY = "select * from SCHEMA.\"part\", SCHEMA.\"lineitem\" where \"lineitem\".\"01\" = \"part\".\"00\"";
//        String QUERY = "select imageClassifier(\"01\") from SCHEMA.\"part\"";
//        String QUERY = "select * from SCHEMA.\"part\" where imageClassifier(\"01\")>0 ";

        Commons.POSTGRES_JDBC = "jdbc:postgresql://" + Commons.POSTGRES_HOST + ":" + Commons.POSTGRES_PORT + "/" + Commons.POSTGRES_DB_NAME;

        String dbUrl = Commons.POSTGRES_JDBC;

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
        Commons.POSTGRES_PASSWORD = "mypassword";
        Commons.POSTGRES_USERNAME = "myusername";
        Commons.POSTGRES_HOST = "136.145.77.83";
        Commons.POSTGRES_PORT = 5434;
        Commons.POSTGRES_DB_NAME = "test";
//        String QUERY = "select product from SCHEMA.\"orders\"";

//        String QUERY = "select * from SCHEMA.\"part\", SCHEMA.\"lineitem\" where \"lineitem\".\"01\" = \"part\".\"00\"";
//        String QUERY = "select imageClassifier(\"01\") from SCHEMA.\"part\"";
        String QUERY = "select * from SCHEMA.\"part\" where imageClassifier(\"01\")>0 ";

        Commons.POSTGRES_JDBC = "jdbc:postgresql://" + Commons.POSTGRES_HOST + ":" + Commons.POSTGRES_PORT + "/" + Commons.POSTGRES_DB_NAME;
        // Rules
        RuleSet rules = RuleSets.ofList(CoreRules.JOIN_ASSOCIATE, CoreRules.JOIN_COMMUTE, CoreRules.JOIN_COMMUTE, CoreRules.JOIN_COMMUTE_OUTER, CoreRules.MULTI_JOIN_OPTIMIZE_BUSHY, CoreRules.JOIN_EXTRACT_FILTER);

        String dbUrl = Commons.POSTGRES_JDBC;
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




//
//
//
//        SqlParser.Config parserConfig = SqlParser.config();
//        parserConfig.withCaseSensitive(true);
//        parserConfig.withUnquotedCasing(Casing.UNCHANGED);
//        parserConfig.withQuotedCasing(Casing.UNCHANGED);
//
//        Frameworks.ConfigBuilder config2 = Frameworks.newConfigBuilder()
//                .defaultSchema(rootSchema)
//                .ruleSets(rules)
//                .parserConfig(parserConfig)
//                .operatorTable(operatorTables)
//                .context(Contexts.of(calciteConnection.config()));
//
//        Planner planner1 = Frameworks.getPlanner(config2.build());
//        SqlNode node = planner1.parse(QUERY);
//        SqlNode validateNode = planner1.validate(node);
//        RelNode relationalPlan = planner1.rel(validateNode).project();
//        System.out.println(relationalPlan.explain());



        
    }

    @Test
    public void testing() throws Exception {
        Commons.POSTGRES_PASSWORD = "mypassword";
        Commons.POSTGRES_USERNAME = "myusername";
        Commons.POSTGRES_HOST = "136.145.77.83";
        Commons.POSTGRES_PORT = 5434;
        Commons.POSTGRES_DB_NAME = "test";

        Commons.POSTGRES_JDBC = "jdbc:postgresql://" + Commons.POSTGRES_HOST + ":" + Commons.POSTGRES_PORT + "/" + Commons.POSTGRES_DB_NAME;

        String dbUrl = Commons.POSTGRES_JDBC;
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

    @Test
    public void calciteStufftest3() throws Exception {
        Commons.POSTGRES_PASSWORD = "mypassword";
        Commons.POSTGRES_USERNAME = "myusername";
        Commons.POSTGRES_HOST = "136.145.77.83";
        Commons.POSTGRES_PORT = 5434;
        Commons.POSTGRES_DB_NAME = "test";
        String QUERY = "select \"gender\", \"id\" from Schema.\"images\" as a inner join Schema.\"customer\" as b on (a.\"id\"=b.\"01\") where a.\"gender\"=0";


        Commons.POSTGRES_JDBC = "jdbc:postgresql://" + Commons.POSTGRES_HOST + ":" + Commons.POSTGRES_PORT + "/" + Commons.POSTGRES_DB_NAME;

        String dbUrl = Commons.POSTGRES_JDBC;

        Connection connection = DriverManager.getConnection("jdbc:calcite:caseSensitive=true");
        CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
        SchemaPlus rootSchema = calciteConnection.getRootSchema();

        DataSource ds = JdbcSchema.dataSource(dbUrl, "org.postgresql.Driver", "myusername", "mypassword");
//        SqlDialect jdbcDialect = JdbcSchema.createDialect(SqlDialectFactoryImpl.INSTANCE, ds);
//        SqlDialect jdbcDialect =  HiveSqlDialect.DEFAULT;
//        String dialectName = jdbcDialect.getClass().getName();
//        System.out.println(dialectName);

        rootSchema.add("SCHEMA", JdbcSchema.create(rootSchema, "test", ds, null, null));


        RuleSet rules = RuleSets.ofList(

//                CoreRules.PROJECT_INTERPRETER_TABLE_SCAN,
                EnumerableRules.ENUMERABLE_PROJECT_RULE,
                EnumerableRules.ENUMERABLE_FILTER_RULE,
//                CoreRules.JOIN_ASSOCIATE,
//                CoreRules.JOIN_COMMUTE,
//                CoreRules.JOIN_COMMUTE,
//                CoreRules.JOIN_COMMUTE_OUTER,
//                CoreRules.MULTI_JOIN_OPTIMIZE_BUSHY,
                CoreRules.FILTER_INTO_JOIN,
                CoreRules.PROJECT_TABLE_SCAN,
//                CoreRules.FILTER_MERGE,
//                CoreRules.FILTER_TO_CALC,
//                CoreRules.PROJECT_TO_CALC,
//                CoreRules.FILTER_CALC_MERGE,
//                CoreRules.PROJECT_CALC_MERGE,
//                CoreRules.JOIN_PROJECT_BOTH_TRANSPOSE,
//                CoreRules.JOIN_PUSH_EXPRESSIONS,
                CoreRules.FILTER_SCAN,
//                CoreRules.JOIN_CONDITION_PUSH,
//                CoreRules.PROJECT_FILTER_TRANSPOSE_WHOLE_EXPRESSIONS,
                CoreRules.JOIN_PUSH_TRANSITIVE_PREDICATES
//                CoreRules.FILTER_TABLE_FUNCTION_TRANSPOSE,
//                CoreRules.FILTER_VALUES_MERGE,
//                EnumerableRules.ENUMERABLE_TABLE_SCAN_RULE
//                EnumerableRules.ENUMERABLE_CALC_RULE,
//                EnumerableRules.ENUMERABLE_AGGREGATE_RULE
//                CoreRules.JOIN_EXTRACT_FILTER
        );

        final List<RelTraitDef> traitDefs = new ArrayList<RelTraitDef>();

        traitDefs.add(ConventionTraitDef.INSTANCE);

        SqlParser.Config sqlconfig = SqlParser.Config.DEFAULT;
        RelDataTypeFactory typeFactory = new JavaTypeFactoryImpl();

        final FrameworkConfig config = Frameworks.newConfigBuilder()
                .parserConfig(sqlconfig)
                .defaultSchema(rootSchema)
                .ruleSets(rules)
                .traitDefs(traitDefs)
                .typeSystem(RelDataTypeSystem.DEFAULT)
                .build();

        HepProgramBuilder builder = new HepProgramBuilder();
        HepPlanner hepPlanner = new HepPlanner(builder.build());
        hepPlanner.addRule(CoreRules.FILTER_SCAN);
        hepPlanner.addRule(CoreRules.FILTER_INTO_JOIN);
        hepPlanner.addRule(CoreRules.PROJECT_TABLE_SCAN);



        Planner planner = Frameworks.getPlanner(config);

        SqlNode sqlNode = planner.parse(QUERY);
        System.out.println(sqlNode);

        SqlToRelConverter.Config sql2relconfig = SqlToRelConverter.config();
        Properties configProperties = new Properties();

        configProperties.put(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), Boolean.TRUE.toString());
        configProperties.put(CalciteConnectionProperty.UNQUOTED_CASING.camelName(), Casing.UNCHANGED.toString());
        configProperties.put(CalciteConnectionProperty.QUOTED_CASING.camelName(), Casing.UNCHANGED.toString());

        CalciteConnectionConfig configCal = new CalciteConnectionConfigImpl(configProperties);

        PlannerImpl plannerImpl = new PlannerImpl(Frameworks
                .newConfigBuilder()
                .sqlToRelConverterConfig(sql2relconfig)
                .parserConfig(sqlconfig)
                .build());
        SqlTypeFactoryImpl factory = new SqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT);
        RexBuilder rexBuilder = new RexBuilder(typeFactory);
        RelOptCluster cluster = RelOptCluster.create(hepPlanner, rexBuilder);
        CalciteSchema cschema = CalciteSchema.createRootSchema(false,false);
        cschema.add("SCHEMA", rootSchema);
        Prepare.CatalogReader catalogReader = new CalciteCatalogReader(
                cschema,
                Collections.singletonList("SCHEMA"),
                typeFactory,
                configCal
        );


        final SqlStdOperatorTable instance = SqlStdOperatorTable.instance();
        SqlValidator validator = SqlValidatorUtil.newValidator(SqlOperatorTables.chain(instance, catalogReader),
                catalogReader, factory, SqlValidator.Config.DEFAULT.withIdentifierExpansion(true));


        SqlToRelConverter sql2rel = new SqlToRelConverter(
                plannerImpl,
                validator,
                catalogReader,
                cluster,
                StandardConvertletTable.INSTANCE,
                sql2relconfig);

        RelNode relNode = sql2rel.convertQuery(sqlNode, true, true).rel;


        RelOptPlanner planner2 = relNode.getCluster().getPlanner();
        planner2.setRoot(relNode);
        RelNode bestExp = planner2.findBestExp();
        System.out.println("This is the best?");
        System.out.println(bestExp.explain());




//
//
//        Planner planner = Frameworks.getPlanner(config);
//        SqlNode parse1 = planner.parse(QUERY);
//
//        SqlNode validate = planner.validate(parse1);
//
//        RelNode done = planner.rel(validate).project();
//        System.out.println(done.explain());

    }

}
