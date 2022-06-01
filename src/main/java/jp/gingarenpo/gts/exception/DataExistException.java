package jp.gingarenpo.gts.exception;

public class DataExistException extends GTSException {
	
	public DataExistException() {
	}
	
	public DataExistException(String message) {
		super(message);
	}
	
	public DataExistException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public DataExistException(Throwable cause) {
		super(cause);
	}
	
	public DataExistException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
