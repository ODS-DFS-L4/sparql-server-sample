package com.example.sparqlservice.validator;

import com.example.sparqlservice.DTO.request.DronePortIntrusionRequestDTO.DetectionEvent.Location;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LocationValidator
    implements ConstraintValidator<ValidLocation, Location> {

  @Override
  public boolean isValid(Location location, ConstraintValidatorContext context) {
    if (location == null) {
      return true;
    }

    // When both of them are null, or both of them are non-null, then returns true.
    return (location.getLatitude() == null && location.getLongitude() == null) ||
        (location.getLatitude() != null && location.getLongitude() != null);
  }

}
