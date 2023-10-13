import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ReceiptSystem {
    public static void main(String[] args) throws IOException {
        Receipt receipt = new Receipt();
        receipt.readReceipt(System.in);
        receipt.printReceipt(System.out);
    }
}

class Receipt {
    Map<Boolean, List<Product>> productMap;

    public Receipt() {
        productMap = new HashMap<>();
    }

    public void readReceipt(InputStream in) throws IOException {
        BufferedReader bf = new BufferedReader(new InputStreamReader(in));
        String jsonLine = bf.lines()
                .takeWhile(line -> !line.contains("]")) // Read until ']' is encountered
                .collect(Collectors.joining("\n"));
        String cleaned = removeBrackets(jsonLine);
        String [] parts = cleaned.split("},");
        for(String part : parts){
            if(!part.endsWith("}")){
                part += "}";
            }
            Product product = Product.createProduct(part);
            boolean isDomestic = product.isDomestic();
            productMap.computeIfAbsent(isDomestic, k -> new ArrayList<>())
                    .add(product);
        }
    }
    public  String removeBrackets(String input) {
        if (input.startsWith("[") || input.endsWith("]")) {
            return input.substring(1, input.length() - 1);
        }
        return input;
    }
    public void printReceipt(OutputStream out){
        PrintWriter pw = new PrintWriter(out);
        boolean key = true;
        List<Product> domesticProducts = productMap.get(true);
        Collections.sort(domesticProducts, new ProductNameComparator());
        for(Product product : domesticProducts){
            if(key){
                pw.println(". Domestic");
                pw.flush();
                key = false;
            }
            printProduct(product, out, true);
        }
        key = true;
        List<Product> importedProducts = productMap.get(false);
        Collections.sort(importedProducts, new ProductNameComparator());
        for(Product product : importedProducts){
            if(key){
                pw.println(". Imported");
                pw.flush();
                key = false;
            }
            printProduct(product, out, false);
            pw = new PrintWriter(out);
            pw.println(String.format("Domestic cost: $%.1f", domesticProducts.stream().mapToDouble(x -> x.price).sum()));
            pw.println(String.format("Imported cost: $%.1f", importedProducts.stream().mapToDouble(x -> x.price).sum()));
            pw.println(String.format("Domestic count: %d", domesticProducts.size()));
            pw.println(String.format("Imported count: %d", importedProducts.size()));
            pw.flush();
        }

    }
    public void printProduct(Product product, OutputStream out, boolean key){
        PrintWriter pw = new PrintWriter(out);

        pw.println(String.format("... %s", product.name));
        pw.println(String.format("    Price: $%.1f", product.price));
        StringBuilder description = new StringBuilder();
        if(product.description.length() <= 10){
            description.append(product.description);
        }else{
            description.append(product.description.substring(0,10));
            description.append("...");
        }
        pw.println(String.format("    %s", description));
        pw.println(String.format("    Weight: %s",product.weight));
        pw.flush();
    }

}
class Product {
    String name;
    boolean domestic;
    double price;
    String weight;
    String description;

    public Product(String name, boolean domestic, double price, String weight, String description) {
        this.name = name;
        this.domestic = domestic;
        this.price = price;
        this.weight = weight;
        this.description = description;
    }

    public static Product createProduct(String productData) {
        JSONObject jsonObject;
        String name = "";
        boolean domestic = false;
        double price = 0;
        String weight = "N/A";
        String description = "N/A";
        try {
            jsonObject = (JSONObject) new JSONParser().parse(productData);
            name = (String) jsonObject.get("name");
            domestic = (boolean) jsonObject.get("domestic");
            price = (double) jsonObject.get("price");
            Object weightObj = jsonObject.get("weight");
            if (weightObj != null) {
                if (weightObj instanceof Number) {
                    weight = ((Number) weightObj).toString();
                }
            }
            description = (String) jsonObject.get("description");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Product(name, domestic, price, weight, description);
    }

    public boolean isDomestic() {
        return domestic;
    }

    public String getName() {
        return name;
    }
}
class ProductNameComparator implements Comparator<Product> {
    @Override
    public int compare(Product p1, Product p2) {
        return p1.getName().compareToIgnoreCase(p2.getName());
    }
}
