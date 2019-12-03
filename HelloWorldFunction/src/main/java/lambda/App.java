package lambda;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.*;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<Object, Object> {

    public Object handleRequest(final Object input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        if (input != null && context != null) {
            String requestBodyJsonString = ((Map<String,String>) input).get("body");
            JsonObject jsonObject = new JsonParser().parse(requestBodyJsonString).getAsJsonObject();
            JsonArray jsonArray = jsonObject.getAsJsonArray("array");
            int scalar =  jsonObject.getAsJsonPrimitive("scalar").getAsInt();

            List<Double> arrayList = new ArrayList<>();
            for (JsonElement item: jsonArray){
                arrayList.add(item.getAsDouble());
            }

            double[] array = arrayList.stream().mapToDouble(d -> d).toArray();
            RealMatrix matrix = MatrixUtils.createRowRealMatrix(array);

            String resultMatrixAsString = new Gson().toJson(matrix.scalarMultiply(scalar).getRow(0));

            String output = "{ \"result\": " + resultMatrixAsString + "}";
            return new GatewayResponse(output, headers, 200);

        } else {
            return new GatewayResponse("{}", headers, 500);
        }
    }

    private String getPageContents(String address) throws IOException{
        URL url = new URL(address);
        try(BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
}
