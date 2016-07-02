package com.api.cron.batch.jobs.sitemap;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Stream;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("sitemapDirectoryReader")
@Scope("step")
public class SitemapDirectoryReader implements ItemReader<SitemapFile> {

	private Queue<Path> fileNames;
	
    public SitemapDirectoryReader() throws IOException {
    	fileNames = new LinkedList<Path>();

		//final FilenameFilter filter = (dir, name) -> !dir.isDirectory() && name.toLowerCase().endsWith(".xml");
		
		final Path p = Paths.get("/", "opt", "sitemaps", "sitemap");
		final PathMatcher filter = p.getFileSystem().getPathMatcher("glob:/**/sitemap-*.xml");
		
		try (final Stream<Path> stream = Files.list(p)) {
		    stream.filter(filter::matches)
		          .forEach(fileNames::add);
		}
    }

	@Override
	public SitemapFile read() throws Exception, UnexpectedInputException,
			ParseException, NonTransientResourceException {
		if(fileNames == null) {
			return null;
		}
		
		if(!fileNames.isEmpty()) {
			Path path = fileNames.remove();
			/*
			   <?xml version="1.0" encoding="UTF-8"?>
			   <sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
				   <sitemap>
				      <loc>http://www.example.com/sitemap1.xml.gz</loc>
				      <lastmod>2004-10-01T18:23:17+00:00</lastmod>
				   </sitemap>
				   <sitemap>
				      <loc>http://www.example.com/sitemap2.xml.gz</loc>
				      <lastmod>2005-01-01</lastmod>
				   </sitemap>
			   </sitemapindex>
			 */
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			
			SitemapFile file = new SitemapFile();
			file.setLastMod(format.format(new Date()));
			file.setLoc(Constant.SITEMAP_INDEX_URL + path.toFile().getName());
			return file;
		}
		
		return null;
	}

}
