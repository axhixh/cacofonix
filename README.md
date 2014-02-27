#Cacofonix

A UDP server to collect, store and query time series metrics.

Yes, it is reinventing the wheel and there are better tools that does this better.

## Loading
Each new observation consists of the time series (metric) name, timestamp and a value. Each of these are sent to the server as a UDP packet. The component of the metric are separated by space.

```metric.name timestamp value```

 - metric.name is the name of the time series
 - timestamp is Unix timestamp in milli-seconds
 - value is a double value


The project contains two examples:

 1. a shell script to send machine load using netcat
 2. DemoClient which sends random values as time series using Java code
 
## Querying
Cacofonix provides very basic querying of time series using a HTTP interface.

HTTP GET to ```http://cacofonix:9002/api/metrics/``` returns a list of metrics stored in the system.

You can do a HTTP GET to ```http://cacofonix:9002/api/metrics/metric.name?start=unix.timestamp&end=unix.timestamp```

This returns the values of the time series with name 'metric.name' between start and end timestamps. The values are returned as tab separated values.

## To Do
This is a project for investigation and research. Will be looking into:

 - aggregation
 - anomaly detection
 - forecasting
 
 
 
