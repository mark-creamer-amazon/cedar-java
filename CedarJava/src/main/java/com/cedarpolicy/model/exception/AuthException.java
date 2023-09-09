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

package com.cedarpolicy.model.exception;

import java.util.Optional;

/**
 * Exception thrown when errors are encountered that prevent communication with the authorization
 * engine. Note that this does not include errors that are generated by the authorization engine
 * while evaluating a request. These errors are included in the <code>errors</code> field of the
 * AuthorizationResponse.
 */
public class AuthException extends Exception {
    /** Generated Error Message. */
    public final Optional<String> message;

    /** Exception that caused the failure. */
    public final Optional<Throwable> cause;

    /**
     * Exceptions encountered during authorization.
     *
     * @param message Error Message
     */
    public AuthException(String message) {
        super(message);
        this.message = Optional.of(message);
        this.cause = Optional.empty();
    }

    /**
     * Constructor to build an exception with both a Throwable cause and an error message.
     *
     * @param message useful debugging message
     * @param cause parent exception that caused this exception
     */
    public AuthException(String message, Throwable cause) {
        super(message, cause);
        this.message = Optional.of(message);
        this.cause = Optional.of(cause);
    }

    /**
     * Constructor that just takes the Throwable cause.
     *
     * @param cause parent exception that caused this exception
     */
    public AuthException(Throwable cause) {
        super("Auth Exception", cause);
        this.cause = Optional.of(cause);
        this.message = Optional.empty();
    }

    /** Display the error message. */
    public String toString() {
        return "Auth Exception: "
                + this.message.map(x -> x + ": ").orElse("")
                + this.cause.map(Throwable::toString).orElse("");
    }
}