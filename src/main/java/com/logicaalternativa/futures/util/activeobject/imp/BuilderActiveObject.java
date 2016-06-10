package com.logicaalternativa.futures.util.activeobject.imp;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicaalternativa.futures.AlternativeFuture;
import com.logicaalternativa.futures.AlternativePromise;
import com.logicaalternativa.futures.imp.AwaitAlternativeFuture;
import com.logicaalternativa.futures.util.activeobject.IBuilderActiveObject;

public class BuilderActiveObject<T> implements IBuilderActiveObject<T> {
	
	private static Logger logger = LoggerFactory.getLogger(BuilderActiveObject.class);
	
	private Executor executor;
	
	private T implementation;

	private Class<T> intrface;

	private Long timeout;

	private BuilderActiveObject(Class<T> intrface) {
		this.intrface = intrface;
		
	}

	@Override
	public IBuilderActiveObject<T> withExecutor(
			Executor executor) {
		this.executor = executor;
		return this;
	}

	@Override
	public IBuilderActiveObject<T> withTimeoutMiliSecIfLocks(final Long timeout) {
		this.timeout = timeout;
		return this;
	}

	@Override
	public IBuilderActiveObject<T> withImplementation(T implementation) {
		this.implementation = implementation;
		return this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T build() {
		
		final InvocationHandler hander = createHandler( implementation, executor, timeout );
		
		final T activeObject = (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {intrface}, hander );
		
		return activeObject;
	}
	
	private InvocationHandler createHandler( final T implementation, final Executor executor, final Long timeoutMiliseconds ) {
					
		return ( proxy, method, args ) -> {
			
			if (  logger.isTraceEnabled() ) {
				
				logger.trace( " Return type:  " + method.getReturnType() );
				
			}
			
			if ( AlternativeFuture.class.isAssignableFrom( method.getReturnType() ) ) {
				
				return processReturnFuture( implementation, method, args, executor );
				
			}
			
			if( method.getReturnType().equals(Void.TYPE)){
				
				executor.execute( runnableReturnVoid( implementation, method, args ) );
				
				return Void.TYPE;
				
			} else {
				
				final AlternativeFuture<Object> processReturnFuture = processReturnObject( implementation, method, args );
				
				return AwaitAlternativeFuture.result( processReturnFuture, timeoutMiliseconds );
				
			}
			
		};
	}

	private AlternativeFuture<Object> processReturnObject( final T implementation, final Method method, final Object[] args  ) {
		
		final AlternativePromise<Object> promise = AlternativePromise.createPromise();
			
		executor.execute( runnableReturnObject( promise, implementation, method, args ) );			
			
		return promise.future();
	}

	private AlternativeFuture<Object> processReturnFuture( final T implementation, final Method method, final Object[] args, final Executor executor  ) {
		
		final AlternativePromise<Object> promise = AlternativePromise.createPromise();
			
		executor.execute( runnableReturnFuture( promise, implementation, method, args, executor ) );			
			
		return promise.future();
	}
	
	private Runnable runnableReturnVoid( final T implementation, final Method method, final Object[] args ) {
		
		return () -> {			
			
            try {            	
            	
            	method.invoke( implementation, args );				
								
			} catch (Exception e) {
				
				logger.error("Error invoking method void", e);
				
			}		
		};
		
	}
	
	private Runnable runnableReturnFuture( final AlternativePromise<Object> promise, final T implementation, final Method method, final Object[] args, final Executor executor ) {
		
		return () -> {
			
			
            try {	
            	
            	final AlternativeFuture<?> future = (AlternativeFuture<?>) method.invoke( implementation, args );
				
				future.onFailure( e-> promise.reject( e) , executor);
				future.onSuccesful( s-> promise.resolve( s ), executor);

				
			} catch (ReflectiveOperationException e) {
				
				promise.reject( e.getCause() );
				
			} catch (Exception e) {
				
				promise.reject(e);
				
			}		
		};
		
	}
	
private Runnable runnableReturnObject( final AlternativePromise<Object> promise, final T implementation, final Method method, final Object[] args ) {
		
		return () -> {
			
			
            try {	
            	
            	final Object result = method.invoke( implementation, args );
				
				promise.resolve( result );
				
			} catch (ReflectiveOperationException e) {
				
				promise.reject( e.getCause() );
				
			} catch (Exception e) {
				
				promise.reject(e);
				
			}		
		};
		
	}

	public static <T> IBuilderActiveObject<T> getInstance(Class<T> intrface) {
		
		return new BuilderActiveObject<T>( intrface );
	}

	

}
