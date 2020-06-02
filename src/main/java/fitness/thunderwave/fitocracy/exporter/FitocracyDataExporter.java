package fitness.thunderwave.fitocracy.exporter;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public class FitocracyDataExporter implements Callable<Integer> {
	
    @ArgGroup(validate = false, heading = "Get Data from Fitocracy using API%n")
    FitocracyAPI fitocracyAPI;

    static class FitocracyAPI {
    	
        @Option(names = {"-a", "--api-calls"}, description = "Make API calls to Fitocracy to get workout data in .json format.")
        boolean isUsingApi = false;
        
        @Option(names = {"-s", "--session-id"}, description = "Your active session id.  ie.  ", required = true)
        String sessionId;
        
        @Option(names = {"-d", "--start-date"}, defaultValue = "2012-01-01", description = "Date to start getting your workouts from. In YYYY-MM-DD format.  ie.  2013-02-21, Defaults to: ${DEFAULT-VALUE}")
        String startDateString;
        
        @Option(names = {"-u", "--api-base-url"}, defaultValue = "https://www.fitocracy.com/api/v2", description = "The base url for Fitocracy's api.  Defaults to: ${DEFAULT-VALUE}")
        String fitocracyBaseApiUrl;
        
        // @Option(names = "-x", defaultValue = "123", description = "Default: ${DEFAULT-VALUE}")

    }

    @ArgGroup(validate = false, heading = "Process CSV from local JSON files%n")
    CsvOnly csvOnly;

    static class CsvOnly {
        @Option(names = {"-c", "--csv-only"}, description = "Only do the csv file based on local .json files.")
        boolean isUsingCsvOnly = false;

    }
    
    @Parameters(index = "0", description = "Your active fitocracy user id.")
    String fitocracyUserId;

    @Option(names = {"-f", "--folder"}, description = "The folder that data is exported to.  Defaults to exports/")
    String folder = "exports";
    

	public static void main(String[] args) throws IOException {
		int exitCode = new CommandLine(new FitocracyDataExporter()).execute(args);
        System.exit(exitCode);
	}
	
	
	
    @Override
    public Integer call()  { 
    	
    	FitocracyApiManager apiManager = new FitocracyApiManager();
    	
    	if(csvOnly != null && csvOnly.isUsingCsvOnly) {
    		
    		try {
				apiManager.processCSVFileOnly(fitocracyUserId, folder);
			} catch (Exception e) {
    			System.err.println(e.getMessage());
    			e.printStackTrace();
    			return -1;
			}
    		
    	} else if(fitocracyAPI != null && fitocracyAPI.isUsingApi) {
    		try {
    			Date startDate = FitocracyApiManager.DATE_FORMATTER.parse(fitocracyAPI.startDateString);
    	    	apiManager.getWorkouts(fitocracyUserId, fitocracyAPI.sessionId, fitocracyAPI.fitocracyBaseApiUrl, startDate, folder);
    		} catch (Exception e) {
    			System.err.println(e.getMessage());
    			e.printStackTrace();
    			return -1;
    		}
    	}

        return 0;
    }

}
