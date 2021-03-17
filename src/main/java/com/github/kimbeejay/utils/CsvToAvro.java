// Copyright 2018 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.github.kimbeejay.utils;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CsvToAvro {

  private final String delimiter;
  private final Schema schema;

  public CsvToAvro(Schema schema, String delimiter) {
    Objects.requireNonNull(delimiter);
    Objects.requireNonNull(schema);

    this.delimiter = delimiter;
    this.schema = schema;
  }

  public GenericRecord convert(String row) throws IllegalArgumentException {
    // Split CSV row into using delimiter
    List<String> values = Arrays.stream(row.split(this.delimiter))
        .map(String::trim)
        .collect(Collectors.toList());

    GenericRecord record = new GenericData.Record(this.schema);
    List<Schema.Field> fields = this.schema.getFields();

    for (int i = 0; i < fields.size(); i++) {
      String type = fields.get(i).schema().getType().getName().toLowerCase();

      switch (type) {
        case "string": {
          record.put(i, values.get(i));
          break;
        }

        case "boolean": {
          record.put(i, Boolean.valueOf(values.get(i)));
          break;
        }

        case "int": {
          try {
            record.put(i, Integer.valueOf(values.get(i)));
          } catch (NumberFormatException e) {
            e.printStackTrace();
            record.put(i, 0);
          }
          break;
        }

        case "long": {
          try {
            record.put(i, Long.valueOf(values.get(i)));
          } catch (NumberFormatException e) {
            e.printStackTrace();
            record.put(i, 0L);
          }

          break;
        }

        case "float": {
          try {
            record.put(i, Float.valueOf(values.get(i)));
          } catch (NumberFormatException e) {
            e.printStackTrace();
            record.put(i, 0f);
          }
          break;
        }

        case "double": {
          try {
            record.put(i, Double.valueOf(values.get(i)));
          } catch (NumberFormatException e) {
            e.printStackTrace();
            record.put(i, 0);
          }
          break;
        }

        default: {
          throw new IllegalArgumentException("Field type " + type + " is not supported.");
        }
      }
    }

    return record;
  }
}
