/*
 * Copyright 2022-2023 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cedarpolicy;

import static com.cedarpolicy.TestUtil.loadSchemaResource;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cedarpolicy.model.DetailedError;
import com.cedarpolicy.model.ValidationRequest;
import com.cedarpolicy.model.ValidationResponse;
import com.cedarpolicy.model.ValidationResponse.SuccessOrFailure;
import com.cedarpolicy.model.ValidationResponse.ValidationSuccessResponse;
import com.cedarpolicy.model.schema.Schema;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for the validator. */
public class ValidationTests {
    private Schema schema;
    private HashMap<String, String> policies;

    private static AuthorizationEngine engine;

    /** Test. */
    @Test
    public void givenEmptySchemaAndNoPolicyReturnsValid() {
        givenSchema(EMPTY_SCHEMA);
        ValidationResponse response = whenValidated();
        thenIsValid(response);
    }

    /** Test. */
    @Test
    public void givenExampleSchemaAndCorrectPolicyReturnsValid() {
        givenSchema(PHOTOFLASH_SCHEMA);
        givenPolicy(
                "policy0",
                "permit("
                        + "    principal == User::\"alice\","
                        + "    action == Action::\"viewPhoto\","
                        + "    resource == Photo::\"VacationPhoto94.jpg\""
                        + ");");
        ValidationResponse response = whenValidated();
        thenIsValid(response);
    }

    /** Test. */
    @Test
    public void givenExampleSchemaAndIncorrectPolicyReturnsValid() {
        givenSchema(PHOTOFLASH_SCHEMA);
        givenPolicy(
                "policy0",
                "permit(\n"
                        + "    principal == User::\"alice\","
                        + "    action == Action::\"viewPhoto\","
                        + "    resource == User::\"bob\""
                        + ");");
        ValidationResponse response = whenValidated();
        thenIsNotValid(response);
    }

    /** Test. */
    @Test
    public void givenInvalidPolicyThrowsBadRequestError() {
        givenSchema(EMPTY_SCHEMA);
        givenPolicy("policy0", "permit { }");
        ValidationResponse response = whenValidated();
        thenValidationFailed(response);
    }

    private void givenSchema(Schema testSchema) {
        this.schema = testSchema;
    }

    private void givenPolicy(String id, String policy) {
        this.policies.put(id, policy);
    }

    private ValidationResponse whenValidated() {
        ValidationRequest request = new ValidationRequest(schema, policies);
        return assertDoesNotThrow(() -> engine.validate(request));
    }

    private void thenIsValid(ValidationResponse response) {
        assertEquals(response.type, SuccessOrFailure.Success);
        final ValidationSuccessResponse success = assertDoesNotThrow(() -> response.success.get());
        assertTrue(
            success.validationErrors.isEmpty(),
                () -> {
                    String errors =
                        response.success.get().validationErrors.stream()
                            .map(note ->
                                String.format("in policy %s: %s", note.getPolicyId(), note.getError()))
                                    .collect(Collectors.joining("\n"));
                    return "Expected valid response but got validation errors:\n" + errors;
                });
    }

    private void thenIsNotValid(ValidationResponse response) {
        assertEquals(response.type, SuccessOrFailure.Success);
        final ValidationSuccessResponse success = assertDoesNotThrow(() -> response.success.get());
        assertFalse(
            success.validationErrors.isEmpty(),
            () -> {
                return "Expected validation errors but did not find any";
            }
            );
    }

    private void thenValidationFailed(ValidationResponse response) {
        assertEquals(response.type, SuccessOrFailure.Failure);
        final List<DetailedError> errors = assertDoesNotThrow(() -> response.errors.get());
        assertFalse(errors.isEmpty());
    }

    @BeforeAll
    private static void setUp() {
        engine = new BasicAuthorizationEngine();
    }

    @BeforeEach
    private void reset() {
        this.schema = null;
        this.policies = new HashMap<>();
    }

    private static final Schema EMPTY_SCHEMA = loadSchemaResource("/empty_schema.json");
    private static final Schema PHOTOFLASH_SCHEMA = loadSchemaResource("/photoflash_schema.json");
}
