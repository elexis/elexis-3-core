/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;

/**
 * A class representing Money (as you might have guessed) Simplifies and standardizes calculations
 * with money (rounding, converting to and from strings etc.) The accepted formats of the string
 * representation of an amount depends on the current locale by default the format of can be set to
 * another locale if necessary (globally) The term "Amount" means here always the x.xx form of Money
 * 
 * @author gerry
 * 
 */
public class Money extends Number implements Comparable<Money> {
	
	private final static String formatPattern = "#,##0.00";
	
	private final static DecimalFormatSymbols symbols = new DecimalFormatSymbols();
	
	private static final long serialVersionUID = 7466555366749958L;
	private static NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
	private int cents; // The value of this money
	private double frac; // What rests after rounding
	
	/**
	 * Create empty Money
	 */
	public Money(){
		cents = 0;
	}
	
	/** Double your wealth */
	public Money(Money money){
		cents = money.cents;
		frac = money.frac;
	}
	
	/** Create Money with some cents */
	public Money(int cent){
		cents = cent;
	}
	
	/** Create Money with a specified amount */
	public Money(double amount){
		cents = (int) Math.round(amount * 100.0);
	}
	
	/**
	 * Create Money with a specified amount as String This might fail if the string doesn't conform
	 * to the current locale's standard currency format
	 * 
	 * @throws ParseException
	 */
	public Money(String val) throws ParseException{
		addAmount(checkInput(val).doubleValue());
	}
	
	/**
	 * Parse an amount given as string This might fail if the string doesn't conform to the current
	 * locale's standard currency format. Note: In Switzerland, Separator is '.' while in Germany,
	 * Austria and Others, it is ','. So if some Computers in the network have an OS locale setting
	 * of Switzerland and others a german, amounts are entered differently. And even more: The
	 * MacOSX does not use swiss layout at all but sees always ',' as separator. So if Macintosh,
	 * Windows and Linux PC's are in he same network, we must make sure, that the decimal comma or
	 * dot is always honored correctly.
	 * 
	 * @param val
	 *            an amount
	 * @return a Number representing the mount
	 * @throws ParseException
	 */
	public static Number checkInput(String rawValue) throws ParseException{
		Number num;
		if (StringTool.isNothing(rawValue)) {
			num = new Double(0.0);
		} else {
			String val = rawValue.trim();
			num = parse(val);
		}
		return num;
	}
	
	private static Double parse(String raw) throws ParseException{
		// try ch format 123'456.789
		ParsePosition parsePosition = new ParsePosition(0);
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator('\'');
		DecimalFormat format = new DecimalFormat(formatPattern, symbols);
		Number ret = format.parse(raw, parsePosition);
		if (ret != null && parsePosition.getIndex() == raw.length())
			return ret.doubleValue();
		// try de format 123.456,789
		parsePosition = new ParsePosition(0);
		symbols.setDecimalSeparator(',');
		symbols.setGroupingSeparator('.');
		format = new DecimalFormat(formatPattern, symbols);
		ret = format.parse(raw, parsePosition);
		if (ret != null && parsePosition.getIndex() == raw.length())
			return ret.doubleValue();
		
		// the string could not be parsed by the known formats
		throw new ParseException(raw, 1);
	}
	
	/** Add some cents */
	public void addCent(String cents){
		String cleanValue = cents != null ? cents.trim() : "0";
		this.cents += Integer.parseInt(cleanValue);
	}
	
	/** Add some cents */
	public void addCent(int cents){
		this.cents += cents;
	}
	
	/** Add some money as x.xx */
	public void addAmount(double amount){
		cents += Math.round(amount * 100.0);
	}
	
	/** Add some money as "x.xx" */
	public void addAmount(String amount) throws ParseException{
		
		addAmount(checkInput(amount).doubleValue());
		
	}
	
	/** Add even more (or less) Money */
	public Money addMoney(Money money){
		cents += money.cents;
		frac += money.frac;
		if (Math.abs(frac) >= 1.0) {
			cents += Math.signum(frac);
			frac -= Math.signum(frac);
		}
		return this;
	}
	
	/** Reduce your wealth */
	public Money subtractMoney(Money money){
		cents -= money.cents;
		frac -= money.frac;
		if (frac < 0) {
			frac += 1;
			cents -= 1;
		}
		return this;
	}
	
	/** Return all the cents (but keep them anyway :-) */
	public int getCents(){
		return cents;
	}
	
	/** return the collected amount as x.xx */
	public double getAmount(){
		return cents / 100.0;
	}
	
	/** return the cents */
	public String getCentsAsString(){
		return Integer.toString(getCents());
	}
	
	/** return the amount as "x.xx" */
	public String getAmountAsString(){
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		String frm = nf.format(getAmount());
		return frm;
	}
	
	/** Round the collected amount to the nearest 0.05 */
	public Money roundTo5(){
		frac = cents;
		double sum = cents / 100.0;
		cents = (int) (5 * Math.round(sum * 20.0));
		frac -= cents;
		return this;
	}
	
	/** Return what was over or missing after rounding */
	public double getFrac(){
		return frac;
	}
	
	public String getFracAsString(){
		return Double.toString(frac);
	}
	
	/**
	 * Multiply your wealth
	 * 
	 * @param factor
	 */
	public Money multiply(double factor){
		cents = (int) Math.round(cents * factor);
		return this;
	}
	
	/**
	 * Turn wealth into dept and vice versa
	 * 
	 * @return multiply(-1.0)
	 */
	public Money negate(){
		return multiply(-1.0);
	}
	
	/**
	 * Are you broke?
	 * 
	 * @return true if you are broke
	 */
	public boolean isZero(){
		return (cents == 0);
	}
	
	public boolean isMoreThan(Money other){
		return cents > other.cents;
	}
	
	/**
	 * Are you in dept?
	 * 
	 * @return true if you are
	 */
	public boolean isNegative(){
		if (cents < 0) {
			return true;
		} else if (cents == 0) {
			return frac < 0;
		} else {
			return false;
		}
	}
	
	/**
	 * Is it worth any effort?
	 * 
	 * @return true if not.
	 */
	public boolean isNeglectable(){
		return Math.abs(cents) < 3;
	}
	
	@Override
	public String toString(){
		return getAmountAsString();
	}
	
	/**
	 * Set a different locale for handling Money
	 * 
	 * @param locale
	 */
	public static void setLocale(Locale locale){
		nf = NumberFormat.getInstance(locale);
	}
	
	@Override
	public boolean equals(Object obj){
		if (!(obj instanceof Money)) {
			return false;
		}
		Money other = (Money) obj;
		return cents == other.cents;
	}
	
	@Override
	public int hashCode(){
		return cents;
	}
	
	public int compareTo(Money other){
		return this.cents - other.getCents();
	}
	
	@Override
	public double doubleValue(){
		return this.getAmount();
	}
	
	@Override
	public float floatValue(){
		return new Float(this.getAmount());
	}
	
	@Override
	public int intValue(){
		return this.getCents();
	}
	
	@Override
	public long longValue(){
		return (long) this.getCents();
	}
	
	public static char getSeparator(){
		double d = 1.0 / 2.0;
		String ds = NumberFormat.getCurrencyInstance().format(d);
		return ds.charAt(1);
	}
}
