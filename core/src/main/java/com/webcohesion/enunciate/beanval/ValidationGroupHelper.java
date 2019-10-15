package com.webcohesion.enunciate.beanval;

import javax.lang.model.element.AnnotationMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class ValidationGroupHelper {

    static List<String> getGroupsOnField(String groupsAsString) {
        List<String> classes = new ArrayList<>();

        final String validationGroupsPrefix = "groups()={";
        int indexOfGroups = groupsAsString.indexOf(validationGroupsPrefix);

        if (indexOfGroups == -1) {
            return Collections.emptyList();
        }

        String tail = groupsAsString.substring(indexOfGroups);
        int indexOfClosingBrace = tail.indexOf("}");


        classes = addClasses(classes, tail.substring(validationGroupsPrefix.length(), indexOfClosingBrace));

        return classes;
    }

    private static List<String> addClasses(List<String> classes, String validationGroups) {

        return Stream.of(validationGroups.split(",")).map(ValidationGroupHelper::stripDotClass).collect(toList());
    }

    private static String stripDotClass(String className) {

        return className.replace(".class", "").trim();

    }

    private static List<String> getActiveValidationGroups(String activeValidationGroups) {
        if (activeValidationGroups != null && activeValidationGroups.length() > 0) {

            return Stream.of(activeValidationGroups.split(",")).map(String::trim).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static boolean hasMatchingValidationGroup(String targetGroups, AnnotationMirror annotationMirror) {
        // get validationGroups from Enunciate config file, this holds the active validation groups (if any) in attribute
        // "beanValidationGroups" of jackson module:
        // e.g. <jackson disabled="false" beanValidationGroups="com.ifyouwannabecool.beanval.DataAPI"/>
        List<String> activeValidationGroups = ValidationGroupHelper.getActiveValidationGroups(targetGroups);
        if (annotationMirror != null) {
            List<String> groupsOnField = getGroupsOnField(annotationMirror.getElementValues().toString());

            if (runningWithDefaultGroupOnFieldInDefaultGroup(groupsOnField, activeValidationGroups)) {
                return true;
            }
            if (validationGroupOnFieldMatchesWithActiveGroup(groupsOnField, activeValidationGroups)) {
                return true;
            }
        }
        return false;
    }

    /**
     * When Enunciate XML does not have validationGroups specified then the constraint is only active when the field has
     * no groups specified either.
     */
    private static boolean runningWithDefaultGroupOnFieldInDefaultGroup(List<String> groupsOnField, List<String> activeValidationGroups) {
        return groupsOnField.isEmpty() && activeValidationGroups.isEmpty();
    }

    /**
     * if at least one validation group matches, then the constraint should be applied
     */
    private static boolean validationGroupOnFieldMatchesWithActiveGroup(List<String> groupsOnField, List<String> activeValidationGroups) {
        return groupsOnField.stream().anyMatch(activeValidationGroups::contains);
    }

}
