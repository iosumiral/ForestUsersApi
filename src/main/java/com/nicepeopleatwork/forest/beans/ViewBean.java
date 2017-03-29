package com.nicepeopleatwork.forest.beans;

import java.util.ArrayList;
import java.util.Arrays;

import com.nicepeopleatwork.forest.conf.Configuration;
import com.nicepeopleatwork.forest.exceptions.AttributeNumberException;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;

public class ViewBean
{
	private double[] values;
	
	/**
	 * Create a viewBean from an array of doubles
	 * @param values
	 * @throws AttributeNumberException if the length of the array is not the same as the number of attributes (plus the revisit)
	 */
	public ViewBean ( double[] values ) throws AttributeNumberException
	{
		this.values = values;
		if ( values.length != Configuration.DEFAULT_ATTRIBUTES.length + 1 )
			throw new AttributeNumberException (  );
	}

	public Instance getInstance ( )
	{
		return new DenseInstance ( 1 , values );
	} 
	
	public static ArrayList < Attribute > attributes ( )
	{
		// add the attributes names to the database
		ArrayList < Attribute > attributes = new ArrayList < Attribute > ( );
		// class value
		attributes.add ( new Attribute ( "revisited" , Arrays.asList ( "0" , "1" ) ) );
		for ( String attName : Configuration.DEFAULT_ATTRIBUTES ) attributes.add ( new Attribute ( attName ) );
		return attributes;
	}
}
