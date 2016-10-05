import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Main {
	static String steamID = "76561197997176853";
	static String steamKey = "039C11BAEA5ECDA457D4BF520B63CE6D";

	public static void main(String[] args) {
		System.out.println("My steam id");

		String fetchAllFriendsURL = "http://api.steampowered.com/ISteamUser/GetFriendList/v0001/?key=" + steamKey
				+ "&steamid=" + steamID + "&relationship=friend&format=json";
		try {

			String sURL = fetchAllFriendsURL; // just a string

			// Connect to the URL using java's native library
			URL url = new URL(sURL);
			HttpURLConnection request = (HttpURLConnection) url.openConnection();
			request.connect();

			// Convert to a JSON object to print data
			JsonParser jp = new JsonParser(); // from gson
			JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); // Convert
																									// the
																									// input
																									// stream
																									// to
																									// a
																									// json
																									// element
			JsonObject rootobj = root.getAsJsonObject(); // May be an array, may
															// be an object.
			String steamID64 = "";
			try {
				JsonArray test = rootobj.get("friendslist").getAsJsonObject().get("friends").getAsJsonArray();
				System.out.println(test.size());
				for (int i = 0; i < test.size(); i++) {
					steamID64 = steamID64 + "," + test.get(i).getAsJsonObject().get("steamid").getAsString();
					System.out.println(steamID64);
				}
				Boolean check = writeDetails(steamID64);
			} catch (Exception e) {
				System.out.println("errored");
			}

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static Boolean writeDetails(String steamID64s) {
		String fetchFriendDataURL = "http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=" + steamKey
				+ "&steamids=" + steamID64s + "&format=json";

		// Connect to the URL using java's native library
		URL urlDetail = null;
		try {
			urlDetail = new URL(fetchFriendDataURL);
			HttpURLConnection requestDetail = (HttpURLConnection) urlDetail.openConnection();
			requestDetail.connect();
			// Convert to a JSON object to print data
			JsonParser jp2 = new JsonParser(); // from gson
			JsonElement root2 = jp2.parse(new InputStreamReader((InputStream) requestDetail.getContent())); // Convert
																											// the
																											// input
																											// stream
																											// to
																											// a
																											// json
																											// element
			JsonObject rootobj2 = root2.getAsJsonObject(); // May be an array,
															// may be an object.
			String realName = "x";
			String personaName = "";
			Connection connection = null;
			try {

				Class.forName("com.mysql.jdbc.Driver");
				connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sys", "localAdmin", "password");
				System.out.println("Connected to database");
				Statement stmt3 = connection.createStatement();
				String sql = "delete from sys.steam_ids";
				stmt3.executeUpdate(sql);
			} catch (Exception e) {
			}
			try {
				JsonArray temp = rootobj2.get("response").getAsJsonObject().get("players").getAsJsonArray();
				System.out.println(temp.size());
				for (int j = 0; j < temp.size(); j++) {
					String steamid = temp.get(j).getAsJsonObject().get("steamid").getAsString();
					personaName = temp.get(j).getAsJsonObject().get("personaname").getAsString();
					try {
						realName = temp.get(j).getAsJsonObject().get("realname").getAsString();
					} catch (Exception e) {
						System.out.println("realname was not defined");
					}
					String insertTableSQL = "INSERT INTO steam_ids values"
							+ "(?,?,?)";

					PreparedStatement stmt2 = connection.prepareStatement(insertTableSQL);
					stmt2.setString(1, steamid);
					stmt2.setString(2, personaName);
					stmt2.setString(3, realName);
					stmt2.executeUpdate();

				}
				Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery("select * from sys.steam_ids");
				while (rs.next()) {
					System.out.println(rs.getString(1) + "  " + rs.getString(2));
				}
				connection.close();

			} catch (Exception e) {
				System.out.println(e);
				return false;
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}

		return true;
	}

}
