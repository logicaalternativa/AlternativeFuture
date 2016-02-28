package com.logicaalternativa.futures.pojo.imp;

import com.logicaalternativa.futures.pojo.AlternativeTuple;

/*
 * AlternativeTupleImp.java
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
public class AlternativeTupleImp<A,B> implements AlternativeTuple<A, B> {
		
	private A componentA;
	private B componenteB;

	public AlternativeTupleImp( final A componentA, final B componenteB) {
		this.componentA = componentA;
		this.componenteB = componenteB;
		
	}

	@Override
	public A getA() {
		
		return componentA;
	
	}

	@Override
	public B getB() {
		
		return componenteB;
	
	}

}
