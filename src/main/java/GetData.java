import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetData {

    final private static String patch_number = "8.6.1";
    final private static String champion_url = "http://ddragon.leagueoflegends.com/cdn/" + patch_number + "/data/en_US/champion.json";
    final private static String individual_url = "http://ddragon.leagueoflegends.com/cdn/" + patch_number + "/data/en_US/champion/";

    private static final String MYSQLSERVERURL = "testdbinstance.co70d8yjkhnh.us-east-2.rds.amazonaws.com";
    private static final String DBNAME = "Champions";
    private static String USERNAME = "";
    private static String PASSWORD = "";
    private static final String ENGINE = "mysql";

    private static String connector = "";
    private static String port = "";

    private static String championJson;
    private static ArrayList<String> titles = new ArrayList<>();
    private static ArrayList<String> champions = new ArrayList<>();
    private static ArrayList<String> q = new ArrayList<>();
    private static ArrayList<String> w = new ArrayList<>();
    private static ArrayList<String> e = new ArrayList<>();
    private static ArrayList<String> r = new ArrayList<>();
    private static ArrayList<String> passives = new ArrayList<>();
    private static ArrayList<Integer> attackRange = new ArrayList<>();

    private static int count;

    public static void main(String[] args) throws JSONException, IOException {
        File input = new File("info.txt");
        BufferedReader br = new BufferedReader(new FileReader(input));
        USERNAME = br.readLine();
        PASSWORD = br.readLine();

        championJson = "";
        count = 0;

        championJson = jsonRetriever(champion_url);
        grabChampionData();

        getURLProperties();
        String databaseUrl = connector + MYSQLSERVERURL + port + "test";

        //String databaseUrl = connector + MYSQLSERVERURL + port + "test" + seriously;


        connectToDatabase(databaseUrl);
    }

    private static void getURLProperties(){
        if(ENGINE.equals("postgres")){
            connector = "jdbc:postgresql://";
            port = ":5432/";
        }
        else if(ENGINE.equals("mysql")){
            connector = "jdbc:mysql://";
            port = ":3306/";
        }
    }

    private static void grabChampionData() throws JSONException {
        JSONObject obj = new JSONObject(championJson);
        JSONObject data = obj.getJSONObject("data");
        Iterator keys = data.keys();
        while(keys.hasNext()){
            Object key = keys.next();
            String name = data.getJSONObject((String) key).getString("name");
            champions.add(name);
            String id = data.getJSONObject((String) key).getString("id");
            abilityRetriever(individual_url + id + ".json", id);
        }
    }

    private static String jsonRetriever(String url){
        JsonParser getJson = new JsonParser(url);
        return  getJson.parseJson();
    }

    private static JSONObject getData(JSONObject champion, String id) throws JSONException {
        return champion.getJSONObject("data").getJSONObject(id);
    }

    private static JSONObject getStats(JSONObject champion, String id) throws JSONException {
        return getData(champion, id).getJSONObject("stats");
    }

    private static JSONArray getAbilities(JSONObject champion, String id) throws JSONException {
        return getData(champion, id).getJSONArray("spells");
    }

    private static void abilityRetriever(String url, String id) throws JSONException {
        JsonParser getJson = new JsonParser(url);
        String jsonString = getJson.parseJson();
        JSONObject champion = new JSONObject(jsonString);
        titles.add(getData(champion, id).getString("title"));
        passives.add(getData(champion, id).getJSONObject("passive").getString("name"));
        q.add(getAbilities(champion, id).getJSONObject(0).getString("name"));
        w.add(getAbilities(champion, id).getJSONObject(1).getString("name"));
        e.add(getAbilities(champion, id).getJSONObject(2).getString("name"));
        r.add(getAbilities(champion, id).getJSONObject(3).getString("name"));
        attackRange.add(getStats(champion, id).getInt("attackrange"));
        count++;
    }

    private static void executeStatement(Connection conn, String sqlStatement) throws SQLException {
        System.out.println("I be executin");
        Statement stmt = conn.createStatement();
        int ok = stmt.executeUpdate(sqlStatement);
    }

    private static void connectToDatabase(String url){
        String quoted = "";
        if(ENGINE.equals("postgres"))
            quoted = "$$";
        else if(ENGINE.equals("mysql")){
            quoted = "\"";
        }
        try (Connection conn = DriverManager.getConnection(url, USERNAME, PASSWORD)){
            System.out.println("Successful connection!");
            for(int i = 0; i < count; i++){
                String insert = "INSERT INTO champions(name, title, passive, q, w, e, r, attackRange) VALUES(" +
                        quoted + champions.get(i) + quoted + ", " +
                        quoted + titles.get(i) + quoted + ", " +
                        quoted + passives.get(i) + quoted + "," +
                        quoted + q.get(i) + quoted + ", " +
                        quoted + w.get(i) + quoted + ", " +
                        quoted + e.get(i) + quoted + ", " +
                        quoted + r.get(i) + quoted + ", " +
                        attackRange.get(i) +" );";
                executeStatement(conn, insert);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
