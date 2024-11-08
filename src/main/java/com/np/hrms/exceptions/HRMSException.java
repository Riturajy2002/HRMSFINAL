package com.np.hrms.exceptions;

public class HRMSException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String message;
	
	public HRMSException(String message) {
        super(message);
        this.message = message;
    }
	
	public String getMessage() {
		return message;
	}
}
