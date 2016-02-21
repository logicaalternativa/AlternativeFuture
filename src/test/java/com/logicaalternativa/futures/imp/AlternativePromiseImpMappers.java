package com.logicaalternativa.futures.imp;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
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
