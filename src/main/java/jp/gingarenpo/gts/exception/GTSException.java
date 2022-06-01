package jp.gingarenpo.gts.exception;

/**
 * GTS内独自で発生する例外の親元。基本的にはJava組み込みの例外を出すように設計しているが
 * どうしてもいい例外がない場合にここを継承して自作している
 */
public class GTSException extends Exception {
	
	public GTSException() {
	}
	
	public GTSException(String message) {
		super(message);
	}
	
	public GTSException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public GTSException(Throwable cause) {
		super(cause);
	}
	
	public GTSException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
