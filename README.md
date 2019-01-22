# Log Plus

LogPlus is a framework to extend current java log framework, The goal of LogPlus is to support json format output while adding additional fields into logs with minimium change to application source code, LogPlus also implemented json layout for [logback](https://logback.qos.ch/) and [log4j](https://logging.apache.org/log4j/) (both log4j1.2 & log4j2). It's pretty cool, and extendable with most java log frameworks.

This framework provides features which can be used in different purposes, it currently supports logback, slf4j, log4j2, log4j1.2.17.

## features
1. integrate with servlet, extend servlet dofilter [dofilter function](https://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/Filter.html) to get extra message from http request
2. provide application & service level traceability, meanwhile traceability feature set performance check point inside application/service
3. security, for sensitive information MaskService provide a way to mask string with designed pattern, also HashService hide indicated key from http request with hash method
4. event, EventService is designed for the purpose of tracing, continuously trace activities/actions happened/generated inside production service
5. formatted output logs, All output logs are in nice JSON format with customized fields, both in pattern configure or json layout.

## usage
1. requirement:
    1. for logback packages (logback-classic, logback-core, slf4j-api)
    2. for log4j1.2.17 packages (log4j1.2.17, slf4j-api, slf4j-log4j12)
    3. for log4j2 packages (log4j-api, log4j-core, slf4j-api, log4j-slf4j-impl)
    4. commons-beanutils
    5. commons-codec ( 1.4 or above)
    6. commons-lang
    
2. configuration:
    1. [logback.xml](src/test/resources/logback.xml) file, change to your desire, a sample provided in this repository
    2. [web.xml](web.xml) file, add listener, filter, filter-mapping, context-param for customized config
    3. customized config file (see [logx.properties](logx.properties) as an example), default config file (see [logx-default.properties](src/main/resources/logx-default.properties)) will be used if no config property file exist/find 
    
    ---
    > LogPlus puts some special key value in the logs based on the logx property configuration file, 
    the configuration file includes field name, field value - related to logback pattern filed
    > - service name
    > - logx clause (logx.header.name, logx.header.includes, logx.value.hash)
    > - fields clauss (applicationName, serviceName, etc...)
    > - message clause (message.event.begin, message.event.end, message.performance.metric)
    > - component clause (configable components)
    > - resolver clause (configable resolver)
    > - eventType clause (operational.connection, abnormal, transaction, business)
    > - txpath (transaction path)
    
3. http header, for the purpose of passing traceable field to different applications/services set logx value into header with key and value, or call checkpoint interface from source code.
> eg. connection = url.openConnection(); 
>
> connection.setRequestProperty(LogXUtils.getLogXHeaderName(), LogXUtils.getLogXHeaderValue());

## License

see the [LICENSE](LICENSE) file for license rights and limitations (GNU GPLv3)
