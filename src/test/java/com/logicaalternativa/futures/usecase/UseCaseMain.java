package com.logicaalternativa.futures.usecase;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.logicaalternativa.futures.AlternativeFuture;
import com.logicaalternativa.futures.AlternativePromise;
import com.logicaalternativa.futures.imp.AlternativeFutures;
import com.logicaalternativa.futures.imp.AwaitAlternativeFuture;

public class UseCaseMain {
	
	
	protected static interface Foo {
		
		abstract AlternativeFuture<String> convert( Integer i ) throws InterruptedException;
		
	}

	public static void main(String[] args) throws InterruptedException {
		
		
		final Foo fooImp = new Foo() {
			
			@Override
			public AlternativeFuture<String> convert(Integer i) throws InterruptedException {
				
				Thread.sleep(5000L);
				
				return AlternativeFuture.successful(i.toString());
			}
		};
		
		final ExecutorService executorService = Executors.newCachedThreadPool();
		
		final InvocationHandler invocationHandler = new InvocationHandler() {
			
			@Override
			public Object invoke(Object proxy, final Method method, final Object[] args)
					throws Throwable {
				
				if ( method.getReturnType().isAssignableFrom( AlternativeFuture.class ) ) {
					
					final AlternativePromise<Object> promise = AlternativePromise.createPromise();
					
					executorService.execute(() -> {
					
						try {		
							
							AlternativeFuture<?> future = (AlternativeFuture<?>) method.invoke(fooImp, args);
							
							future.onFailure( e-> promise.reject( e) , executorService);
							future.onSuccesful(s-> promise.resolve( s ), executorService);

							
						} catch (Exception e) {
							promise.reject(e);
							
						}
							
							
					});
					
					
					return promise.future();
					
					
					
				}
				return null;
			}
			
			
			
		
		};
	
		Class<?>[] interfaces = new Class[] {Foo.class};
		
		Foo fooProxy = (Foo) Proxy.newProxyInstance( UseCaseMain.class.getClassLoader(), interfaces , invocationHandler);
		
		AlternativeFuture<String> result = fooProxy.convert(10);
		
		System.out.println("Despues");
		
		try {
			String res = AwaitAlternativeFuture.result(result, 6000L);
			
			System.out.println("Res " + res);
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	
	}

}
