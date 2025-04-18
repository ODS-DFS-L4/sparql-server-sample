package com.example.sparqlservice.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "RDFストアの最終更新日時")
public class ModifiedAtResponseDTO {
  private String lastModifiedAt;
}
