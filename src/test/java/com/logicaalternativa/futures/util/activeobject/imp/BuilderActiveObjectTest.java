package com.logicaalternativa.futures.util.activeobject.imp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import com.logicaalternativa.futures.AlternativeFuture;
import com.logicaalternativa.futures.imp.AwaitAlternativeFuture;
import com.logicaalternativa.futures.util.activeobject.IBuilderActiveObject;

public class BuilderActiveObjectTest {

	private static final Long TIME_SLEEP = 2000L;
	
	private ExecutorService executor;

	
	@Before
	public void setUp() {
		
		executor = Executors.newCachedThreadPool();
		
		
	}
	
	@Test
	public void checkWithImplementation() {
		
		final IBuilderActiveObject<IFooFuture> builder0 = BuilderActiveObject.getInstance( IFooFuture.class );
	
		assertNotNull( builder0 ) ;
		
		final IFooFuture ifoo = s -> AlternativeFuture.successful( "1" );
		
		final IBuilderActiveObject<IFooFuture> builder1 = builder0.withImplementation( ifoo );
		
		assertEquals(builder0, builder1);
		
	}

	@Test
	public void checkWithExecutor() {
		
		final IBuilderActiveObject<IFooFuture> builder0 = BuilderActiveObject.getInstance( IFooFuture.class );
	
		assertNotNull( builder0 ) ;
		
		final Executor executor = Executors.newCachedThreadPool();
		
		final IBuilderActiveObject<IFooFuture> builder1 = builder0.withExecutor( executor );
		
		assertEquals(builder0, builder1);
		
	}

	@Test
	public void checkTimeout() {
		
		final IBuilderActiveObject<IFooFuture> builder0 = BuilderActiveObject.getInstance( IFooFuture.class );
	
		assertNotNull( builder0 ) ;
		
		final Long timeout = 1L;
		
		final IBuilderActiveObject<IFooFuture> builder1 = builder0.withTimeoutMiliSecIfLocks( timeout );
		
		assertEquals(builder0, builder1);
		
	}

	@Test
	public void checkActiveObjectObjectOk() throws Exception {
		
		// GIVEN
		
		final IFooObject implementation = s -> {			
												waitForMilliSec( TIME_SLEEP );
												return s.toString();
											};
		
		final IFooObject ifooActiveObject = BuilderActiveObject
										.getInstance( IFooObject.class )
										.withExecutor( Executors.newCachedThreadPool() )
										.withImplementation( implementation )
										.withTimeoutMiliSecIfLocks( 2 * TIME_SLEEP )
										.build();
		
		final Integer value = 1;
		
		final Long after = System.currentTimeMillis(); 
		
		// WHEN
		
		final String actual = ifooActiveObject.convert( value );
		
		// THEN
		
		checkTimeMoreTimeSleep(after);
		
		assertEquals( value.toString(), actual);
		
	}

	@Test
	public void checkActiveObjectFutureOk() throws Exception {
		
		// GIVEN
		
		final IFooFuture implementation = s -> {			
												waitForMilliSec( TIME_SLEEP );
												return AlternativeFuture.successful( s.toString() );
											};
		
		final IFooFuture ifooActiveObject = BuilderActiveObject
										.getInstance( IFooFuture.class )
										.withExecutor( Executors.newCachedThreadPool() )
										.withImplementation( implementation )
										.build();
	
		
		final Integer value = 1;
		
		final Long after = System.currentTimeMillis(); 
		
		// WHEN
		
		final AlternativeFuture<String> convertFuture = ifooActiveObject.convertFuture( value );
		
		// THEN
		
		checkTimeLessTimeSleep(after);
		
		final String actual = AwaitAlternativeFuture.result(convertFuture, 2 * TIME_SLEEP );
		
		assertEquals( value.toString(), actual);
		
	}

	@Test
	public void checkActiveObjectVoidOk() throws Exception {
		
		// GIVEN
		
		final AtomicInteger actualValue = new AtomicInteger( 0 );
		
		final IFooVoid implementation = s -> {			
												waitForMilliSec( TIME_SLEEP );
												actualValue.set( s );
											};
		
		final IFooVoid ifooActiveObject = BuilderActiveObject
										.getInstance( IFooVoid.class )
										.withExecutor( Executors.newCachedThreadPool() )
										.withImplementation( implementation )
										.build();
	
		
		final Integer expectedValue = 1;
		
		final Long after = System.currentTimeMillis(); 
		
		// WHEN
		
		ifooActiveObject.fireAndForget( expectedValue );
		
		// THEN
		
		checkTimeLessTimeSleep(after);
		
		waitForMilliSec( 2 * TIME_SLEEP );
		
		assertEquals( expectedValue.intValue(), actualValue.get() );
		
	}
	
	@Test
	public void checkActiveObjectFutureWithError() throws Exception {
		
		
		final IFooFuture implementation = s -> {
			
											waitForMilliSec( TIME_SLEEP );
											return AlternativeFuture.successful( s.toString() );
										};
		
		
		// GIVEN
		
		final IFooFuture ifooActiveObject = BuilderActiveObject
										.getInstance( IFooFuture.class )
										.withExecutor( executor )
										.withImplementation( implementation )
										.build();
	
		
		Long after = System.currentTimeMillis(); 
		
		// WHEN
		
		final AlternativeFuture<String> convertFuture = ifooActiveObject.convertFuture( null );
		
		// THEN
		
		checkTimeLessTimeSleep(after);
		
		try {
			
			AwaitAlternativeFuture.result( convertFuture, 2 * TIME_SLEEP );
			
			fail("You mustn't be here");
			
		} catch ( Exception e ) {

			assertTrue ( e instanceof NullPointerException );
			
		}		
	}
	
	@Test
	public void checkActiveObjectVoidError() throws Exception {
		
		// GIVEN
		
		final IFooVoid implementation = s -> {			
												waitForMilliSec( TIME_SLEEP );
												throw new RuntimeException("Exception test");
											};
		
		final IFooVoid ifooActiveObject = BuilderActiveObject
										.getInstance( IFooVoid.class )
										.withExecutor( Executors.newCachedThreadPool() )
										.withImplementation( implementation )
										.build();
	
		
		final Integer expectedValue = 1;
		
		final Long after = System.currentTimeMillis(); 
		
		// WHEN
		
		try {
			
			ifooActiveObject.fireAndForget( expectedValue );
		} catch (Exception e) {
			
			fail( "You mustn't be here" );
		}
		
		// THEN
		
		checkTimeLessTimeSleep(after);
		
		waitForMilliSec( 2 * TIME_SLEEP );
		
	}


	private void checkTimeLessTimeSleep(final Long after) {
		
		long time = System.currentTimeMillis() - after;
		
		assertTrue(time < TIME_SLEEP  );
	}


	private void checkTimeMoreTimeSleep(final Long after) {
		
		long time = System.currentTimeMillis() - after;
		
		assertTrue(time >= TIME_SLEEP  );
	}
	
	private interface IFooFuture{
		
		AlternativeFuture<String> convertFuture( Integer value );		
		
	}
	
	private interface IFooObject {
		
		String convert( Integer value );		
		
	}
	
	private interface IFooVoid {
		
		void fireAndForget( Integer value );		
		
	}	
	
	private static void waitForMilliSec( final Long timeSleep ) {
		
		try {
			
			Thread.sleep( timeSleep );		
			
		} catch (InterruptedException e) {
			
			System.err.println( "ERROR " + e);
			e.printStackTrace();
		}
	}
	
	

}
