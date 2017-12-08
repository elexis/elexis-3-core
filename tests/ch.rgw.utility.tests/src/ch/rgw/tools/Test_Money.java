/*******************************************************************************
 * Copyright (c) 2005-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.rgw.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.text.ParseException;

import org.junit.Test;

public class Test_Money {
	@Test
	public void testCheckInput() throws ParseException{
		
		assertEquals(1234, Money.checkInput("1234").doubleValue(), 0.0001);
		
		assertEquals(1234.56, Money.checkInput("1234.56").doubleValue(), 0.0001);
		
		assertEquals(1.234, Money.checkInput("1.234").doubleValue(), 0.0001);
		
		assertEquals(1234, Money.checkInput("1'234").doubleValue(), 0.0001);
		
		assertEquals(12340, Money.checkInput("1'234'0").doubleValue(), 0.0001);
		
		assertEquals(1234.56, Money.checkInput("1234,56").doubleValue(), 0.0001);
		
		assertEquals(1234, Money.checkInput("1.234,0").doubleValue(), 0.0001);
		
		assertEquals(1.234, Money.checkInput("1,234").doubleValue(), 0.0001);
		
		assertEquals(12340, Money.checkInput("1.234.0").doubleValue(), 0.0001);
	}
	
	@Test
	public void testCheckInputFail(){
		try {
			Money.checkInput("1xyz2");
			fail("Expected Exception not thrown!");
		} catch (ParseException pe) {}
		try {
			Money.checkInput("1.234'00");
			fail("Expected Exception not thrown!");
		} catch (ParseException pe) {}
		try {
			Money.checkInput("1'234,00");
			fail("Expected Exception not thrown!");
		} catch (ParseException pe) {}
		try {
			Money.checkInput("1,234.00");
			fail("Expected Exception not thrown!");
		} catch (ParseException pe) {}
		try {
			Money.checkInput("1,234,00");
			fail("Expected Exception not thrown!");
		} catch (ParseException pe) {}
	}
	
	@Test
	public void testAddMoney(){
		Money money = new Money(0);
		Money moneyOne = new Money(1.0);
		Money moneyPointOne = new Money(0.1);
		Money moneyMinusOne = new Money(-1.0);
		
		money.addMoney(moneyOne);
		assertEquals(1, money.getAmount(), 0.0001);
		money.addMoney(moneyPointOne);
		assertEquals(1.1, money.getAmount(), 0.0001);
		money.addMoney(moneyMinusOne);
		assertEquals(0.1, money.getAmount(), 0.0001);
	}
	
	@Test
	public void testSubstractMoney(){
		Money money = new Money(1.1);
		Money moneyOne = new Money(1.0);
		Money moneyPointOne = new Money(0.1);
		Money moneyMinusOne = new Money(-1.0);
		
		money.subtractMoney(moneyOne);
		assertEquals(0.1, money.getAmount(), 0.0001);
		money.subtractMoney(moneyPointOne);
		assertEquals(0.0, money.getAmount(), 0.0001);
		// Subtracting a negative amount does not lead to positive result !!!
		// money.addMoney(moneyMinusOne);
		// assertEquals(1.0, money.getAmount(), 0.0001);
	}
	
}
