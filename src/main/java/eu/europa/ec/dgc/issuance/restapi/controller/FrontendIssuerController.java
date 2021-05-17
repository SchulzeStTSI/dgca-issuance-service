package eu.europa.ec.dgc.issuance.restapi.controller;

import eu.europa.ec.dgc.issuance.restapi.dto.DgciIdentifier;
import eu.europa.ec.dgc.issuance.restapi.dto.DgciInit;
import eu.europa.ec.dgc.issuance.restapi.dto.IssueData;
import eu.europa.ec.dgc.issuance.restapi.dto.SignatureData;
import eu.europa.ec.dgc.issuance.service.DgciService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dgci")
@ConditionalOnExpression("'${issuance.type}'=='frontend' && ${issuance.enabled}==true")
@AllArgsConstructor
public class FrontendIssuerController {
    
    private final DgciService dgciService;

    @Operation(
        summary = "Prepares an DGCI for the Code Generation in Frontend",
        description = "Creates new dgci and return meta data for certificate creation"
    )
    @PostMapping(value = "/issue", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DgciIdentifier> initDgci(@Valid @RequestBody DgciInit dgciInit) {
        return ResponseEntity.ok(dgciService.initDgci(dgciInit));
    }

    @Operation(
        summary = "Completes the issuing process",
        description = "calculate cose signature for given certificate hash, "
            + "generate TAN and update DGCI Registry database"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "signature created"),
        @ApiResponse(responseCode = "404", description = "dgci with related id not found"),
        @ApiResponse(responseCode = "400", description = "wrong issue data")})
    @PutMapping(value = "/issue/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SignatureData> finalizeDgci(@PathVariable long id, @Valid @RequestBody IssueData issueData)
        throws Exception {
        return ResponseEntity.ok(dgciService.finishDgci(id, issueData));
    }



}
