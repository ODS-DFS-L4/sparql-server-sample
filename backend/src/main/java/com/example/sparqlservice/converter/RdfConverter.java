package com.example.sparqlservice.converter;

import org.apache.jena.rdf.model.Model;

public interface RdfConverter<T> {
  Model convert(T entity);
}
