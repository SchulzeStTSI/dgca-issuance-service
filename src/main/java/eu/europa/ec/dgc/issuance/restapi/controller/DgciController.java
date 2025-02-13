/*-
 * ---license-start
 * EU Digital Green Certificate Issuance Service / dgca-issuance-service
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package eu.europa.ec.dgc.issuance.restapi.controller;

import ehn.techiop.hcert.data.Eudgc;
import eu.europa.ec.dgc.issuance.restapi.dto.DgciIdentifier;
import eu.europa.ec.dgc.issuance.restapi.dto.DgciInit;
import eu.europa.ec.dgc.issuance.restapi.dto.DidDocument;
import eu.europa.ec.dgc.issuance.restapi.dto.EgdcCodeData;
import eu.europa.ec.dgc.issuance.restapi.dto.IssueData;
import eu.europa.ec.dgc.issuance.restapi.dto.SignatureData;
import eu.europa.ec.dgc.issuance.service.DgciService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dgci")
@AllArgsConstructor
public class DgciController {

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

    @Operation(
        summary = "create qr code of edgc",
        description = "create edgc for given data"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "signed edgc qr code created"),
        @ApiResponse(responseCode = "400", description = "wrong issue data")})
    @PutMapping(value = "/issue", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EgdcCodeData> createEdgc(@Valid @RequestBody Eudgc eudgc) {
        EgdcCodeData egdcCodeData = dgciService.createEdgc(eudgc);
        return ResponseEntity.ok(egdcCodeData);
    }

    @Operation(
        summary = "Returns a DID document",
        description = "Return a DID document"
    )
    @GetMapping(value = "/{dgciHash}")
    public ResponseEntity<DidDocument> getDidDocument(@PathVariable String dgciHash) {
        return ResponseEntity.ok(dgciService.getDidDocument(dgciHash));
    }

    /**
     * dgci status.
     * @param dgciHash hash
     * @return response
     */
    @Operation(
        summary = "Checks the status of DGCI",
        description = "Produce status HTTP code message"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "dgci exists"),
        @ApiResponse(responseCode = "424", description = "dgci locked"),
        @ApiResponse(responseCode = "404", description = "dgci not found")})
    @RequestMapping(value = "/{dgciHash}",method = RequestMethod.HEAD)
    public ResponseEntity<Void> dgciStatus(@PathVariable String dgciHash) {
        HttpStatus httpStatus;
        switch (dgciService.checkDgciStatus(dgciHash)) {
            case EXISTS:
                httpStatus = HttpStatus.NO_CONTENT;
                break;
            case LOCKED:
                httpStatus = HttpStatus.LOCKED;
                break;
            case NOT_EXISTS:
                httpStatus = HttpStatus.NOT_FOUND;
                break;
            default:
                throw new IllegalArgumentException("unknown dgci status");
        }
        return ResponseEntity.status(httpStatus).build();
    }
}
