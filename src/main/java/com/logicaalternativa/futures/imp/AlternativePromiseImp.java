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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicaalternativa.futures.AlternativeFuture;
import com.logicaalternativa.futures.AlternativePromise;
import com.logicaalternativa.futures.FunctionApply;

public class AlternativePromiseImp<T> implements AlternativePromise<T>{
	
	private static Logger logger = LoggerFactory.getLogger(AlternativePromiseImp.class);
	
	private T value;
	
	private Throwable error;
	
	private AlternativeFutureImp future;
	
	public AlternativePromiseImp() {
		
		future = new AlternativeFutureImp();
		
	}

	@Override
	public synchronized void reject(Throwable err) {
		
		validatePromiseIsResovedYet();
		
		error = err;
		
		execQueue( future.getOnFailure(), err );
		
	}

	private void validatePromiseIsResovedYet() {
		
		if ( error != null
				|| value != null ) {
			
			throw new IllegalStateException("The promise is already resolved");
			
		}
		
	}

	@Override
	public synchronized void resolve(T val) {
		
		validatePromiseIsResovedYet();
		
		value = val;
				
		execQueue( future.getOnSuccesfulQueue(), value );
		
	}

	@Override
	public AlternativeFuture<T> future() {
		
		return future;
		
	}	
	
	

	private static <E> void execQueue( final BlockingQueue<FunctionExecutorPojo<FunctionApply<E>>> fucntionQueue, final E value ) {
		
		if ( value == null ) {
			
			return;
		}
		
		executeQueue( fucntionQueue, value );
		
	}
	
	private static <E>  void executeQueue(
			final BlockingQueue<FunctionExecutorPojo<FunctionApply<E>>> onSuccesfulQueue, final E value) {
		
		while ( onSuccesfulQueue != null
				&& ! onSuccesfulQueue.isEmpty() ) {
			
			FunctionExecutorPojo<FunctionApply<E>> fuctionPojo = takeOfQueue( onSuccesfulQueue );
				
			if ( fuctionPojo != null ) {
				
				executeFucntionApply( fuctionPojo.getFunction(), value, fuctionPojo.getExecutorService());
				
			}
			
		}
	}
	
	private static <E> void executeFucntionApply (
			final FunctionApply<E> functionApply, final E value, ExecutorService executorService) {
		
		
			executorService.execute( () -> {
		
				try {
					
					functionApply.apply(value);
					
				} catch (Exception e) {
					
					logger.error("Error to execute FunctionApply", e);
					
				}
				
			});
		
			
			
			
	}
	
	private static <E> E takeOfQueue( BlockingQueue<E> queue ) {
		
		E take = null;
		
		try {
			
			take = queue.take();
			
		} catch (InterruptedException e) {
			
			logger.error("Error to take of queue", e);
		}
		
		return take;
		
	}
	
	private static <E> void putOnQueue( BlockingQueue<FunctionExecutorPojo<E>> queue, E element, final ExecutorService executorService) {
		
		try {
			
			final FunctionExecutorPojo<E> functionExecutorPojo = new FunctionExecutorPojoImp<E>(element, executorService);
			
			queue.put(functionExecutorPojo);
			
		} catch (InterruptedException e) {
			
			logger.error("Error to put the value in the queue");
		}	
		
	}
	
	
	private class AlternativeFutureImp implements AlternativeFuture<T> {
		
		private BlockingQueue<FunctionExecutorPojo<FunctionApply<T>>> onSuccesfulQueue;
	
		private BlockingQueue<FunctionExecutorPojo<FunctionApply<Throwable>>> onFailureQueue;
		
		public AlternativeFutureImp() {
			super();
			onSuccesfulQueue = new LinkedBlockingQueue<FunctionExecutorPojo<FunctionApply<T>>>();
			onFailureQueue = new LinkedBlockingQueue<FunctionExecutorPojo<FunctionApply<Throwable>>>();
		}

		@Override
		public void onSuccesful(final FunctionApply<T> function, final ExecutorService executorService) {
			
			putOnQueue(onSuccesfulQueue, function, executorService );

			execQueue(onSuccesfulQueue, value);
			
		}
		
		@Override
		public void onFailure(FunctionApply<Throwable>  function, ExecutorService executorService ) {
			
			putOnQueue(onFailureQueue, function, executorService);
			
			execQueue(onFailureQueue, error);
			
		}
		
		protected BlockingQueue<FunctionExecutorPojo<FunctionApply<T>>> getOnSuccesfulQueue() {
			
			return onSuccesfulQueue;
		
		}

		protected BlockingQueue<FunctionExecutorPojo<FunctionApply<Throwable>>> getOnFailure() {
			
			return onFailureQueue;
		
		}
		
	}
	
	private static class FunctionExecutorPojoImp<E> implements FunctionExecutorPojo<E> {

		private E function;
		
		private ExecutorService executorService;
		
		@Override
		public E getFunction() {
			return function;
		}

		@Override
		public ExecutorService getExecutorService() {
			return executorService;
		}

		public FunctionExecutorPojoImp(E function,
				ExecutorService executorService) {
			super();
			this.function = function;
			this.executorService = executorService;
		}
		
	}
	
	private static interface FunctionExecutorPojo<E> {
		
		E getFunction();
		
		ExecutorService getExecutorService();		
		
	}

}
