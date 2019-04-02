# Log Plus

LogPlus is a framework to extend current java log framework, The goal of LogPlus is to support json format output while adding additional fields into logs with minimium change to application source code, LogPlus implemented json layout for [logback](https://logback.qos.ch/) and [log4j](https://logging.apache.org/log4j/) (both log4j1.2 & log4j2). It's pretty cool, and extendable with most java log frameworks.

This framework provides features which can be used in different purposes, it currently supports logback(slf4j), log4j2, log4j1.2.17, some of features only support for java web application.

## features
1. integrate with servlet, extend servlet dofilter [dofilter function](https://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/Filter.html) to get extra message from http request with logplus-web component [LogPlusWeb](https://github.com/DawnInfoTekInc/LogPlusWeb)
2. provide application & service level traceability, meanwhile traceability feature set performance check point inside application/service level
3. security, for sensitive information MaskService provide a way to mask string with designed pattern, also HashService hide indicated key from http request with hash method
4. event, EventService is designed for the purpose of tracing, continuously trace activities/actions happened/generated inside production service
5. multithread support, logplus supports multithread application by passing application context to child thread so that tracibility can extend to child thread as well, remember to clean threadlocal (LogPlusUtils.clearThreadContext();), see [sample code](#sample-code).
6. formatted output logs, All output logs are in nice JSON format with customized fields, both in pattern configure or json layout.

## usage
1. requirement:
    1. for logback packages (logback-classic, logback-core, slf4j-api) with json output static configure in pattern, or with jsonlayout dynamic configure, see example.
    2. for log4j1.2.17 packages (log4j1.2.17, slf4j-api, slf4j-log4j12), see example.
    3. for log4j2 packages (log4j-api, log4j-core, slf4j-api, log4j-slf4j-impl) with json output static configure in pattern, or with jsonlayout dynamic configure, see example.
    4. commons-lang
    5. commons-beanutils
    
2. configuration:
    1. [logback.xml](src/test/resources/logback.xml) file, change to your desire, a sample for each usage provided in this repository
    2. [web.xml](web.xml) file, add listener, filter, filter-mapping, context-param for customized config
    3. customized config file (see [logplus.properties](logplus.properties) as an example), default config file (see [logplus-default.properties](src/main/resources/logplus-default.properties)) will be used if no config property file exist/find 
    
    ---
    > LogPlus puts some special key value in the logs based on the LogPlus property configuration file, 
    the configuration file includes field name, field value - related to logback pattern filed
    > - security clause (logplus.value.hash)
    > - logplus clause (logplus.header.name, logplus.header.includes, logplus.json.fields.custom)
    > - fields clauss (applicationName, serviceName, etc...)
    > - message clause (message.event.begin, message.event.end, message.performance.metric)
    > - component clause (configable components)
    > - resolver clause (configable resolver)
    > - eventType clause (operational.connection, abnormal, transaction, business)
    > - txpath (transaction path)
    
3. trace API, for the purpose of passing traceable field to different applications/services set logplus value into header with key and value, or call checkpoint interface from source code, [a sample](src/test/java/com/dawninfotek/AppTest.java) for calling checkpoint.
> eg. connection = url.openConnection(); 
>
> connection.setRequestProperty(LogPlusUtils.getLogPlusHeaderName(), LogPlusUtils.getLogPlusHeaderValue());

## examples
### configuration
1. with logback:
    1. with static json config in pattern:
    > - add jars: logback-classic, logback-core, slf4j-api, logplus, logplus-web(with filter&listener), commons-beanutils, commons-lang
    > - config logplus.properties, no need to change/add logplus-default.properties, customize logplus.properties to override logplus-default.properties
    > - config web.xml to add listener, filter, custom config file logplus.properties. by default, it will try to find config file from class path, and filter everything under root "/"
    > - config logback.xml, see sample [logback.xml](src/test/resources/logback.xml)

    2. with jsonlayout dynamic config:
    > - add jars: logback-classic, logback-core, slf4j-api, logplus, logplus-web(with filter&listener), commons-beanutils, commons-lang
    > - config logplus.properties, no need to change/add logplus-default.properties, customize logplus.properties to override logplus-default.properties
    > - config web.xml to add listener, filter, custom config file logplus.properties. by default, it will try to find config file from class path, and filter everything under root "/"
    > - config logback.xml, see sample [logback.xml](src/test/resources/logback_json_layout.xml)

2. with log4j1.2.17:
    1. with static json config in pattern:
    > - add jars: log4j1.2.17, slf4j-api, slf4j-log4j12, logplus, logplus-web(with filter&listener), commons-beanutils, commons-lang
    > - config logplus.properties, no need to change/add logplus-default.properties, customize logplus.properties to override logplus-default.properties
    > - config web.xml to add listener, filter, custom config file logplus.properties. by default, it will try to find config file from class path, and filter everything under root "/"
    > - config logback.xml, see sample [log4j.properties](src/test/resources/log4j.properties)

    2. with jsonlayout dynamic config:
    > - add jars: log4j1.2.17, slf4j-api, slf4j-log4j12, logplus, logplus-web(with filter&listener) commons-beanutils, commons-lang
    > - config logplus.properties, no need to change/add logplus-default.properties, customize logplus.properties to override logplus-default.properties
    > - config web.xml to add listener, filter, custom config file logplus.properties. by default, it will try to find config file from class path, and filter everything under root "/"
    > - config logback.xml, see sample [log4j.properties](src/test/resources/log4j_json_layout.properties)

3. with log4j2 (currently we do not support log4j2.8.2, with slight source change you can make it support 2.8.2):
    1. with static json config in pattern:
    > - add jars: log4j-api, log4j-core, slf4j-api, log4j-slf4j-impl, logplus, logplus-web(with filter&listener), commons-beanutils, commons-lang
    > - config logplus.properties, no need to change/add logplus-default.properties, customize logplus.properties to override logplus-default.properties
    > - config web.xml to add listener, filter, custom config file logplus.properties. by default, it will try to find config file from class path, and filter everything under root "/"
    > - config logback.xml, see sample [log4j2.properties](src/test/resources/log4j2.properties)

    2. with jsonlayout dynamic config:
    > - add jars: log4j-api, log4j-core, slf4j-api, log4j-slf4j-impl, logplus, logplus-web(with filter&listener), commons-beanutils, commons-lang
    > - config logplus.properties, no need to change/add logplus-default.properties, customize logplus.properties to override logplus-default.properties
    > - config web.xml to add listener, filter, custom config file logplus.properties. by default, it will try to find config file from class path, and filter everything under root "/"
    > - config logback.xml, see sample [log4j2.xml](src/test/resources/log4j2_json_layout.xml)
### sample code ###
1. if set logplus.system.inheritable.fields=true (see [logplus-default.properties](src/main/resources/logplus-default.properties)), see below sample code on how to pass context manually 
~~~~
> Spring @Async
Code in the Async method
    @Async
    public Future<String> asyncGetProfile(String key, Map<String, String> threadContext) {
        LogPlusUtils.initThreadContext(threadContext);          
            try {
                    logger.info("asyncGetProfile() is called ...");
                    Thread.sleep(500);
                    logger.info("asyncGetProfile() done ...");
                }catch (Exception e) {
                    logger.error("Somthing went wrong",e);
                }finally {
                    LogPlusUtils.clearThreadContext();
                }       
                return new AsyncResult<String>("How are you!");
              }


Code in Parent Thread
    Future<String> f = profileService.asyncGetProfile(null, LogPlusUtils.getCopyOfThreadContext());
        String r = f.get();


> Java Worker
Parent Thread:
    try{                      
        workmanager.schedule(new MyWorker("", LogPlusUtils.getCopyOfThreadContext()));
    }catch (Exception e) {
        logger.error("",e);
    }                           

Work:
public MyWorker(String id, Map<String, String> logPlusContext){
    super();
    this.id = id;
    this.logPlusContext = logPlusContext;
}             

@Override
public void run() {
    LogPlusUtils.initThreadContext(logPlusContext);
    logger.info("Async Thread started ..." + id);
    try{
        Thread.sleep(500);
        logger.info("Async Thread done ...");                                    
    }catch (Exception e) {
        logger.error("",e);
    }finally{
        LogPlusUtils.clearThreadContext();                                       
    }
}
~~~~

## License

see the [LICENSE](LICENSE) file for license rights and limitations (GNU GPLv3)
