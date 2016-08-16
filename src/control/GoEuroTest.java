package control;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.Location;

public class GoEuroTest {

	public static void main(String[] args) {

		final Logger slf4jLogger = LoggerFactory.getLogger(GoEuroTest.class
				.getName());

		if (args != null && args.length > 0 && args[0] != null
				&& !args[0].isEmpty()) {
			String city = args[0];
			LocationService locationService = new LocationService();
			List<Location> locationList = locationService.callLocationApi(city);
			if (locationList != null && !locationList.isEmpty())
				locationService.createLocationCSV(locationList);
		} else {
			slf4jLogger.error("City name must be passed as first parameter!");
		}
	}
}
