package com.seamfix.brprinterapp.utils;

import com.seamfix.brprinterapp.model.Config;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import javax.persistence.Column;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Log4j
public class EncryptionInterceptor extends EmptyInterceptor {

    private static final List<Class> encryptedEntities = Arrays.asList(Config.class);
    private final Crypter crypter;

    public EncryptionInterceptor() {
        crypter = new Crypter();
    }


    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        return doSaveOrFlushAction(entity, state);
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        return doSaveOrFlushAction(entity, currentState);
    }

    /**
     * This method is invoked after any transaction is committed.
     * After a typical transaction, the db entities are propagated back to the objects used to save them.
     * This means that when we save, our object will have encrypted properties. This method will be used to decrypt them.
     *
     * @param entities entities involved in committed transaction
     */
    @Override
    public void postFlush(Iterator entities) {
        entities.forEachRemaining(entity -> {
            if (encryptedEntities.contains(entity.getClass())) {
                Field[] declaredFields = entity.getClass().getDeclaredFields();
                for (Field declaredField : declaredFields) {
                    Class<?> type = declaredField.getType();
                    if (!type.equals(String.class)) {
                        continue;
                    }

                    boolean isColumn = declaredField.isAnnotationPresent(Column.class);
                    if (!isColumn) {
                        continue;
                    }

                    try {
                        PropertyDescriptor pd = new PropertyDescriptor(declaredField.getName(), entity.getClass());
                        String value = (String) pd.getReadMethod().invoke(entity);
                        if (StringUtils.isNotBlank(value)) {
                            pd.getWriteMethod().invoke(entity, crypter.decrypt(value));
                        }
                    } catch (Exception e) {
                        log.error("Error while reflecting entity in post flush " + declaredField.getName(), e);
                    }

                }
            }
        });
    }

    private boolean doSaveOrFlushAction(Object entity, Object[] state) {
        if (encryptedEntities.contains(entity.getClass())) {
            try {
                for (int i = 0; i < state.length; i++) {
                    if (state[i] instanceof String) {
                        String plainText = (String) state[i];
                        String actualPlainText = crypter.decrypt(plainText);
                        if (StringUtils.isNotBlank(actualPlainText)) {
                            state[i] = crypter.encrypt(actualPlainText);
                        }
                    }
                }
                return true;
            } catch (Exception e) {
                log.error("Error while saving/flushing " + entity.getClass().getSimpleName(), e);
            }
        }
        return false;
    }
}
