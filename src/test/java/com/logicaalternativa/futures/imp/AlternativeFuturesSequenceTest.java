/*
 * AlternativeFuturesSequenceTest.java
 * 
 * Copyright 2016 Miguel Rafael Esteban Martin <miguel.esteban@logicaalternativa.com>
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 * 
 * 
 */
package com.logicaalternativa.futures.imp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.logicaalternativa.futures.AlternativeFuture;

public class AlternativeFuturesSequenceTest {

	@Test
	public void testSequenceOk() throws Exception {
		
		// GIVEN
		
		final List<String> list = Stream.of("A", "B","C").collect( Collectors.toList() );
		
		final List<AlternativeFuture<String>> listFutures = list.stream()
						.map(s -> AlternativeFuture.successful( s ))
						.collect(Collectors.toList());
		
		// WHEN
		
		AlternativeFuture<List<String>> res = AlternativeFutures.sequence( listFutures );
		
		// THEN
		
		List<String> result = AwaitAlternativeFuture.result( res, 100L );
		
		assertEquals( list, result );
		
	}
	
	@Test
	public void testSequenceWithErrorInOneComponent() throws Exception {
		
		// GIVEN
		
		final List<AlternativeFuture<String>> listFutures = new ArrayList<>();

		final Exception expectedException = new Exception( "Error in B" );
		
		listFutures.add( AlternativeFuture.successful( "A" ) );
		listFutures.add( AlternativeFuture.failed( expectedException ) );
		listFutures.add( AlternativeFuture.successful( "C" ) );		
		
		// WHEN
		
		AlternativeFuture<List<String>> res = AlternativeFutures.sequence( listFutures );
		
		// THEN
		
		try {
			
			AwaitAlternativeFuture.result( res, 100L );
			
			fail("You mustn't be here");
			
		} catch (Exception actualException) {
			
			assertEquals(expectedException, actualException);
		}
		
	}
	
	

}
