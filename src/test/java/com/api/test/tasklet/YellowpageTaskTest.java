package com.api.test.tasklet;

import com.api.cron.batch.metadata.YellowPageInfo;
import com.api.cron.batch.model.Topic;
import com.api.cron.batch.task.TaskException;
import com.api.cron.batch.task.YellowPagesTask;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

public class YellowpageTaskTest {
    @Test
    public void testYellowPagesTaskFirstPage() throws UnsupportedEncodingException, TaskException {
        YellowPageInfo info = new YellowPageInfo.YellowPageMetadataBuilder().setCategory("martial+arts").setLocation("los+angeles" + "%2C%20" + "CA").execute();
        YellowPagesTask task = new YellowPagesTask();
        task.execute(info);


        int count = task.getTotalBusinesses();

        System.out.println("size " + count);

        while (task.hasNext()) {
            String email = task.getEmail();
            String website = task.getNextWebsite();

            if (Objects.nonNull(email)) {
                System.out.println(email);
            }

            if (Objects.nonNull(website)) {
                System.out.println(website);
            }


            task.next();
        }
    }

    @Test
    public void testYellowPagesTask() throws UnsupportedEncodingException, TaskException {
        YellowPageInfo info = new YellowPageInfo.YellowPageMetadataBuilder().setCategory("martial+arts").setLocation("los+angeles" + "%2C%20" + "CA").setPage(2 + "").execute();
        YellowPagesTask task = new YellowPagesTask();
        task.execute(info);

        while (task.hasNext()) {
            String email = task.getEmail();
            String website = task.getNextWebsite();
            String phone = task.getNextPhones();

            if (Objects.nonNull(email)) {
                System.out.println(email);
            }

            if (Objects.nonNull(website)) {
                System.out.println(website);
            }

            if (Objects.nonNull(phone)) {
                System.out.println(phone);
            }

            task.next();
        }
    }
}
