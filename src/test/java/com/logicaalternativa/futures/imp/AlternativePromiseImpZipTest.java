/*
 * AlternativePromiseImpMappers.java.java
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

import org.junit.Test;

import com.logicaalternativa.futures.AlternativeFuture;
import com.logicaalternativa.futures.AlternativePromise;
import com.logicaalternativa.futures.pojo.AlternativeTuple;

public class AlternativePromiseImpZipTest {
	
	private final static String RES_STRING = "futureString";
	
	private final static  Integer RES_INT = 10;
	
	private final static Exception RES_EXCEPTION = new Exception( "Houston, We have a problem!" );
	
	
	@Test
	public void zipWihtOtherFutureSynOk() throws Exception {
		
		// GIVEN
		
		final AlternativeFuture<String> futureString = AlternativeFutures.successful( RES_STRING );
		
		final AlternativeFuture<Integer> futureInteger = AlternativeFutures.successful( RES_INT );
		
		// WHEN
		
		final AlternativeFuture<AlternativeTuple<String, Integer>> futureZip = futureString.zip( futureInteger );
		
		// THEN
		
		final AlternativeTuple<String, Integer> result = AwaitAlternativeFuture.result( futureZip, 120L);
		
		assertEquals( RES_STRING, result.getA() );
		
		assertEquals( RES_INT, result.getB() );
		
	}
	
	@Test
	public void zipWihtOtherFutureAsynOkOne() throws Exception {
		
		// GIVEN
		
		AlternativePromise<String> promiseString = AlternativeFutures.createPromise();
		
		AlternativePromise<Integer> promiseInt = AlternativeFutures.createPromise();		
		
		resolveNewThreadPromise( promiseInt, RES_INT, 80L );
		
		resolveNewThreadPromise( promiseString, RES_STRING, 50L );
		
		// WHEN
		
		final  AlternativeFuture<String> futureString = promiseString.future();
		
		final AlternativeFuture<Integer> futureInteger = promiseInt.future();
		
		final  AlternativeFuture<AlternativeTuple<String, Integer>> futureZip = futureString.zip( futureInteger );
		
		// THEN
		
		AlternativeTuple<String, Integer> result = AwaitAlternativeFuture.result( futureZip, 120L );
		
		assertEquals( RES_STRING, result.getA() );
		
		assertEquals( RES_INT, result.getB() );
		
	}
	
	@Test
	public void zipWihtOtherFutureAsynOkTwo() throws Exception {
		
		// GIVEN
		
		AlternativePromise<String> promiseString = AlternativeFutures.createPromise();
		
		AlternativePromise<Integer> promiseInt = AlternativeFutures.createPromise();		
		
		resolveNewThreadPromise( promiseInt, RES_INT, 50L );
		
		resolveNewThreadPromise( promiseString, RES_STRING, 80L );
		
		// WHEN
		
		final  AlternativeFuture<String> futureString = promiseString.future();
		
		final AlternativeFuture<Integer> futureInteger = promiseInt.future();
		
		final  AlternativeFuture<AlternativeTuple<String, Integer>> futureZip = futureString.zip( futureInteger );
		
		// THEN
		
		AlternativeTuple<String, Integer> result = AwaitAlternativeFuture.result( futureZip, 100L );
		
		assertEquals( RES_STRING, result.getA() );
		
		assertEquals( RES_INT, result.getB() );
		
	}
	
	
	@Test
	public void zipWihtOtherFutureFailureSecondFutureSync() throws Exception {
		
		// GIVEN
		
		AlternativeFuture<String> futureString = AlternativeFutures.successful( RES_STRING );
		
		AlternativeFuture<Integer> futureInteger = AlternativeFutures.failed( RES_EXCEPTION );
		
		// WHEN

		AlternativeFuture<AlternativeTuple<String, Integer>> futureZip = futureString.zip( futureInteger );
		
		// THEN 

		try {
			
			
			AwaitAlternativeFuture.result( futureZip, 100L);
			
			fail("You mustn't to be here");
			
		} catch (Exception e) {
			
			assertEquals( RES_EXCEPTION, e );
		}
		
	}	
	
	@Test
	public void zipWihtOtherFutureFailureSecondFutureAsync() throws Exception {
		
		// GIVEN
		
		final AlternativePromise<String> promiseString = AlternativeFutures.createPromise();
		
		final AlternativePromise<Integer> promiseInt = AlternativeFutures.createPromise();		
		
		resolveNewThreadPromise( promiseString, RES_STRING, 100L );
		
		rejectNewThreadPromise( promiseInt, RES_EXCEPTION, 40L );
		
		final  AlternativeFuture<String> futureString = promiseString.future();
		
		final AlternativeFuture<Integer> futureInteger = promiseInt.future();
		
		// WHEN
		
		final AlternativeFuture<AlternativeTuple<String, Integer>> futureZip = futureString.zip( futureInteger );
		
		try {
			
			AwaitAlternativeFuture.result( futureZip, 80L);
			
			fail("You mustn't to be here");
			
		} catch (Exception e) {
			
			// THEN 
			
			assertEquals( RES_EXCEPTION, e );
		}
		
	}
	
	@Test
	public void zipWihtOtherFutureFailureFirstFutureSync() throws Exception {
		
		// GIVEN
		
		final AlternativeFuture<String> futureString = AlternativeFutures.failed( RES_EXCEPTION );
		
		final AlternativeFuture<Integer> futureInteger = AlternativeFutures.successful( RES_INT );
		
		// WHEN 

		final AlternativeFuture<AlternativeTuple<String, Integer>> futureZip = futureString.zip( futureInteger );
		
		// THEN

		try {
			
			AwaitAlternativeFuture.result( futureZip, 100L);
			
			fail("You mustn't to be here");
			
		} catch (Exception e) {
			
			assertEquals( RES_EXCEPTION, e );
		}
		
	}
	
	
	
	
	@Test
	public void zipWihtOtherFutureFailureFirstFutureAsync() throws Exception {
		
		// GIVEN
		
		final AlternativePromise<String> promiseString = AlternativeFutures.createPromise();
		
		final AlternativePromise<Integer> promiseInt = AlternativeFutures.createPromise();		
		
		resolveNewThreadPromise( promiseInt, RES_INT, 100L );
		
		rejectNewThreadPromise( promiseString, RES_EXCEPTION, 40L );
		
		final  AlternativeFuture<String> futureString = promiseString.future();
		
		final AlternativeFuture<Integer> futureInteger = promiseInt.future();
		
		// WHEN
		
		final AlternativeFuture<AlternativeTuple<String, Integer>> futureZip = futureString.zip( futureInteger );
		
		try {
			
			AwaitAlternativeFuture.result( futureZip, 80L);
			
			fail("You mustn't to be here");
			
		} catch (Exception e) {
			
			// THEN 
			
			assertEquals( RES_EXCEPTION, e );
		}
		
	}
	


	private <V> void resolveNewThreadPromise( final AlternativePromise<V> promise, final V value, final Long millis) {
		
		( 
			new Thread( () -> { sleep( millis); promise.resolve( value); } )
			
		).start();
	}
	
	private <V> void rejectNewThreadPromise( final AlternativePromise<?> promise, final Throwable value, final Long millis) {
		
		( 
			new Thread( () -> { sleep( millis); promise.reject( value); } )
			
		).start();
	}
	
	private static void sleep( Long millis ){
		
		try {
			
			Thread.sleep(millis);
			
		} catch (InterruptedException e) {}
	}
	
	
	
}
