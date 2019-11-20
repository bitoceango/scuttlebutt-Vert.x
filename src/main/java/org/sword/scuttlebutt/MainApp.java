package org.sword.scuttlebutt;

import lombok.extern.slf4j.Slf4j;

/**
 * A Camel Application
 */
@Slf4j
public class MainApp {

    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) throws Exception {
        Model a=new Model("A-model");
        Model b=new Model("B-model");

        a.set("key1","test1");
        b.set("key2","test2");

        Duplex  ad= a.creatStream();
        Duplex bd = b.creatStream();

        a.set("key3","test3");
        ad.pullSource(bd::source);
        bd.pullSource(ad::source);

        a.set("k4","test4");


        log.info("model a:{}",a.getStore().toString());
        log.info("model b:{}",b.getStore().toString());

    }

}

