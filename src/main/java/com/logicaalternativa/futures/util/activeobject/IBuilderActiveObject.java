package com.logicaalternativa.futures.util.activeobject;

import java.util.concurrent.Executor;

public interface IBuilderActiveObject<T> {
	
	IBuilderActiveObject<T> withExecutor( final Executor executor );
	
	IBuilderActiveObject<T> withImplementation( final T implementation );

	IBuilderActiveObject<T> withTimeoutMiliSecIfLocks(final Long timeout);

	T build();

}
