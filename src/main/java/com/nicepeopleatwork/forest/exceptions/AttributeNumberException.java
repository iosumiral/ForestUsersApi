package com.nicepeopleatwork.forest.exceptions;

import com.nicepeopleatwork.forest.conf.ForestConfiguration;

public class AttributeNumberException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AttributeNumberException ( int num )
	{
		super ( message( num ) );
	}
	
	public static String message( int num )
	{
		StringBuilder str = new StringBuilder();
		int numValues = ForestConfiguration.getATTRIBUTES ( ).length + 1;
		str.append ( "Incorrect number of values introduced. Required " + numValues + " values:\n" );
		str.append ( "revisited" );
		for ( String s : ForestConfiguration.getATTRIBUTES ( ) )
		{
			str.append ( ", " );
			str.append ( s );
		}
		str.append ( ".\nReceived " );
		str.append ( num );
		str.append ( "." );
		return str.toString ( );
	}
}
