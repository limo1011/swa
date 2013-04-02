package de.shop.bestellverwaltung.service;

import de.shop.util.AbstractShopException;

public abstract class AbstractBestellungServiceException extends AbstractShopException {
	private static final long serialVersionUID = -626920099480136224L;

	public AbstractBestellungServiceException(String msg) {
		super(msg);
	}
}
