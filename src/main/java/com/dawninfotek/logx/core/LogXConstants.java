package com.dawninfotek.logx.core;

public interface LogXConstants {

    public static final String HEADER_NAME = "logx.header.name";
    public static final String FIELDS_NAME = "logx.fields";
	public static final String HEADER_INCLUDES = "logx.header.includes";
	public static final String MASK_KEYWORD = "logx.value.hash";
	public static final String JSON_LAYOUT_INCLUDES = "logx.custom.json.includes";
	public static final String REQUEST_HEADER = "REQUEST_HEADER";
	public static final String REQUST_PARAMETER = "REQUST_PARAMETER";
	public static final String SESSION_ID = "SESSION_ID";
	public static final String SESSION = "SESSION";
	public static final String REMOTEADDR = "REMOTEADDR";
	public static final String URL_MAPPINGS_INCLUDES = "logx.filter.urlmapping.includes";	
	public static final String URL_MAPPINGS_EXCLUDES = "logx.filter.urlmapping.excludes";
	
    public static final String MASK_NAME = "userName";
    public static final String UUID = "uuid";
    public static final String PROCESS_ID = "processId";
    public static final String SERVICE_NAME = "serviceName";
    public static final String APPLICATION_NAME = "applicationName";    
    public static final String PERFORMANCE_METRIC = "Performance_Metric";
    public static final String TRANSACTION_PATH = "transactionPath";
    public static final String PATH = "path";
    public static final String TX_PATH_PREFIX="txpath.";
	public static final String CURR_CHECKPOINT = "current_checkpoint";
	public static final String CHECKPOINT = "checkpoint";
    
    public static final String LOG_MSG_EVENT_BEGIN = "message.event.begin";
    public static final String LOG_MSG_EVENT_END = "message.event.end";    
    public static final String LOG_MSG_PFM_METRIC = "message.performance.metric";
    
    public static final String TYPE_A_S_C = "eventType.application.state.change";
    public static final String TYPE_A_C = "eventType.abnormal.condition";
    public static final String TYPE_SRV = "eventType.service";
    public static final String TYPE_B = "eventType.business"; 
    
    public static final String C_NAME_PREFIX = "component."; 
    public static final String RESOLVER_PREFIX = "resolver.";
    //component names
    public static final String C_NAME_CP = "CheckPointService";
    public static final String C_NAME_CFG = "Configuration";    
    public static final String C_NAME_EVT = "EventService";
    public static final String C_NAME_HASH = "HashService";
    public static final String C_NAME_MASK = "MaskService";    
}
