package com.api.test.tasklet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.Test;

public class PathMatchingTest {

	@Test
	public void testPathMatching() throws IOException {

		final Path p = Paths.get("/", "opt", "sitemaps", "sitemap");
		final PathMatcher filter = p.getFileSystem().getPathMatcher("glob:/**/sitemap-*.xml");
		
		try (final Stream<Path> stream = Files.list(p)) {
		    stream.filter(filter::matches)
		          .forEach(System.out::println);
		}

	}
}

