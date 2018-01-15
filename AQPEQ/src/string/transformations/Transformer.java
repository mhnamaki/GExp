package string.transformations;

import java.text.DecimalFormat;
import java.util.*;
import java.lang.*;
import java.io.*;

import javax.measure.*;//converter.UnitConverter;
import javax.measure.converter.*;
import javax.measure.quantity.*;
import javax.measure.unit.*;

import org.apache.jena.ext.com.google.common.collect.ContiguousSet;
import org.apache.jena.ext.com.google.common.collect.DiscreteDomain;
import org.apache.jena.ext.com.google.common.collect.Range;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/** 
 * @author Casey Fleck
 * @project Transformation function
 * @email cjfleck@coastal.edu
 * @date 6.16.17
 */

/**
 * Transformer.java - a searching algorithm that takes a string as input and
 * matches the word with its best specified parameter by the user.
 */
public class Transformer {

	/**
	 * lastToken - retrieves the last word in a string separated by a space
	 * 
	 * @param input
	 *            - the input string to be separate
	 * @return - the last word from the input string
	 */
	public static String lastToken(String input) {
		String lastWord = input.substring(input.lastIndexOf(" ") + 1);
		return lastWord;
	}

	/**
	 * firstToken - retrieves the first token in a string separated by a space
	 * 
	 * @param input
	 *            - the input string to be separated
	 * @return - the first word from the input string
	 */
	public static String firstToken(String input) {
		int i = input.indexOf(" ");
		// ******might develop a problem her if only identifying tokens by a
		// space

		String firstWord = input.substring(0, i);
		return firstWord;
	}

	/**
	 * Abbreviation - combines the initials of all tokens in string except the
	 * last, which is left alone
	 * 
	 * @param input
	 *            - the input string to be separated
	 * @return - initials of all words in string and the last token left alone
	 */
	public static String Abbreviation(String input) {
		String abbrev = input.replaceAll("\\B.|\\P{L}", "").toUpperCase();
		// gather initials of input string, converting them to uppercase

		abbrev = abbrev.substring(0, abbrev.length() - 1);
		String lastWord = " " + input.substring(input.lastIndexOf(" ") + 1);
		abbrev = abbrev.concat(lastWord);
		// collect and concatenate last word of input string

		return abbrev;
	}

	/**
	 * Drop - drop the last token of input string
	 * 
	 * @param input
	 *            - input string that will lose its last token
	 * @return - same string without its last token
	 */
	public static String Drop(String input) {
		String withoutLastString = input.substring(0, input.lastIndexOf(" "));
		return withoutLastString;
	}

	/**
	 * Acronym - combines the initials of each token
	 * 
	 * @param input
	 *            - input string needed to find acronym
	 * @return - acronym (or initials) of input string
	 */
	public static String Acronym(String input) {
		String acro = input.replaceAll("\\B.|\\P{L}", "").toUpperCase();
		return acro;
	}

	/**
	 * Synonym - searches for the specified map value that is associated with
	 * the input value in question
	 * 
	 * @param input
	 *            - the input value that has a specified synonym associated to
	 *            it
	 * @param map
	 *            - the map being searched for the synonym to the specified
	 *            input value
	 * @return - the synonym to the specified input value
	 */
	public static String Synonym(String input, Map<String, String> map) {
		String output = "No mapped values are added yet.";
		if (map.isEmpty() == true || map.containsKey(input) == false) {
			return output;
		}
		// check if map is empty, or if there are no synonym's for the specified
		// input value yet

		output = map.get(input);
		return output;
	}

	/**
	 * addSynonym - adds a synonym to the specified mapped value
	 * 
	 * @param input
	 *            - original token that does not have a specified synonym (key)
	 *            mapped to it yet
	 * @param synonym
	 *            - synonym that will be mapped to input value
	 * @param map
	 *            - construct that maps the specified synonym to the input value
	 * @return - the map containing the key (input string) and its map to the
	 *         value (synonym)
	 */
	public static Map<String, String> addSynonym(String input, String synonym,
			Map<String, String> map) {
		map.put(input, synonym);
		return map;
	}

	/**
	 * replaceWithSynonym - replaces the specified token located in the input
	 * with its mapped synonym
	 * 
	 * @param token
	 *            - value located in input string to be replaced
	 * @param input
	 *            - input string that will be edited
	 * @param map
	 *            - construct that contains the mapped synonym to the input
	 *            value
	 * @return - output string with replaced synonym
	 */
	public static String replaceWithSynonym(String token, String input,
			Map<String, String> map) {
		String replacement = "No mapped values are added yet.";
		String output = "New string";

		if (map.isEmpty() == true || map.containsKey(token) == false) {
			return replacement;
		}
		// check if map is empty, or if there are no synonym's for the specified
		// input value yet

		replacement = map.get(token);
		output = input.replaceAll(token, replacement);
		// Replace all occurrences of token (key) in string with its synonym
		// (value)

		return output;
	}

	/**
	 * DateGap - finds the time difference between two dates
	 * 
	 * @param date1
	 *            - the current date to be compared
	 * 
	 * @param date2
	 *            - the date that will be the end date
	 * 
	 * @return the difference btw the first and second input dates
	 */
	public static String DateGap(DateTime date1, DateTime date2) {
		// original date gets subtracted first
		int currentYear = date1.getYear();
		int inputYear = date2.getYear();
		int diff = currentYear - inputYear;
		String apart = null;

		// if comparing current year to a previous year
		if (diff > 0) {
			if (diff == 1) {
				apart = diff + " year ago";
			} else {
				apart = diff + " yrs ago";
			}
		} else if (diff < 0) {
			diff = diff * -1;
			if (diff == 1) {
				apart = diff + " year from " + currentYear + " to " + inputYear;
			} else {
				apart = diff + " yrs from " + currentYear + " to " + inputYear;
			}
		}
		return apart;
	}

	/**
	 * DateGap
	 * 
	 * @param date1
	 *            - input year to compare to today's date to
	 * @return the difference btw input date and today's date
	 */
	public static String DateGap(DateTime date1) {
		// original date gets subtracted first
		DateTime dt = new DateTime();
		int inputYear = date1.getYear();
		int currentYear = dt.getYear();
		int diff = currentYear - inputYear;
		String apart = null;

		// if comparing current year to a previous year
		if (diff > 0) {
			if (diff == 1) {
				apart = diff + " year ago";
			} else {
				apart = diff + " yrs ago";
			}
		} else if (diff < 0) {
			diff = diff * -1;
			if (diff == 1) {
				apart = diff + " year from now.";
			} else {
				apart = diff + " yrs from now";
			}
		}
		return apart;
	}

	/**
	 * Range - finds the range of a specific number given a specific threshold
	 * 
	 * @param number
	 *            - the number to find a range within
	 * @param threshold
	 *            - the distance of how far the numbers go out
	 * @return the range of numbers based on the certain threshold
	 */
	public static ArrayList<Integer> Range(int number, int threshold) {
		ContiguousSet<Integer> intList = ContiguousSet.create(
				Range.closedOpen(number - threshold, number + threshold + 1),
				DiscreteDomain.integers());
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.addAll(intList);
		return list;

	}

	/**
	 * UnitConversion - converts one unit to another based on whether both unit
	 * measures are the same type of measurement
	 * 
	 * @param unit
	 *            - known input measurement
	 * @param convertTo
	 *            - unit that input will be converted to
	 * @return - the converted unit
	 * @throws ConversionException
	 *             - throws an error if measurements are not the same type of
	 *             unit (e.g. length, tempurature, duration, volume, mass).
	 */
	public static String UnitConvertion(Measure unit, Measure convertTo)
			throws ConversionException { // NOTE: Maybe make an exception class
											// for Conversion exception if
											// different units are put in
		String conversion = "No conversion";

		UnitConverter toKilo = unit.getUnit().getConverterTo(
				convertTo.getUnit());
		double num = toKilo.convert(Measure.valueOf(
				unit.getValue().doubleValue(), unit.getUnit()).doubleValue(
				unit.getUnit()));
		num = Math.round(num * 10000);
		num = num / 10000;
		DecimalFormat df = new DecimalFormat("0.0");
		conversion = df.format(num) + " " + convertTo.getUnit();
		return conversion;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Measurable<Mass> weight;
		Measure<Volume> v;
		Measure<Duration> d;
		Measure<Temperature> t;
		Measure<Length> l;
		Measure<Length> u = Measure.valueOf(3, NonSI.MILE);
		Measure<Length> i = Measure.valueOf(10, SI.KILOMETRE);
		String k = UnitConvertion(u, i);
		System.out.println(k);

		Measure<Temperature> c = Measure.valueOf(0, SI.CELSIUS);
		Measure<Temperature> f = Measure.valueOf(0, NonSI.FAHRENHEIT);
		String temp = UnitConvertion(c, f);
		System.out.println(temp);
	}

}
