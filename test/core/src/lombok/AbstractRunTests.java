/*
 * Copyright © 2009-2010 Reinier Zwitserloot and Roel Spilker.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRunTests {
	protected static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	public void compareFile(DirectoryRunner.TestParams params, File file) throws Throwable {
		StringBuilder messages = new StringBuilder();
		StringWriter result = new StringWriter();
		transformCode(messages, result, file);
		compare(
				file.getName(),
				readFile(params.getAfterDirectory(), file, false),
				result.toString(),
				readFile(params.getMessagesDirectory(), file, true),
				messages.toString(),
				params.printErrors());
	}
	
	protected abstract void transformCode(StringBuilder message, StringWriter result, File file) throws Throwable;
	
	protected String readFile(File file) throws IOException {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			result.append(line);
			result.append(LINE_SEPARATOR);
		}
		reader.close();
		return result.toString();
	}
	
	private String readFile(File dir, File file, boolean messages) throws IOException {
		if (dir == null) return "";
		return readFile(new File(dir, file.getName() + (messages ? ".messages" : "")));
	}
	
	private void compare(String name, String expectedFile, String actualFile, String expectedMessages, String actualMessages, boolean printErrors) throws Throwable {
		try {
			compareContent(name, expectedFile, actualFile);
		} catch (Throwable e) {
			if (printErrors) {
				System.out.println("***** " + name + " *****");
				System.out.println(e.getMessage());
				System.out.println("**** Expected ******");
				System.out.println(expectedFile);
				System.out.println("****  Actual  ******");
				System.out.println(actualFile);
				System.out.println("*******************");
			}
			throw e;
		}
		try {
			compareContent(name, expectedMessages, actualMessages);
		}
		catch (Throwable e) {
			if (printErrors) {
				System.out.println("***** " + name + " *****");
				System.out.println(e.getMessage());
				System.out.println("**** Expected ******");
				System.out.println(expectedMessages);
				System.out.println("****  Actual  ******");
				System.out.println(actualMessages);
				System.out.println("*******************");
			}
			throw e;
		}
	}
	
	private static void compareContent(String name, String expectedFile, String actualFile) {
		String[] expectedLines = expectedFile.split("(\\r?\\n)");
		String[] actualLines = actualFile.split("(\\r?\\n)");
		if (actualLines[0].startsWith("// Generated by delombok at ")) {
			actualLines[0] = "";
		}
		expectedLines = removeBlanks(expectedLines);
		actualLines = removeBlanks(actualLines);
		int size = Math.min(expectedLines.length, actualLines.length);
		for (int i = 0; i < size; i++) {
			String expected = expectedLines[i];
			String actual = actualLines[i];
			assertEquals(String.format("Difference in %s on line %d", name, i + 1), expected, actual);
		}
		if (expectedLines.length > actualLines.length) {
			fail(String.format("Missing line %d in generated %s: %s", size + 1, name, expectedLines[size]));
		}
		if (expectedLines.length < actualLines.length) {
			fail(String.format("Extra line %d in generated %s: %s", size + 1, name, actualLines[size]));
		}
	}
	
	private static String[] removeBlanks(String[] in) {
		List<String> out = new ArrayList<String>();
		for (String s : in) {
			if (!s.trim().isEmpty()) out.add(s);
		}
		return out.toArray(new String[0]);
	}
}
