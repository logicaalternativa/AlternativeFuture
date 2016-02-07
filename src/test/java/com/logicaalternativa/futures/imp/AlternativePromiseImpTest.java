/*
 * AlternativePromiseImpTest.java
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import com.logicaalternativa.futures.AlternativeFuture;
import com.logicaalternativa.futures.AlternativePromise;

public class AlternativePromiseImpTest {
	
	AlternativePromise<String> promise;
	
	@Before
	public void setUp(){
		
		promise = new AlternativePromiseImp<String>();
	}

	@Test
	public void resolveSync() {
		
		final AlternativeFuture<String> future = promise.future();
		
		final String expected = "Hello world!";
		
		future.onSuccesful(s -> assertEquals(expected, s));
		
		promise.resolve(expected);	
		
	}
	
	@Test
	public void resolveSyncSeveralOnSucceesful() throws InterruptedException {
		
		// GIVEN
		
		final AlternativeFuture<String> future = promise.future();
		
		final String expected = "Hello world!";
		
		final AtomicInteger count = new AtomicInteger(0);
		
		int numOnSuccesful = 3;
		
		// WHEN
		
		assignOnSuccesfulSeveralTimes(future, expected, count, numOnSuccesful);	
		
		promise.resolve(expected);
		
		// THEN
		
		Thread.sleep( 100 );
		
		assertEquals( numOnSuccesful, count.get() );
		
	}
	
	
	@Test
	public void resolveASyncSeveralOnSucceesfulResole() throws InterruptedException {
		
		// GIVEN
		
		final AlternativeFuture<String> future = promise.future();
		
		final String expected = "Hello world!";
		
		final AtomicInteger count = new AtomicInteger(0);
		
		final Integer numOnSuccesful = 3;
		
		ExecutorService executor = Executors.newCachedThreadPool();

		// WHEN
		
		executor.execute( () -> assignOnSuccesfulSeveralTimes(future, expected, count, numOnSuccesful) );
		
		executor.execute( () -> promise.resolve(expected) );
		
		// THEN		
		
		Thread.sleep( 350 );
		
		assertEquals( numOnSuccesful.intValue(), count.get() );
		
	}
	
	
	@Test
	public void resolveASync() throws InterruptedException {
		
		final AlternativeFuture<String> future = promise.future();
		
		final String expected = "Hello world!";
		
		future.onSuccesful(s -> assertEquals(expected, s));
		
		(new Thread(){

			@Override
			public void run() {
				promise.resolve(expected);	
			}
			
			
		}).start();
		
		Thread.sleep(100);
		
	}
		
	@Test
	public void rejectSync() {
		
		final AlternativeFuture<String> future = promise.future();
		
		final Exception expected = new Exception("Hello world!");
		
		future.onFailure(s -> assertEquals(expected, s));
		
		promise.reject(expected);	
		
	}
	

	
	@Test
	public void rejectSeveralSync() throws InterruptedException {
		
		final AlternativeFuture<String> future = promise.future();
		
		final Exception expected = new Exception("Hello world!");
		
		final Integer numOnFailure = 3;
		
		final AtomicInteger count = new AtomicInteger(0);
		
		assignOnFailureSeveralTimes(future, expected, numOnFailure, count);
		
		promise.reject(expected);
		
		Thread.sleep( 100 );
		
		assertEquals( numOnFailure.intValue(), count.get() );
		
	}
	
	@Test
	public void rejectASync() throws InterruptedException {
		
		final AlternativeFuture<String> future = promise.future();
		
		final Exception expected = new Exception("Hello world!");
		
		future.onFailure(s -> assertEquals(expected, s));
		
		(new Thread(){

			@Override
			public void run() {
				promise.reject(expected);	
			}
			
			
		}).start();
		
		Thread.sleep(100);	
		
	}
	
	
	@Test
	public void ifReject2TimesSholdThrowException()  {
		
		try {
			
			promise.reject( new Exception("Hello world!") );
			promise.reject( new Exception("Hello world2!") );
			
			fail ( "You don't have to be here!" );
			
		} catch (IllegalStateException e) {
			
			assertEquals( "The promise is already resolved", e.getMessage() );
		}
		
		
	}
	
	@Test
	public void ifResolve2TimesSholdThrowException()  {
		
		try {
			
			promise.resolve("Hola Mundo");
			promise.resolve("Hola Mundo2");
			
			fail ( "You don't have to be here!" );
			
		} catch (IllegalStateException e) {
			
			assertEquals( "The promise is already resolved", e.getMessage() );
		}
	}
	
	@Test
	public void ifResolveRejectShouldThrowException()  {
		
		try {
			
			promise.resolve("Hola Mundo");
			promise.reject( new Exception("Hello world!") );
			
			fail ( "You don't have to be here!" );
			
		} catch (IllegalStateException e) {
			
			assertEquals( "The promise is already resolved", e.getMessage() );
		}
		
	}
	
	@Test
	public void ifRejectResolveShouldThrowException()  {
		
		try {
			
			promise.reject( new Exception("Hello world!") );
			promise.resolve("Hola Mundo");
			
			fail ( "You don't have to be here!" );
			
		} catch (IllegalStateException e) {
			
			assertEquals( "The promise is already resolved", e.getMessage() );
		}
		
	}
	


	private void assignOnSuccesfulSeveralTimes(final AlternativeFuture<String> future,
			final String expected, final AtomicInteger count, int numOnSuccesful) {
		for ( int i =0 ;i<numOnSuccesful; i++ ) {
			
			future.onSuccesful(s -> { assertEquals(expected, s); count.incrementAndGet(); } );
		
		}
		
	}

	private void assignOnFailureSeveralTimes(final AlternativeFuture<String> future,
			final Exception expected, final Integer numOnFailure,
			final AtomicInteger count) {
		
		for ( int i = 0 ;i < numOnFailure; i++ ) {
					
			future.onFailure(s -> { assertEquals(expected, s); count.incrementAndGet(); } );
				
		}
		
	}
	
}
