package com.example.sparqlservice.aop;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.example.sparqlservice.repository.GeneralRdfRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateModifiedAspect {
  private final GeneralRdfRepository generalRdfRepository;

  /**
   * This method is triggered after the execution of any repository class method
   * annotated with {@link com.example.sparqlservice.annotation.UpdateModifiedAt}.
   * It updates the modified timestamp in the RDF store.
   */
  @AfterReturning("@annotation(com.example.sparqlservice.annotation.UpdateModifiedAt)")
  public void updateModifiedAt() {
    log.debug("UpdateModifiedAt Annotation process starts...");
    try {
      generalRdfRepository.updateModifiedTimestamp();
    } catch (Exception e) {
      log.error("Failed to update modified timestamp: {}", e.getMessage());
    }
  }
}
