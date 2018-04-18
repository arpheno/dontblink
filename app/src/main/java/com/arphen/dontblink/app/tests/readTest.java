package com.arphen.dontblink.app.tests;

import com.arphen.dontblink.app.GoodReads;

import junit.framework.TestCase;

import static org.jsoup.Jsoup.parse;

/**
 * Created by Arphen on 21.04.2014.
 */
public class readTest extends TestCase {
    public void test() throws Exception {

        assertEquals("3.70", GoodReads.getRating("martin amis", "money"));

    }

}
