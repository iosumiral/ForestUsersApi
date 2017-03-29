package com.nicepeopleatwork.forest.exceptions;

import com.nicepeopleatwork.forest.conf.Configuration;

public class AttributeNumberException extends Exception
{
	public AttributeNumberException (  )
	{
		super ( message() );
	}
	
	public static String message()
	{
		StringBuilder str = new StringBuilder();
		int numValues = Configuration.DEFAULT_ATTRIBUTES.length + 1;
		str.append ( "Incorrect number of values introduced. Required " + numValues + " values:\n" );
		str.append ( "revisited, " );
		for ( String s : Configuration.DEFAULT_ATTRIBUTES )
		{
			str.append ( ", " + s );
		}
		str.append ( "." );
		return str.toString ( );
	}
}
