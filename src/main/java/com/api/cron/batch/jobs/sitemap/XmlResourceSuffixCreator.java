package com.api.cron.batch.jobs.sitemap;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.batch.item.file.ResourceSuffixCreator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("xmlResourceSuffixCreator")
@Scope("step")
public class XmlResourceSuffixCreator implements ResourceSuffixCreator { 
	
	private AtomicInteger count = new AtomicInteger();
 
    public String getSuffix(int arg0) { 
        return "-" + count.getAndIncrement() + ".xml"; 
    } 


}
