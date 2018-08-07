//package com.seamfix.brprinterapp.utils;
//
//import org.hibernate.EmptyInterceptor;
//
//import java.util.Arrays;
//import java.util.List;
//
//public class EncryptionInterceptor extends EmptyInterceptor {
//
//    private static final List<Class> encryptedEntities = Arrays.asList(CapturedDataEntity.class, Project.class, MetaData.class, BioUser.class, Config.class, Tag.class, ControlToLabelMapper.class, ProjectSetting.class,
//            LocationSettingBreakdown.class, CaptureDraft.class, NotificationEntity.class, ReEnrolEntity.class, ReEnrolNotificationEntity.class);
//    private final Crypter crypter;
//
//    public EncryptionInterceptor() {
//        crypter = new Crypter();
//    }
//
//}
