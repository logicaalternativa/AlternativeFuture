/*
 * AwaitAlternativeFutureTest.java
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

import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.logicaalternativa.futures.AlternativeFuture;
import com.logicaalternativa.futures.AlternativePromise;

public class AwaitAlternativeFutureTest {
	
	private static final Long TIMEOUT_MILISECONDS = 200L;

	@Test
	public void testVaidateArgumentFutureNull() throws Exception {
		
		try {
			
			AwaitAlternativeFuture.result( null, TIMEOUT_MILISECONDS );
			
			fail("You mustn't to be here!");
		
		} catch (IllegalArgumentException e) {
			
			assertEquals("Future is null", e.getMessage() );
			
		}
		
		
	}

	@Test
	public void testValidateArgumentTimeoutNull() throws Exception {
		
		final String myString = "Hello world!";
		
		final AlternativeFuture<String> myFuture = AlternativeFuture.successful( myString );
		
		try {
			
			AwaitAlternativeFuture.result( myFuture, null );

			fail("You mustn't to be here!");
		
		} catch (IllegalArgumentException e) {
			
			assertEquals("Timeout is null", e.getMessage() );
			
		}
		
	}

	@Test
	public void testValidateArgumentTimeoutLessThanZero() throws Exception {
		
		final String myString = "Hello world!";
		
		final AlternativeFuture<String> myFuture = AlternativeFuture.successful( myString );
		
		try {
			
			AwaitAlternativeFuture.result( myFuture, -100L );
			
			fail("You mustn't to be here!");
		
		} catch (IllegalArgumentException e) {
			
			assertEquals("Timeout is less than zero", e.getMessage() );
			
		}
		
	}

	@Test
	public void testSynOk() throws Exception {
		
		final String myString = "Hello world!";
		
		final AlternativeFuture<String> myFuture = AlternativeFuture.successful( myString );
		
		final String result = AwaitAlternativeFuture.result( myFuture, TIMEOUT_MILISECONDS );
		
		assertEquals( myString, result );
		
	}
	
	@Test
	public void testSynErrorException() throws Exception {
		
		final Exception expect  = new Exception ("Houston, we have a problem");
		
		final AlternativeFuture<String> myFuture = AlternativeFuture.failed( expect );
		
		try {
			
			AwaitAlternativeFuture.result( myFuture, TIMEOUT_MILISECONDS );
			
			fail( "You mustn't to be here!" );
			
		} catch (Exception e1) {
			
			assertEquals(expect, e1);
		}
		
			
	}
	
	@Test
	public void testSynErrorThrowable() throws Exception {
		
		final Throwable expect  = new Throwable ("Houston, we have a problem");
		
		final AlternativeFuture<String> myFuture = AlternativeFuture.failed( expect );
		
		try {
			
			AwaitAlternativeFuture.result( myFuture, TIMEOUT_MILISECONDS );
			
			fail( "You mustn't to be here!" );
			
		} catch (Exception e1) {
			
			assertEquals( expect, e1.getCause() );
		}
		
			
	}

	@Test
	public void testAsyn() throws Exception {
		
		final String myString = "Hello world!";
		
		final AlternativePromise<String> myPromise = AlternativePromise.createPromise();
		
		( new Thread(){
			
			@Override
			public void run() {
				
				try {
					
					Thread.sleep( 100 );
					
				} catch (InterruptedException e) {}
				
				myPromise.resolve( myString );
				
			}
			
		} ).start();
		
		
		final AlternativeFuture<String> myFuture = myPromise.future();
		
		final String result = AwaitAlternativeFuture.result( myFuture, TIMEOUT_MILISECONDS );
		
		assertEquals( myString, result );
		
	}
	


	@Test
	public void testAsynErrorTimeOut() throws Exception {
		
		final String myString = "Hello world!";
		
		final AlternativePromise<String> myPromise = AlternativePromise.createPromise();
		
		( new Thread(){
			
			@Override
			public void run() {
				
				try {
					
					Thread.sleep( 300 );
					
				} catch (InterruptedException e) {}
				
				myPromise.resolve( myString );
				
			}
			
		} ).start();
		
		
		final AlternativeFuture<String> myFuture = myPromise.future();
		
		try {
			
			AwaitAlternativeFuture.result( myFuture, TIMEOUT_MILISECONDS );
			
			fail("You mustn't to be here!");
			
		} catch (TimeoutException e) {
			
			assertEquals( "Timeout getting the future",  e.getMessage() );
			
		}
		
	}

}
