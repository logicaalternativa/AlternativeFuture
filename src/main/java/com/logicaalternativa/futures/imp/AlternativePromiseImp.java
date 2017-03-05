/*
 * AlternativePromiseImp.java
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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

import com.logicaalternativa.futures.AlternativeFuture;
import com.logicaalternativa.futures.AlternativePromise;
import com.logicaalternativa.futures.Monad;
import com.logicaalternativa.futures.pojo.AlternativeTuple;
import com.logicaalternativa.futures.pojo.FunctionExecutorPojo;
import com.logicaalternativa.futures.pojo.imp.AlternativeTupleImp;
import com.logicaalternativa.futures.util.IExecQueue;
import com.logicaalternativa.futures.util.IManageQueue;
import com.logicaalternativa.futures.util.imp.ExecQueue;
import com.logicaalternativa.futures.util.imp.ManageQueue;

public class AlternativePromiseImp<T> implements AlternativePromise<T>{
	
	private T value;
	
	private AtomicBoolean isResolve;
	
	private AtomicBoolean isAlreadySetValue;
	
	private Throwable error;
	
	private AtomicBoolean isReject;
	
	private AlternativeFutureImp future;
	
	private IManageQueue iManageQueue;
	
	private IExecQueue iExecQueue;
	
	public AlternativePromiseImp() {
		
		future = new AlternativeFutureImp();
		
		iManageQueue = ManageQueue.getInstance();
		
		iExecQueue = ExecQueue.getInstance();
		
		isAlreadySetValue = new AtomicBoolean( false );
		
		isResolve = new AtomicBoolean( false );
		
		isReject = new AtomicBoolean( false );
		
	}

	@Override
	public void reject(Throwable err) {
		
		setAndvalidatePromiseIsResovedYet();
		
		error = err;
		
		isReject.set( true);
		
		execQueue( future.getOnFailure(), err, true );
		
	}

	private void setAndvalidatePromiseIsResovedYet() {
		
		boolean isAlready = isAlreadySetValue.getAndSet( true );
		
		if ( isAlready ) {
			
			throw new IllegalStateException("The promise is already resolved");
			
		}
		
	}

	@Override
	public void resolve(T val) {
		
		setAndvalidatePromiseIsResovedYet();
		
		value = val;
				
		isResolve.set( true);
		
		execQueue( future.getOnSuccesfulQueue(), value, true );
		
	}

	@Override
	public AlternativeFuture<T> future() {
		
		return future;
		
	}	

	private <E> void execQueue( final BlockingQueue<FunctionExecutorPojo<Consumer<E>>> fucntionQueue, final E value, boolean isAlreadySetValue ) {
		
		if ( ! isAlreadySetValue ) {
			
			return;
		}
		
		iExecQueue.executeQueue( fucntionQueue, value, iManageQueue );
		
	}	
	
	
	private class AlternativeFutureImp implements AlternativeFuture<T> {
		
		private BlockingQueue<FunctionExecutorPojo<Consumer<T>>> onSuccesfulQueue;
	
		private BlockingQueue<FunctionExecutorPojo<Consumer<Throwable>>> onFailureQueue;
		
		public AlternativeFutureImp() {
			super();
			onSuccesfulQueue = new LinkedBlockingQueue<FunctionExecutorPojo<Consumer<T>>>();
			onFailureQueue = new LinkedBlockingQueue<FunctionExecutorPojo<Consumer<Throwable>>>();
		}

		@Override
		public void onSuccesful(final Consumer<T> function, final Executor executor) {
			
			iManageQueue.putOnQueue(onSuccesfulQueue, function, executor );

			execQueue(onSuccesfulQueue, value, isResolve.get());
			
		}
		
		@Override
		public void onFailure(Consumer<Throwable>  function, Executor executor ) {
			
			iManageQueue.putOnQueue( onFailureQueue, function, executor );
			
			execQueue( onFailureQueue, error, isReject.get() );
			
			
		}		

		@Override
		public <U> AlternativeFuture<U> map(Function<T, U> mapper,
				Executor executor) {
			
			AlternativePromise<U> promise = AlternativePromise.createPromise();
			
			onSuccesful( s -> promise.resolve( mapper.apply( s ) ), executor );
			
			onFailure( s -> promise.reject( s ), executor );
			
			return promise.future();
			
		}
		


		@Override
		public <U> AlternativeFuture<U> flatMap( 
				Function<T, AlternativeFuture<U>> mapper,
				Executor executor) {
			
			final AlternativePromise<U> promise = AlternativePromise.createPromise();
			
			final Consumer<Throwable> funOnFailure = t -> promise.reject( t );
			
			onSuccesful( s -> { 
							
							final AlternativeFuture<U> map = mapper.apply( s ); 							
							map.onSuccesful(t  -> promise.resolve(t), executor);
							map.onFailure( funOnFailure, executor);
							
			}, executor );
			
			onFailure( funOnFailure, executor);
			
			return promise.future();
		}


		@Override
		public <U> AlternativeFuture<AlternativeTuple<T, U>> zip( final AlternativeFuture<U> otherfuture) {
			
			final Executor executor = Executors.newSingleThreadExecutor();
			
			final AlternativePromise<AlternativeTuple<T, U>> promise = AlternativePromise.createPromise();
			
			final AtomicBoolean isReject = new AtomicBoolean( false );
			
			final Consumer<Throwable> funOnFailure = s -> {
				
				if ( ! isReject.getAndSet( true ) ) {
					
					promise.reject( s );			
					
				}
			};
			
			final Consumer<T> funOnSuccesfull = s -> {
				
				otherfuture.onSuccesful( t -> {
						
							final AlternativeTuple<T, U> res = new AlternativeTupleImp<>( s, t );
							
							promise.resolve( res );
							
						}
						, executor);	
				
			};			
			
			onSuccesful( funOnSuccesfull, executor );

			onFailure( funOnFailure, executor );
			
			otherfuture.onFailure( funOnFailure, executor );
			
			return promise.future();
		}
		
		protected BlockingQueue<FunctionExecutorPojo<Consumer<T>>> getOnSuccesfulQueue() {
			
			return onSuccesfulQueue;
		
		}

		protected BlockingQueue<FunctionExecutorPojo<Consumer<Throwable>>> getOnFailure() {
			
			return onFailureQueue;
		
		}

		@Override
		public <U> AlternativeFuture<U> map(final Function<T, U> mapper) {
			
			return map(mapper, Executors.newSingleThreadScheduledExecutor() );
			
		}

		@Override
		public <U> AlternativeFuture<U> flatMap(final Function<T, Monad<U>> mapper) {
			
			Function<T, AlternativeFuture<U>> newMapper = s -> (AlternativeFuture<U>) mapper.apply(s);
			
			return flatMap(  newMapper, Executors.newSingleThreadScheduledExecutor() );
			
		}

		@Override
		public <U> AlternativeFuture<U> pure(final U value) {
			
			return AlternativeFuture.successful(value);
			
		}
		
	}

}
