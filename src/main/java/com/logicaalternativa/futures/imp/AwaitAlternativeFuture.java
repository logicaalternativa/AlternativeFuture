/*
 * AwaitAlternativeFuture.java
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.logicaalternativa.futures.AlternativeFuture;

public class AwaitAlternativeFuture<T>  {
	
	private final  AlternativeFuture<T> future;
	
	private final Condition notResult;
	
	private final Lock lock;

	private Long timeout;
	
	private AwaitAlternativeFuture( AlternativeFuture<T> future, Long timeout ) {
		
		validateArguments(future, timeout);
		
		this.future = future;
		
		this.timeout = timeout;
		
		lock = new ReentrantLock();
		
		notResult = lock.newCondition();
		
	}

	private void validateArguments(AlternativeFuture<T> future, Long timeout) {
		if ( future == null ) {
			
			throw new IllegalArgumentException( "Future is null");
			
		} else if ( timeout == null ) {
			
			throw new IllegalArgumentException( "Timeout is null" );
			
		} else if ( timeout < 0 ) {
			
			throw new IllegalArgumentException( "Timeout is less than zero" );
			
		}
	}

	public T getResult() throws Exception {
		
		lock.lock();
		
		try {
			
			final AtomicBoolean isExecuted = new AtomicBoolean( false );
			
			final AtomicReference<T> result = new AtomicReference<T>();
			
			final AtomicReference<Throwable> throwableReferemce = new AtomicReference<Throwable>();
			
			final ExecutorService executor = Executors.newSingleThreadScheduledExecutor();
			
			future.onSuccesful( s -> { isExecuted.set( true );result.set(s); sendSignal(); } , executor );
			
			future.onFailure( s -> { throwableReferemce.set(s); sendSignal(); } , executor );
			
			notResult.await( timeout, TimeUnit.MILLISECONDS );
			
			Throwable ex = throwableReferemce.get();
			
			validateAndThrowException(ex); 
			
			if ( ! isExecuted.get() ) {
				
				throw new TimeoutException( "Timeout getting the future" );
			}
		
			return result.get();
			
		} finally {
			
			lock.unlock();
			
		}
		
	}

	private void validateAndThrowException(Throwable ex) throws Exception {
		if ( ex != null )  {
			
			if ( ex instanceof Exception ) {
				
				throw (Exception) ex;
				
			} else {
				
				throw new Exception( ex );
				
			}
			
		}
	}
	
	private void sendSignal() {
		
		lock.lock();
		
		try {
			
			notResult.signal();	
			
		} finally {
			
			lock.unlock();
			
		}
		
	}

	public static <V> V result( AlternativeFuture<V> future, Long timeoutMiliseconds) throws Exception {
		
		final AwaitAlternativeFuture<V> awaitAlternativeFuture = new AwaitAlternativeFuture<V>( future, timeoutMiliseconds );
		
		return awaitAlternativeFuture.getResult();
		
	}

}
