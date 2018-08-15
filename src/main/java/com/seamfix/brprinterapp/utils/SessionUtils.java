package com.seamfix.brprinterapp.utils;

import com.seamfix.brprinterapp.controller.LandingPageController;
import com.seamfix.brprinterapp.model.BioUser;
import com.seamfix.brprinterapp.model.Project;
import com.seamfix.brprinterapp.model.Tag;
import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;


public class SessionUtils {


    @Getter
    @Setter
    private static BioUser loggedInUser;

    @Getter
    @Setter
    private static Tag currentTag;

    @Getter
    @Setter
    private static Project currentProject;

    @Getter
    @Setter
    private static LandingPageController landingPageController;



    public static Set<Project> getLoggedInUserProjects() {
        if (loggedInUser == null) {
            return new HashSet<>();
        }

        Set<Project> projects = loggedInUser.getProjects();
        if (projects == null || projects.isEmpty()) {
            return new HashSet<>();
        }

        Set<Project> sorted = projects.stream().filter(Project::isActive).sorted(Comparator.comparing(o -> o.getName().toUpperCase())).collect(Collectors.toCollection(LinkedHashSet::new));
        return sorted;
    }
    public static String getLoggedInUserEmail() {
        return loggedInUser == null ? "" : loggedInUser.getEmail();
    }

    public static String getCurrentTagValue() {
        return currentTag == null ? "0" : currentTag.getTag();
    }

}
