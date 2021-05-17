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

import com.nimbusds.jose.util.Base64URL;
import eu.europa.ec.dgc.issuance.restapi.dto.DidDocument;
import eu.europa.ec.dgc.issuance.service.DgciService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.Base64;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dgci")
@ConditionalOnExpression("${dgcihelper.enabled}==true")
@AllArgsConstructor
public class DgciController {

    private final DgciService dgciService;

    @Operation(
        summary = "Returns a DID document",
        description = "Return a DID document"
    )
    @GetMapping(value = "/{dgciHash}")
    public ResponseEntity<DidDocument> getDidDocument(@PathVariable String dgciHash) {
        dgciHash = Base64.getEncoder().encodeToString(Base64URL.from(dgciHash).decode());
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
        dgciHash = Base64.getEncoder().encodeToString(Base64URL.from(dgciHash).decode());
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
