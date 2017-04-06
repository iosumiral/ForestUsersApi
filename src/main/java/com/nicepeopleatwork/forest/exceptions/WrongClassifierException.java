package com.nicepeopleatwork.forest.exceptions;

public class WrongClassifierException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public WrongClassifierException()
	{
		super ( "The classifier that was loaded is the wrong one for this format." );
	}
}
