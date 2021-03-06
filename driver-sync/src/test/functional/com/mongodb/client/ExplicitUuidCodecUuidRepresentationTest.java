/*
 * Copyright 2008-present MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mongodb.client;

import org.bson.BsonBinarySubType;
import org.bson.UuidRepresentation;
import org.bson.codecs.UuidCodec;
import org.bson.codecs.configuration.CodecRegistry;
import org.junit.After;

import java.util.UUID;

public class ExplicitUuidCodecUuidRepresentationTest extends AbstractExplicitUuidCodecUuidRepresentationTest {
    private MongoClient mongoClient;

    public ExplicitUuidCodecUuidRepresentationTest(final UuidRepresentation uuidRepresentationForClient,
                                                   final UuidRepresentation uuidRepresentationForExplicitEncoding,
                                                   final BsonBinarySubType subType,
                                                   final UuidCodec uuidCodec, final UUID uuid, final byte[] encodedValue,
                                                   final byte[] standardEncodedValue) {
        super(uuidRepresentationForClient, uuidRepresentationForExplicitEncoding, subType, uuidCodec, uuid, encodedValue,
                standardEncodedValue);
    }

    @Override
    protected void createMongoClient(final UuidRepresentation uuidRepresentation, final CodecRegistry codecRegistry) {
        mongoClient = MongoClients.create(Fixture.getMongoClientSettingsBuilder()
                .uuidRepresentation(uuidRepresentation)
                .codecRegistry(codecRegistry)
                .build());
    }

    @Override
    protected MongoDatabase getDatabase(final String databaseName) {
        return mongoClient.getDatabase(databaseName);
    }

    @After
    public void cleanUp() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
