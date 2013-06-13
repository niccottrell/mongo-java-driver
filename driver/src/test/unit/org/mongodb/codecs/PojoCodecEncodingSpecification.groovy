/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
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

package org.mongodb.codecs

import org.bson.BSONWriter
import org.mongodb.codecs.pojo.ObjectWithArray
import org.mongodb.codecs.pojo.ObjectWithMapOfStrings
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Subject

import static org.junit.Assert.fail

class PojoCodecEncodingSpecification extends Specification {
    private final BSONWriter bsonWriter = Mock();

    @Subject
    private final PojoCodec<Object> pojoCodec = new PojoCodec<Object>(Codecs.createDefault(), null);

    def 'should encode simple pojo'() {
        setup:
        String valueInSimpleObject = 'MyName';

        when:
        pojoCodec.encode(bsonWriter, new SimpleObject(valueInSimpleObject));

        then:
        1 * bsonWriter.writeStartDocument();
        1 * bsonWriter.writeName('name');
        1 * bsonWriter.writeString(valueInSimpleObject);
        1 * bsonWriter.writeEndDocument();
    }

    def 'should encode pojo containing other pojos'() {
        setup:
        String anotherName = 'AnotherName';

        when:
        pojoCodec.encode(bsonWriter, new NestedObject(new SimpleObject(anotherName)));

        then:
        1 * bsonWriter.writeStartDocument();
        1 * bsonWriter.writeName('mySimpleObject');
        1 * bsonWriter.writeStartDocument();
        1 * bsonWriter.writeName('name');
        1 * bsonWriter.writeString(anotherName);
        2 * bsonWriter.writeEndDocument();
    }

    def 'should encode pojo containing other pojos and fields'() {
        when:
        pojoCodec.encode(bsonWriter, new NestedObjectWithFields(98, new SimpleObject('AnotherName')));

        then:
        1 * bsonWriter.writeStartDocument();
        1 * bsonWriter.writeName('intValue');
        1 * bsonWriter.writeInt32(98);
        1 * bsonWriter.writeName('mySimpleObject');
        1 * bsonWriter.writeStartDocument();
        1 * bsonWriter.writeName('name');
        1 * bsonWriter.writeString('AnotherName');
        2 * bsonWriter.writeEndDocument();
    }

    def 'should support arrays'() {
        when:
        pojoCodec.encode(bsonWriter, new ObjectWithArray());

        then:
        1 * bsonWriter.writeStartDocument();
        then:
        1 * bsonWriter.writeName('theStringArray');
        then:
        1 * bsonWriter.writeStartArray();
        then:
        1 * bsonWriter.writeString('Uno');
        then:
        1 * bsonWriter.writeString('Dos');
        then:
        1 * bsonWriter.writeString('Tres');
        then:
        1 * bsonWriter.writeEndArray();
        then:
        1 * bsonWriter.writeEndDocument();
    }

    def 'should encode maps of primitive types'() {
        when:
        pojoCodec.encode(bsonWriter, new ObjectWithMapOfStrings());

        then:
        1 * bsonWriter.writeStartDocument();
        1 * bsonWriter.writeName('theMap');
        1 * bsonWriter.writeStartDocument();
        1 * bsonWriter.writeName('first');
        1 * bsonWriter.writeString('the first value');
        1 * bsonWriter.writeName('second');
        1 * bsonWriter.writeString('the second value');
        2 * bsonWriter.writeEndDocument();
    }

    def 'should encode maps of objects'() {
        setup:
        String simpleObjectValue = 'theValue';

        when:
        pojoCodec.encode(bsonWriter, new ObjectWithMapOfObjects(simpleObjectValue));

        then:
        1 * bsonWriter.writeStartDocument();
        1 * bsonWriter.writeName('theMap');
        1 * bsonWriter.writeStartDocument();
        1 * bsonWriter.writeName('first');
        1 * bsonWriter.writeStartDocument();
        1 * bsonWriter.writeName('name');
        1 * bsonWriter.writeString(simpleObjectValue);
        3 * bsonWriter.writeEndDocument();
    }

    def 'should encode maps of maps'() {
        when:
        pojoCodec.encode(bsonWriter, new ObjectWithMapOfMaps());

        then:
        1 * bsonWriter.writeStartDocument();
        1 * bsonWriter.writeName('theMap');
        1 * bsonWriter.writeStartDocument();
        1 * bsonWriter.writeName('theMapInsideTheMap');
        1 * bsonWriter.writeStartDocument();
        1 * bsonWriter.writeName('innerMapField');
        1 * bsonWriter.writeString('theInnerMapFieldValue');
        3 * bsonWriter.writeEndDocument();
    }

    def 'should not encode special fields like jacoco data'() {
        setup:
        JacocoDecoratedObject jacocoDecoratedObject = new JacocoDecoratedObject('thisName');

        when:
        pojoCodec.encode(bsonWriter, jacocoDecoratedObject);

        then:
        1 * bsonWriter.writeStartDocument();
        1 * bsonWriter.writeEndDocument();
    }

    @Ignore('not implemented')
    def 'should encode ids'() {
        setup:
        fail('Not implemented');
    }

    @Ignore('not implemented')
    def 'should throw an exception when it cannot encode a field'() {
        setup:
        fail('Not implemented');
    }

    @Ignore('not implemented')
    def 'should encode enums as strings'() {
        setup:
        fail('Not implemented');
    }

    private static class JacocoDecoratedObject {
        @SuppressWarnings('FieldName')
        private final String $name;

        JacocoDecoratedObject(String name) {
            this.$name = name;
        }
    }

    private static class SimpleObject {
        private final String name;

        SimpleObject(String name) {
            this.name = name;
        }
    }

    private static class NestedObject {
        private final SimpleObject mySimpleObject;

        NestedObject(SimpleObject mySimpleObject) {
            this.mySimpleObject = mySimpleObject;
        }
    }

    private static class NestedObjectWithFields {
        private final int intValue;
        private final SimpleObject mySimpleObject;

        NestedObjectWithFields(int intValue, SimpleObject mySimpleObject) {
            this.intValue = intValue;
            this.mySimpleObject = mySimpleObject;
        }
    }

    private static final class ObjectWithMapOfObjects {
        private final Map<String, SimpleObject> theMap = [:];

        private ObjectWithMapOfObjects(String theValue) {
            theMap.put('first', new SimpleObject(theValue));
        }
    }

    private static final class ObjectWithMapOfMaps {
        private final Map<String, Map<String, String>> theMap = ['theMapInsideTheMap': ['innerMapField': 'theInnerMapFieldValue']];
    }

}
