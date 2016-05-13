/*
 * AlternativePromiseImpThenTest.java
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

import static org.junit.Assert.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import com.logicaalternativa.futures.AlternativeFuture;
import com.logicaalternativa.futures.AlternativePromise;

public class AlternativePromiseImpFlatMapTest {
	
	
	@Test
	public void convertFuturesOk() throws Exception {
		
		// GIVEN
		
		final Integer valueExpected = 1;

		final ExecutorService executorService = Executors.newCachedThreadPool();
		
		final AlternativeFuture<Integer> futureInteger = AlternativeFuture.successful(valueExpected );
		
		// WHEN		
		
		final AlternativeFuture<String> res = futureInteger
			.flatMap( s -> { sleep( 50L ); return convertToString(s); }, executorService);
		
		// THEN
		
		final String valueActual = AwaitAlternativeFuture.result( res, 500L );
		
		assertEquals(valueExpected.toString(), valueActual);
		
	}
	

	
	@Test
	public void convertFuturesFromFutureFailed() throws Exception {
		
		// GIVEN
		
		final Exception expectedException = new Exception("Test error");

		final ExecutorService executorService = Executors.newCachedThreadPool();
		
		final AlternativeFuture<Integer> futureInteger = AlternativeFuture.failed( expectedException );
		
		// WHEN		
		
		final AlternativeFuture<String> res = futureInteger
				.flatMap( s -> { 
									sleep( 50L ); 
									return convertToString( s ); 
								}
				, executorService);
		
		// THEN
		
		try {
			
			AwaitAlternativeFuture.result( res, 500L );
			
			fail( "You musntn't be here" );
			
		} catch (Exception  actualException) {
			
			assertEquals( expectedException, actualException );
			
		}
		
	}
	

	
	@Test
	public void convertFuturesFromFutureFailedSecond() throws Exception {
		
		// GIVEN
		
		final Exception expectedException = new Exception("Test error");

		final ExecutorService executorService = Executors.newCachedThreadPool();
		
		final AlternativeFuture<Integer> futureInteger = AlternativeFuture.successful( 1 );
		
		// WHEN		
		
		final AlternativeFuture<String> res = futureInteger
				.flatMap( s -> { sleep( 50L ); return returnFailure(expectedException); }, executorService);
		
		// THEN
		
		try {
			
			AwaitAlternativeFuture.result( res, 500L );
			
			fail( "You musntn't be here" );
			
		} catch (Exception  actualException) {
			
			assertEquals( expectedException, actualException );
			
		}
		
	}
	
	
	private AlternativeFuture<String> convertToString( final Integer value ){
		
		return resolvePromise( value.toString(), 25L);
		
		
	}

	
	
	private AlternativeFuture<String> returnFailure( final Exception expectedException ){
		
		return AlternativeFuture.failed( expectedException );
		
		
	}
	
	private <T> AlternativeFuture<T> resolvePromise(final T value, final Long millisec) {
		
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		
		final AlternativePromise<T> resPromise = AlternativePromise.createPromise();
		
		executor.execute( () -> { sleep( millisec ); resPromise.resolve( value ); }  );
		
		return resPromise.future();
		
	}
	
	
	private static void sleep( final Long millisec ){
		
		try {
			
			Thread.sleep( millisec );
			
		} catch (InterruptedException e) {} 
	}
	
	
	
	
	
}
