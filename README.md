# Fitocracy Workout Exporter 

To use this application, you need a java runtime environment and little bit of knowledge with the command line.

There are 3 values that you need for this to work.

1) Your Fitocracy Id 
2) Your current active session Id
3) The date you started Fitocracy


### For the Fitocracy Id:

Easiest way is to get your browser on the desktop in development mode and hit you main profile page.  Each web browser, slightly different to get into this mode.  You want to see the network connections (http).

In the network section of the development tool, you should see a user id for yourself when access your profile page.

Like:

https://www.fitocracy.com/activity_stream/0/?user_id=12345678

Where your user_id=12345678

### For the Session Id

Next get the session id, this is for your active session you have with fitocracy.  It's stored as a cookie for the website.  It will look something like this. 

sessionid=14d01ff07ba43393aeebd0d662ab5665
 
### Start Date to start the scan
Finally, you need a date to start with.  Typically, the day that you joined Fitocracy.


### Example Running the application:
command-line:

> java -jar bin/fitocracy-exporter-0.1.jar 12345678 "sessionid=14d01ff07ba43393aeebd0d662ab5665" "2019-11-25"

this will generate json files in the format of:  
> 12345678_2019-12-01.json

Then an overall CSV file, that can be used in a spreadsheet.
> 12345678.csv