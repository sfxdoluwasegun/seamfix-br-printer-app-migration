package com.seamfix.brprinterapp.service;

import com.seamfix.brprinterapp.config.AppConfig;
import com.seamfix.brprinterapp.config.ConfigKeys;
import com.seamfix.brprinterapp.model.*;
import com.seamfix.brprinterapp.utils.Crypter;
import javafx.util.Pair;
import lombok.extern.log4j.Log4j;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.management.FBManager;
import org.hibernate.*;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import com.seamfix.brprinterapp.utils.EncryptionInterceptor;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import javax.naming.NamingException;
import java.io.File;
import java.nio.file.Files;
import java.util.List;


/**
 * Created by rukevwe on 8/3/2018
 */

@Log4j
public class DataService {

    private static DataService dataService;
    private static SessionFactory sessionFactory;
    private static Crypter crypter;


    private DataService() throws Throwable {
        initialize();

    }

    public static DataService getInstance() {
        if (dataService == null) {
            synchronized (DataService.class) {
                if (dataService == null) {
                    try {
                        dataService = new DataService();
                    } catch (Throwable e) {
                        log.error("Error while initializing dataservice", e);
                    }
                }
            }
        }
        return dataService;
    }

    public static void initialize() throws Throwable {
        if (sessionFactory == null) {
            File folder = new File(System.getProperty("user.home"), ".bioprinter");
            folder.mkdir();
            Files.setAttribute(folder.toPath(), "dos:hidden", true);

            File dbFile = new File(folder, "bioprinter.fdb");

            String pathname = dbFile.getAbsolutePath();
            String username = AppConfig.getDbUser();
            String password = AppConfig.getDbPassword();

            //log.info(username + "======CREDENTIAL==========" +password);

            FBManager manager = new FBManager(GDSType.getType("EMBEDDED"));
            manager.start();
            if (!manager.isDatabaseExists(pathname, username, password)) {
                manager.createDatabase(pathname, username, password);
            }
            manager.stop();

            Configuration cfg = new Configuration();
            cfg.setProperty("hibernate.connection.url", "jdbc:firebirdsql:embedded:" + pathname);
            cfg.setProperty("hibernate.connection.username", username);
            cfg.setProperty("hibernate.connection.password", password);
            cfg.setInterceptor(new EncryptionInterceptor());

            StandardServiceRegistryBuilder ssrb = new StandardServiceRegistryBuilder().applySettings(cfg.configure().getProperties());
            sessionFactory = cfg.buildSessionFactory(ssrb.build());

            crypter = new Crypter();

            AppConfig.getAndSave(ConfigKeys.INSTALL_DATE);
        }
    }

    public <T> T getUniqueByProperty(Class<T> clazz, String prop, String value) {
        Session sxn = null;
        Transaction tx = null;
        try {
            sxn = getSession();
            tx = sxn.beginTransaction();
            Criteria cr = sxn.createCriteria(clazz);
            cr.add(Restrictions.ilike(prop, value, MatchMode.EXACT));
            T t = (T) cr.uniqueResult();
            commit(tx);
            return t;
        } catch (Exception e) {
            rollback(tx);
            log.error("Error while getting unique by property ", e);
        } finally {
            closeSession(sxn);
        }
        return null;
    }

    public <T> List<T> getListByHQL(String hql) {
        Session sxn = null;
        Transaction tx = null;
        try {
            sxn = getSession();
            tx = sxn.beginTransaction();
            Query q = sxn.createQuery(hql);
            List<T> list = q.list();
            commit(tx);
            return list;
        } catch (Exception e) {
            rollback(tx);
            log.error("Error while getting list by hql ", e);
        } finally {
            closeSession(sxn);
        }
        return null;
    }

    public <T extends BaseEntity> boolean create(T t) {
        Session sxn = null;
        Transaction tx = null;
        try {
            sxn = getSession();
            tx = sxn.beginTransaction();
            sxn.save(t);
            commit(tx);
            return true;
        } catch (Exception e) {
            rollback(tx);
            log.error("Error saving " + t.getClass());
        } finally {
            closeSession(sxn);
        }
        return false;
    }

    public <T extends BaseEntity> void createOrUpdate(T t) {
        Session sxn = null;
        Transaction tx = null;
        try {
            sxn = getSession();
            tx = sxn.beginTransaction();
            sxn.saveOrUpdate(t);
            commit(tx);
        } catch (Exception exception) {
            rollback(tx);
            log.error("Error while updating", exception);
            throw new IllegalArgumentException(exception);
        } finally {
            closeSession(sxn);
        }

    }

    public <T> T getUniqueByProperties(Class<T> clazz, Pair<String, String>... params) {
        Session sxn = null;
        Transaction tx = null;
        try {
            sxn = getSession();
            tx = sxn.beginTransaction();
            Criteria cr = sxn.createCriteria(clazz);
            if (params != null && params.length != 0) {
                for (Pair<String, String> param : params) {
                    cr.add(Restrictions.ilike(param.getKey(), param.getValue(), MatchMode.EXACT));
                }
            }
            T t = (T) cr.uniqueResult();
            commit(tx);
            return t;
        } catch (Exception e) {
            rollback(tx);
            log.error("Error while getting unique by property ", e);
        } finally {
            closeSession(sxn);
        }
        return null;
    }

    private Session getSession() throws HibernateException, NamingException {
        return sessionFactory.openSession();
    }

    private void rollback(Transaction tx) {
        if (tx != null) {
            tx.rollback();
        }
    }

    private void closeSession(Session sxn) {
        if (sxn != null) {
            sxn.close();
        }
    }
    public void commit(Transaction tx) {
        if (tx != null) {
            tx.commit();
        }
    }


    public Tag getTag() {
        List<Tag> listByHQL = getListByHQL("select t from Tag t");

        if (listByHQL != null && !listByHQL.isEmpty()) {
            return listByHQL.get(0);
        }

        return null;
    }


    public Config getConfigByKey(String key) {
        return getUniqueByProperty(Config.class, "configKey", crypter.encrypt(key));
    }

    public BioUser getBioUserByUsernameAndPassword(String username, String password) {
        return getUniqueByProperties(BioUser.class, new Pair<>("email", crypter.encrypt(username)), new Pair<>("pw", crypter.encrypt(password)));
    }

    public BioUser getBioUserByUsername(String username) {
        return getUniqueByProperties(BioUser.class, new Pair<>("email", crypter.encrypt(username)));
    }

    public Project getProjectByPid(String pId) {
        return getUniqueByProperties(Project.class, new Pair<>("pId", crypter.encrypt(pId)));
    }

    public IdCard getIdCardByGenId(String genId) {
        return getUniqueByProperty(IdCard.class, "systemId", genId);
    }
}
