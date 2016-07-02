package com.api.test.tasklet;

import org.junit.Assert;
import org.junit.Test;

public class StringManipulationTest {

	@Test
	public void testRegularExpression() {
		String value = "los angeles".replaceAll("([\\s|\\W]+)", "-");
		Assert.assertEquals("los-angeles", value);
	}
}
