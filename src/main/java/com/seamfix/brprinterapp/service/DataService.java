package com.seamfix.brprinterapp.service;

import com.seamfix.brprinterapp.config.AppConfig;
import lombok.extern.log4j.Log4j;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.management.FBManager;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;


/**
 * Created by rukevwe on 8/3/2018
 */

@Log4j
public class DataService {

    private static DataService dataService;
    private static SessionFactory sessionFactory;
//
//
//    private DataService() throws Throwable {
//        initialize();
//
//    }

//    private static void initialize() throws Throwable{
//        if (sessionFactory == null) {
//            File folder = new File(System.getProperty("user.home"), ".bioprinter");
//            folder.mkdir();
//            Files.setAttribute(folder.toPath(), "dos:hidden", true);
//
//            File dbFile = new File(folder, "biorprinter.fdb");
//
//            String pathname = dbFile.getAbsolutePath();
//            String username = AppConfig.getDbUser();
//            String password = AppConfig.getDbPassword();
//
//            //log.info(username + "======CREDENTIAL==========" +password);
//
//            FBManager manager = new FBManager(GDSType.getType("EMBEDDED"));
//            manager.start();
//            if (!manager.isDatabaseExists(pathname, username, password)) {
//                manager.createDatabase(pathname, username, password);
//            }
//            manager.stop();
//
//            Configuration cfg = new Configuration();
//            cfg.setProperty("hibernate.connection.url", "jdbc:firebirdsql:embedded:" + pathname);
//            cfg.setProperty("hibernate.connection.username", username);
//            cfg.setProperty("hibernate.connection.password", password);
//            cfg.setInterceptor(new EncryptionInterceptor());
//
//            StandardServiceRegistryBuilder ssrb = new StandardServiceRegistryBuilder().applySettings(cfg.configure().getProperties());
//            sessionFactory = cfg.buildSessionFactory(ssrb.build());
//
//            crypter = new Crypter();
//
//            AppConfig.getAndSave(ConfigKeys.INSTALL_DATE);
//        }
//    }
//    public static void main(String[] args) throws Throwable {
//        DataService.initialize();
//
//        DataService d = new DataService();
//        d.deleteCapaturedDataByPids(Arrays.asList("seun", "seyi"));
//    }

}
