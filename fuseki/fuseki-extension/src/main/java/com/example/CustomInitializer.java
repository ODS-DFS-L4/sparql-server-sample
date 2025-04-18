package com.example;


import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sys.JenaSubsystemLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.service_executor.AirwayReservationServiceExecutor;
import com.example.service_executor.DronePortReservationServiceExecutor;

public class CustomInitializer implements JenaSubsystemLifecycle {
  private static final Logger logger = LoggerFactory.getLogger(CustomInitializer.class);

  @Override
  public void start() {
    logger.info("Starting the initialization process...");
    ServiceExecutorRegistry registry = ServiceExecutorRegistry.get();
    registry.add(new AirwayReservationServiceExecutor());
    registry.add(new DronePortReservationServiceExecutor());
    logger.info("Initialization process completed successfully.");
  }

  @Override
  public void stop() {
  }

  @Override
  public int level() {
    return 50;
  }

}
