package com.teuida.jikimi.common.validation;

import com.teuida.jikimi.common.enums.CourseType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class CourseTypesValidator implements ConstraintValidator<ValidCourseTypes, Set<CourseType>> {

    @Override
    public void initialize(ValidCourseTypes constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Set<CourseType> courseTypes, ConstraintValidatorContext context) {
        // Null or empty sets are valid (field is optional)
        if (courseTypes == null || courseTypes.isEmpty()) {
            return true;
        }

        // If ALL is present, the set must contain only ALL
        if (courseTypes.contains(CourseType.ALL)) {
            return courseTypes.size() == 1;
        }

        // Otherwise, any combination of non-ALL types is valid
        return true;
    }
}
