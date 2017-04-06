package com.nicepeopleatwork.forest.exceptions;

import com.nicepeopleatwork.forest.conf.ForestConfiguration;

public class AttributeNumberException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AttributeNumberException (  )
	{
		super ( message() );
	}
	
	public static String message()
	{
		StringBuilder str = new StringBuilder();
		int numValues = ForestConfiguration.getATTRIBUTES ( ).length + 1;
		str.append ( "Incorrect number of values introduced. Required " + numValues + " values:\n" );
		str.append ( "revisited, " );
		for ( String s : ForestConfiguration.getATTRIBUTES ( ) )
		{
			str.append ( ", " + s );
		}
		str.append ( "." );
		return str.toString ( );
	}
}
