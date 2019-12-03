package fitness.thunderwave.fitocracy.exporter;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public class FitocracyDataExporter implements Callable<Integer> {

    @Parameters(index = "0", description = "Your active fitocracy user id.")
    private String fitocracyUserId;
	
    @Parameters(index = "1", description = "Your active session id.")
    private String sessionId;
    
    @Parameters(index = "2", description = "Date to start getting your workouts from. In YYYY-MM-DD format.  ie.  2013-02-21")
    private String startDateString;
    
    @Option(names = {"-a", "--api-base-url"}, description = "The base url for Fitocracy's api.  Defaults to https://www.fitocracy.com/api/v2")
    private String fitocracyBaseApiUrl = "https://www.fitocracy.com/api/v2";

	public static void main(String[] args) throws IOException {
		int exitCode = new CommandLine(new FitocracyDataExporter()).execute(args);
        System.exit(exitCode);
	}
	
	
	
    @Override
    public Integer call() throws Exception { 
		
		Date startDate = FitocracyApiManager.DATE_FORMATTER.parse(startDateString);
    	
    	FitocracyApiManager apiManager = new FitocracyApiManager();
    	apiManager.getWorkouts(fitocracyUserId, sessionId, fitocracyBaseApiUrl, startDate);

        return 0;
    }

}
