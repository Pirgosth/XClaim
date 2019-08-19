package com.pirgosth.claimPlugin;

@SuppressWarnings("serial")
public class MissingPluginException extends Exception{
	public MissingPluginException(String message) {
		super(message);
	}
}