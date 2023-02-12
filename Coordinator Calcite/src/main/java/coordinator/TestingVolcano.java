package coordinator;

import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableRules;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.Contexts;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptCostImpl;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.prepare.Prepare;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.rules.CoreRules;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.type.OperandTypes;
import org.apache.calcite.sql.type.ReturnTypes;
import org.apache.calcite.sql.util.ChainedSqlOperatorTable;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.sql2rel.StandardConvertletTable;
import org.apache.calcite.tools.Program;
import org.apache.calcite.tools.Programs;
import org.apache.calcite.tools.RuleSet;
import org.apache.calcite.tools.RuleSets;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static coordinator.Commons.*;
import static coordinator.Commons.POSTGRES_JDBC;

public class TestingVolcano {
    static public int IMAGECLASSIFIER(int a){
        return a+2;
    }
    public static void run() throws Exception {
        Properties configProperties = new Properties();

        configProperties.put(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), Boolean.TRUE.toString());
        configProperties.put(CalciteConnectionProperty.UNQUOTED_CASING.camelName(), Casing.UNCHANGED.toString());
        configProperties.put(CalciteConnectionProperty.QUOTED_CASING.camelName(), Casing.UNCHANGED.toString());

        CalciteConnectionConfig config = new CalciteConnectionConfigImpl(configProperties);

        RelDataTypeFactory typeFactory = new JavaTypeFactoryImpl();

//____________________________________________________________________

        POSTGRES_PASSWORD = "mypassword";
        POSTGRES_USERNAME = "myusername";
        POSTGRES_HOST = "136.145.77.83";
        POSTGRES_PORT = 5434;
        POSTGRES_DB_NAME = "test";
//        String QUERY = "select product from SCHEMA.\"orders\"";
//        String QUERY = "select * from SCHEMA.\"orders\"";
//        String QUERY = "select * from SCHEMA.\"part\", SCHEMA.\"lineitem\" where \"lineitem\".\"01\" = \"part\".\"00\"";
//        String QUERY = "select imageClassifier(\"01\") from SCHEMA.\"part\"";
        String QUERY = "select * from SCHEMA.\"part\" where imageClassifier(\"01\")>0 ";

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
        rootSchema.add("imageClassifier", ScalarFunctionImpl.create(TestingVolcano.class, "IMAGECLASSIFIER"));

        //______________________________________________________________

        VolcanoPlanner planner = new VolcanoPlanner(RelOptCostImpl.FACTORY,Contexts.of(config));
        planner.addRelTraitDef(ConventionTraitDef.INSTANCE);
        RelOptCluster cluster = RelOptCluster.create(planner, new RexBuilder(typeFactory));
        SqlToRelConverter.Config converterConfig = SqlToRelConverter.config();
        CalciteSchema cschema = CalciteSchema.createRootSchema(false,false);
        cschema.add("SCHEMA", rootSchema);
        Prepare.CatalogReader catalogReader = new CalciteCatalogReader(
                cschema,
                Collections.singletonList("SCHEMA"),
                typeFactory,
                config
        );

        SqlFunction udf  = new SqlFunction("imageClassifier",
                SqlKind.OTHER_FUNCTION,
                ReturnTypes.INTEGER,
                null,
                OperandTypes.INTEGER,
                SqlFunctionCategory.USER_DEFINED_FUNCTION);

        SqlStdOperatorTable operatorTables = SqlStdOperatorTable.instance();
        operatorTables.register(udf);

//        SqlOperatorTable operatorTable = SqlStdOperatorTable.instance();

        SqlValidator.Config validatorConfig = SqlValidator.Config.DEFAULT
                .withLenientOperatorLookup(config.lenientOperatorLookup())
                .withDefaultNullCollation(config.defaultNullCollation())
                .withIdentifierExpansion(true);
        SqlValidator validator = SqlValidatorUtil.newValidator(
                operatorTables,
                catalogReader,
                typeFactory,
                validatorConfig
        );

        SqlNode sqlNode = parse(QUERY);
        SqlNode validatedSqlNode = validator.validate(sqlNode);
        System.out.println(validatedSqlNode.toString());

        SqlToRelConverter converter = new SqlToRelConverter(null, validator, catalogReader, cluster, StandardConvertletTable.INSTANCE, converterConfig);



        RelRoot root = converter.convertQuery(validatedSqlNode, false, true);
        RelNode relationalExpression = root.rel;
        RuleSet rules = RuleSets.ofList(CoreRules.JOIN_ASSOCIATE,
                CoreRules.JOIN_COMMUTE, CoreRules.JOIN_COMMUTE,
                CoreRules.JOIN_COMMUTE_OUTER, CoreRules.MULTI_JOIN_OPTIMIZE_BUSHY,
                CoreRules.FILTER_INTO_JOIN,
                CoreRules.FILTER_TO_CALC,
//                CoreRules.PROJECT_TO_CALC,
//                CoreRules.FILTER_CALC_MERGE,
//                CoreRules.PROJECT_CALC_MERGE,
                EnumerableRules.ENUMERABLE_TABLE_SCAN_RULE,
                EnumerableRules.ENUMERABLE_PROJECT_RULE,
                EnumerableRules.ENUMERABLE_FILTER_RULE,
                EnumerableRules.ENUMERABLE_CALC_RULE,
                EnumerableRules.ENUMERABLE_AGGREGATE_RULE,
                CoreRules.JOIN_EXTRACT_FILTER);
        Program program = Programs.of(RuleSets.ofList(rules));

        System.out.println(relationalExpression.explain());
        RelNode optimizerRelTree = program.run(
                planner,
                relationalExpression,
                relationalExpression.getTraitSet().plus(EnumerableConvention.INSTANCE),
                Collections.emptyList(),
                Collections.emptyList()
        );

        System.out.println(optimizerRelTree.explain());


    }
    public static SqlNode parse(String sql) throws Exception {
        SqlParser.Config parserConfig = SqlParser.config();
        parserConfig.withQuotedCasing(Casing.UNCHANGED);
        parserConfig.withCaseSensitive(true);
        parserConfig.conformance();
        parserConfig.withUnquotedCasing(Casing.UNCHANGED);

        SqlParser parser = SqlParser.create(sql, parserConfig);

        return parser.parseStmt();
    }
}
