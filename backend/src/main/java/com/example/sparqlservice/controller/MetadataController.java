package com.example.sparqlservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sparqlservice.DTO.response.ModifiedAtResponseDTO;
import com.example.sparqlservice.service.RdfService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "メタデータ管理", description = "メタデータの管理と取得を行うエンドポイント。 ")
@Slf4j
@RestController
@RequestMapping("/api/metadata")
@RequiredArgsConstructor
public class MetadataController {
  private final RdfService rdfService;

  @GetMapping("/last-modified")
  @Operation(summary = "最終更新日時を取得する。", description = "RDFストアの最終更新日時を取得する。", responses = {
      @ApiResponse(responseCode = "200", description = "Successful response", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ModifiedAtResponseDTO.class), examples = @ExampleObject(value = "{ \"lastModifiedAt\": \"2025-01-24T14:30:00Z\" }"))),
      @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(examples = @ExampleObject(value = "{}")))
  })
  public ResponseEntity<ModifiedAtResponseDTO> getLastModified() {
    ModifiedAtResponseDTO response = new ModifiedAtResponseDTO();

    log.debug("Fetching last modified timestamp");
    String lastModifiedTimestamp = rdfService.getOrUpdateModifiedTimestamp();
    response.setLastModifiedAt(lastModifiedTimestamp);
    return ResponseEntity.ok().body(response);
  }
}
