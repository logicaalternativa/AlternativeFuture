/*
 * AlternativePromiseImpMappers.java
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import com.logicaalternativa.futures.AlternativeFuture;

public class AlternativePromiseImpMappers {

	@Test
	public void mapperToOtherFutureOk() throws InterruptedException {
		
		AlternativeFuture<String> futureString = AlternativeFutures.successful( "1" );
		
		ExecutorService executorService = Executors.newCachedThreadPool();
		
		AlternativeFuture<Integer> futureInteger = futureString
														.map(s -> Integer.parseInt(s) 
																, executorService );
		
		final AtomicInteger atomicInteger = new AtomicInteger( 0 );
		
		futureInteger.onSuccesful(s -> atomicInteger.set( s ), executorService);
		
		Thread.sleep(200L);
		
		assertEquals( 1, atomicInteger.get() );
		
	}
	
	@Test
	public void mapperToOtherFutureReject() throws InterruptedException {
		
		final Exception expected = new Exception( "Exception test" );
		
		AlternativeFuture<String> futureString = AlternativeFutures.failed( expected);
		
		ExecutorService executorService = Executors.newCachedThreadPool();
		
		AlternativeFuture<Integer> futureInteger = futureString
														.map(s -> Integer.parseInt(s) 
																, executorService );
		
		AtomicReference<Throwable> atomicReference = new AtomicReference<Throwable>();
		
		futureInteger.onFailure( s -> atomicReference.set( s ), executorService);
		
		Thread.sleep(200L);
		
		assertEquals( expected, atomicReference.get() );
		
	}

}
