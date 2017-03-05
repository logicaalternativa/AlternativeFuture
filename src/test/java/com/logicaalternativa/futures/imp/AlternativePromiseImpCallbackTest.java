/*
 * AlternativePromiseImpCallbackTest.java
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicaalternativa.futures.AlternativeFuture;
import com.logicaalternativa.futures.AlternativePromise;

public class AlternativePromiseImpCallbackTest {
	
	private static Logger logger = LoggerFactory.getLogger(AlternativePromiseImp.class);
	
	private AlternativePromise<String> promise;
	
	private ExecutorService executorService;
	
	private String threadName;
	
	private AtomicBoolean isCallingCallback; 
	
	@Before
	public void setUp(){
		
		executorService = Executors.newCachedThreadPool();
		
		promise = AlternativePromise.createPromise();

		threadName = Thread.currentThread().getName();
		
		isCallingCallback = new AtomicBoolean( false );
	}

	@Test
	public void resolveSync() throws InterruptedException {
		
		// GIVEN
		
		
		final AlternativeFuture<String> future = promise.future();
		
		final String expected = "Hello world!";
		
		future.onSuccesful(
				lamdaCheck(expected, threadName, isCallingCallback), 
				executorService );
		
		// WHEN
		
		promise.resolve(expected);	
		
		// THEN
		
		Thread.sleep( 200 );
		
		assertTrue( isCallingCallback.get() );
		
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
	public void resolveASyncSeveralOnSucceesfulResolve() throws InterruptedException {
		
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
		
		// GIVEN
		
		final AlternativeFuture<String> future = promise.future();
		
		final String expected = "Hello world!";
		
		future.onSuccesful(
				lamdaCheck(expected, threadName, isCallingCallback), 
				executorService );
		// WHEN
		
		(new Thread(){

			@Override
			public void run() {
				promise.resolve(expected);	
			}			
			
		}).start();
		
		// THEN
		
		Thread.sleep(200);
		
		assertTrue( isCallingCallback.get() );
		
	}
		
	@Test
	public void rejectSync() throws InterruptedException {
		
		final AlternativeFuture<String> future = promise.future();
		
		final Exception expected = new Exception("Hello world!");
		
		future.onFailure(
				lamdaCheck(expected, threadName, isCallingCallback), 
				executorService );
		
		promise.reject(expected);		
		
		// THEN
		
		Thread.sleep(200);
		
		assertTrue( isCallingCallback.get() );
		
		
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
		
		future.onFailure(
				lamdaCheck(expected, threadName, isCallingCallback), 
				executorService );
		
		(new Thread(){
			
			@Override
			public void run() {
				promise.reject(expected);	
			}
			
			
		}).start();
		
		// THEN
		
		Thread.sleep(200);
		
		assertTrue( isCallingCallback.get() );
		
	}
	
	
	@Test
	public void ifReject2TimesSholdThrowException()  {
		
		try {
			
			promise.reject( new Exception("Hello world!") );
			promise.reject( new Exception("Hello world2!") );
			
			fail ( "You mustn't to be here!" );
			
		} catch (IllegalStateException e) {
			
			assertEquals( "The promise is already resolved", e.getMessage() );
		}
		
		
	}
	
	@Test
	public void ifResolve2TimesSholdThrowException()  {
		
		try {
			
			promise.resolve("Hola Mundo");
			promise.resolve("Hola Mundo2");
			
			fail ( "You mustn't to be here!" );
			
		} catch (IllegalStateException e) {
			
			assertEquals( "The promise is already resolved", e.getMessage() );
		}
	}
	
	@Test
	public void ifResolveRejectShouldThrowException()  {
		
		try {
			
			promise.resolve("Hola Mundo");
			promise.reject( new Exception("Hello world!") );
			
			fail ( "You mustn't to be here!" );
			
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
			
			future.onSuccesful(s ->  count.incrementAndGet() , executorService);
		
		}
		
	}

	private void assignOnFailureSeveralTimes(final AlternativeFuture<String> future,
			final Exception expected, final Integer numOnFailure,
			final AtomicInteger count) {
		
		for ( int i = 0 ;i < numOnFailure; i++ ) {
					
			future.onFailure(s -> count.incrementAndGet(), executorService );
				
		}
		
	}

	private static <E> Consumer<E> lamdaCheck(final E expected,
			final String threadName, final AtomicBoolean isCallingCallback) {
		
		return s -> { 
			
			logValue(" Value ", expected, s);
			
			final String nameCurrent = Thread.currentThread().getName();
			logValue(" Thread name ", threadName, nameCurrent);
			
			boolean res = expected.equals( s )
								&& ! threadName.equals(nameCurrent);
			
			isCallingCallback.set(res);
			
		};
		
	}
	
	private static void logValue ( final String description, final Object expected, final Object actual ) {
		
		logger.info( description + ": expected [" + expected + "] -> actual [" + actual + "]" );
		
	}
	
}
