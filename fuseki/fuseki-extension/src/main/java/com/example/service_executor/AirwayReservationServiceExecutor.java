package com.example.service_executor;


import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.QueryEngineMain;
import org.apache.jena.sparql.service.single.ServiceExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.service.AirwayService;

public class AirwayReservationServiceExecutor implements ServiceExecutor {
  private static final Logger logger = LoggerFactory.getLogger(AirwayReservationServiceExecutor.class);

  private final AirwayService airwayService = new AirwayService();

  @Override
  public QueryIterator createExecution(OpService opExecute, OpService original, Binding binding,
      ExecutionContext execCxt) {

    // Get Service URI
    String serviceURI = opExecute.getService().getURI();
    logger.debug("Service URI: {}", serviceURI);

    // Check if the Service URI matches this Executor.
    if (!serviceURI.startsWith("custom:AirwayReservation")) {
      logger.debug("The Service URI does not start with 'custom:AirwayReservation'");
      return null;
    }

    // Get Airway Reservation Model Object from API
    Model localModel = ModelFactory.createDefaultModel();


    try {
      localModel = airwayService.getAirwayReservationModel(serviceURI);
    } catch (Exception e) {
      logger.error(e.getMessage());
      return null;
    }

    // Get the sub-query in the Service clause.
    Op subOp = opExecute.getSubOp();
    DatasetGraph localDSG = DatasetFactory.create(localModel).asDatasetGraph();
    ExecutionContext localExecCxt = new ExecutionContext(
        execCxt.getContext(),
        localDSG.getDefaultGraph(),
        execCxt.getDataset(),
        execCxt.getExecutor());

    // Execute the sub-query
    QueryIterator subOpResult = new QueryEngineMain(subOp, localDSG, binding, localExecCxt.getContext()).eval(subOp,
        localDSG, binding, localExecCxt.getContext());

    return subOpResult;
  }
}