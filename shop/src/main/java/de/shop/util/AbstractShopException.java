package de.shop.util;

public abstract class AbstractShopException extends RuntimeException {
	private static final long serialVersionUID = -1236574998680316151L;

	public AbstractShopException(String msg) {
		super(msg);
	}
	
	public AbstractShopException(String msg, Throwable t) {
		super(msg, t);
	}
}
