# Log Plus

LogPlus is a framework to extend current java log framework, LogPlus is based on [logback](https://logback.qos.ch/) framework.
It's pretty cool, and extendable with most java log framework.

This framework provides features which can be used in different purposes.

## features
1. integrate with servlet, override servlet [dofilter function](https://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/Filter.html) to get extra message from http request
2. provide checkpoint service, checkpoint service set performance check point inside application
3. security, for sensitive information MaskService provide a way to mask string with designed pattern, also HashService hide indicated key from http request with hash method
4. event, EventService is designed for the purpose of tracing, continuously trace activities/actions happened/generated inside production service

## usage
1. requirement:
    1. logback packages (logback-classic, logback-core, slf4j-api)
    2. commons-beanutils
    3. commons-codec ( 1.4 or above)
    4. commons-lang
    
2. configuration:
    1. [logback.xml](src/main/resources/logback.xml) file, change to your desire, an example provided in this repository
    2. [web.xml](src/main/resources/web.xml) file, add listener, filter, filter-mapping, context-param for customized config
    3. customized config file (see [logx.properties](logx.properties) as an example), default config file (see [logx-default.properties](src/main/resources/logx-default.properties)) will be used if no config property file exist/find 
    
    ---
    > LogPlus puts some special key value in to the log based on the property configuration file, 
    the configuration file includes field name, field value - related to logback pattern filed
    > - service name
    > - logx clause (logx.header.name, logx.header.includes, logx.value.hash)
    > - message clause (message.event.begin, message.event.end, message.performance.metric)
    > - eventType clause (operational.connection, abnormal, transaction, business)
    > - txpath (transaction path)
    
3. http header, for the purpose of passing traceable field to different tier set logx value into header with key and value,
> eg. connection = url.openConnection(); 
> connection.setRequestProperty(LogXUtils.getLogXHeaderName(), LogXUtils.getLogXHeaderValue());

## License

see the [LICENSE](LICENSE) file for license rights and limitations (GNU GPLv3)