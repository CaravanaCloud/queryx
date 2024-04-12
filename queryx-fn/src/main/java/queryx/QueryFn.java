package queryx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import static java.util.Base64.*;
import static java.util.Collections.*;
import static java.util.Optional.*;
import static queryx.QueryUtils.urlPing;

@Named("qx-fn")
public class QueryFn implements RequestHandler<Map<String,Object>, Map<String,String>> {
    @Inject
    ObjectMapper mapper;

    @Inject
    QueryConfig cfg;

    @Inject
    DataSource ds;

    @PostConstruct
    public void init(){
        Log.info("Starting lambda health check");
        QueryUtils.lambdaCheck(cfg.checkURL());
    }

    @Override
    public Map<String, String> handleRequest(Map<String, Object> input, Context context) {
        try {
            Log.info("Query invocation requested");
            // Log.info(input);
            var queriesParam = input.get("queries");
            var queries = resolveQueries(queriesParam);
            var data = executeQueries(queries);
            var result = Map.of(
                    "data", data.toString(),
                    "status","ok");
            return result;
        } catch (Exception e) {

            return Map.of("status", "error",
                    "error_message", e.getMessage());
        }
    }

    private List<String> resolveQueries(Object queriesParam) {
        var queries = new ArrayList<String>();
        var queriesCfg = decodeQueries();
        queries.addAll(queriesCfg);
        if (queriesParam instanceof String) {
            queries.add((String) queriesParam);
        } else if (queriesParam instanceof List) {
            queries.addAll((List<String>) queriesParam);
        }
        return queries;
    }

    private String executeQueries(List<String> queries) throws JsonProcessingException {
        Log.infof("Executing [%s] queries", queries.size());
        List<List<Map<String,String>>> results = new ArrayList<>();
        queries.forEach(query -> {
            try {
                var result = executeQuery(query);
                results.add(result);
            } catch (SQLException|JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        var result = mapper.writeValueAsString(results);
        return result;
    }



    private List<String> decodeQueries() {
        var result = decodeQueries64()
                .map(queriesStr -> queriesStr.split(","))
                .map(Arrays::asList)
                .orElse(emptyList());
        return result;
    }

    private Optional<String> decodeQueries64() {
        return ofNullable(cfg.queries64())
                .map(getDecoder()::decode)
                .map(String::new);
    }

    private List<Map<String,String>> executeQuery(String query) throws SQLException, JsonProcessingException {
        try (var conn = ds.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(query)) {
            return rsToList(rs);
        }
    }

    public List<Map<String,String>> rsToList(ResultSet rs) throws SQLException, JsonProcessingException {
        Map<String, Integer> columns = new HashMap<>();
        List<Map<String,String>> rows = new ArrayList<>();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        while (rs.next()) {
            Map<String, String> row = new HashMap<>();
            for (int i = 1; i <= columnCount; ++i) {
                var label = rsmd.getColumnLabel(i);
                var type = rsmd.getColumnType(i);
                var value = switch (type) {
                    case Types.INTEGER -> String.valueOf(rs.getInt(i));
                    case Types.VARCHAR -> rs.getString(i);
                    case Types.BIT -> rs.getBoolean(i) ? "1" : "0";
                    case Types.TIMESTAMP -> rs.getTimestamp(i).toString();
                    case Types.DATE -> rs.getDate(i).toString();
                    case Types.TIME -> rs.getTime(i).toString();
                    case Types.OTHER -> rs.getObject(i).toString();
                    default -> rs.getString(i);
                };
                row.put(label, value);
            }
            rows.add(row);
        }
        return rows;
    }


}
