package control;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.GeoPosition;
import data.Location;

public class LocationService {

	private final Logger slf4jLogger = LoggerFactory
			.getLogger(LocationService.class.getName());

	public List<Location> callLocationApi(String city) {

		List<Location> locationList = null;

		try {
			URI uri = new URI("http", "api.goeuro.com",
					"/api/v2/position/suggest/en/" + city, null);
			String apiEndpoint = uri.toASCIIString();

			CloseableHttpClient httpclient = HttpClients.createDefault();

			slf4jLogger.info("Calling Location API at {}", apiEndpoint);

			HttpGet httpget = new HttpGet(apiEndpoint);
			CloseableHttpResponse response = null;

			response = httpclient.execute(httpget);

			slf4jLogger.info("Processing Location API response");

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			String jsonString = null;
			String line = null;

			StringBuilder sb = new StringBuilder();
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}

			jsonString = sb.toString();

			if (jsonString != null && !jsonString.isEmpty()) {

				locationList = new ArrayList<Location>();

				JSONArray locations = new JSONArray(jsonString);

				if (locations.length() == 0) {
					slf4jLogger.warn("No matches found for {}", city);
				}

				for (int i = 0; i < locations.length(); i++) {
					if (locations.get(i) instanceof JSONObject) {

						JSONObject locationObj = (JSONObject) locations.get(i);

						Location location = new Location();
						GeoPosition geoPos = new GeoPosition();

						JSONObject geoPositionObj = locationObj
								.getJSONObject("geo_position");
						geoPos.setLatitude(geoPositionObj.getDouble("latitude"));
						geoPos.setLongitude(geoPositionObj
								.getDouble("longitude"));

						location.setCoreCountry(locationObj
								.getBoolean("coreCountry"));
						location.setCountry(locationObj.getString("country"));
						location.setCountryCode(locationObj
								.getString("countryCode"));
						location.setDistance(locationObj.getString("distance"));
						location.setFullName(locationObj.getString("fullName"));
						location.setGeoPosition(geoPos);
						location.setIataCode(locationObj
								.getString("iata_airport_code"));
						location.setId(locationObj.getString("_id"));
						location.setInEurope(locationObj.getBoolean("inEurope"));
						location.setKey(locationObj.getString("key"));
						location.setLocationId(locationObj.has("location_id") ? locationObj
								.getString("location_id") : "");
						location.setName(locationObj.getString("name"));
						location.setType(locationObj.getString("type"));

						locationList.add(location);

					} else {
						slf4jLogger
								.warn("Unexpected element found in Location API response: {}",
										locations.get(i));
					}
				}
			}
			response.close();

		} catch (ClientProtocolException e) {
			slf4jLogger.error(e.getMessage());
		} catch (IOException e) {
			slf4jLogger.error(e.getMessage());
		} catch (JSONException e) {
			slf4jLogger.error(e.getMessage());
		} catch (URISyntaxException e) {
			slf4jLogger.error(e.getMessage());
		}

		return locationList;
	}

	public void createLocationCSV(List<Location> locationList) {

		String newLineSeparator = "\n";
		String fileName = "goEuroTest.csv";

		slf4jLogger.info("Creating {} from the retrieved locations", fileName);

		FileWriter fileWriter = null;

		try {

			fileWriter = new FileWriter(fileName);

			addCsvField(fileWriter, "_id");
			addCsvField(fileWriter, "key");
			addCsvField(fileWriter, "name");
			addCsvField(fileWriter, "fullName");
			addCsvField(fileWriter, "iata_airport_code");
			addCsvField(fileWriter, "type");
			addCsvField(fileWriter, "country");
			addCsvField(fileWriter, "latitude");
			addCsvField(fileWriter, "longitude");
			addCsvField(fileWriter, "location_id");
			addCsvField(fileWriter, "inEurope");
			addCsvField(fileWriter, "countryCode");
			addCsvField(fileWriter, "coreCountry");
			addCsvField(fileWriter, "distance");

			fileWriter.append(newLineSeparator);

			for (Location location : locationList) {

				addCsvField(fileWriter, location.getId());
				addCsvField(fileWriter, location.getKey());
				addCsvField(fileWriter, location.getName());
				addCsvField(fileWriter, location.getFullName());
				addCsvField(fileWriter, location.getIataCode());
				addCsvField(fileWriter, location.getType());
				addCsvField(fileWriter, location.getCountry());
				addCsvField(
						fileWriter,
						location.getGeoPosition() != null ? String
								.valueOf(location.getGeoPosition()
										.getLatitude()) : "");
				addCsvField(
						fileWriter,
						location.getGeoPosition() != null ? String
								.valueOf(location.getGeoPosition()
										.getLongitude()) : "");
				addCsvField(fileWriter, location.getLocationId());
				addCsvField(fileWriter, String.valueOf(location.isInEurope()));
				addCsvField(fileWriter, location.getCountryCode());
				addCsvField(fileWriter,
						String.valueOf(location.isCoreCountry()));
				addCsvField(fileWriter, location.getDistance());

				fileWriter.append(newLineSeparator);
			}
			fileWriter.flush();
			fileWriter.close();

			slf4jLogger.info("{} created", fileName);

		} catch (Exception e) {
			slf4jLogger.error(e.getMessage());
		}
	}

	public void addCsvField(FileWriter fileWriter, String value)
			throws IOException {
		String delimiter = ";";
		fileWriter.append(value);
		fileWriter.append(delimiter);
	}
}
